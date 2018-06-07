package edu.upc.whatsapp.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.glassfish.tyrus.client.ClientManager;

import java.io.IOException;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import edu.upc.whatsapp.MadSecurity;
import edu.upc.whatsapp.R;
import edu.upc.whatsapp._GlobalState;
import edu.upc.whatsapp.a_WelcomeActivity;
import edu.upc.whatsapp.e_MessagesActivity;
import entity.Message;

public class PushService extends Service {
  private _GlobalState globalState;
  private Timer timer;
  private boolean connectedToServer;
  private Session session;
  private String PUSHSERVER ="mads.dnsdynamic.com:62987/whatsappServer";
  //address need to be local in file when starting solo on boot.
  @Override
  public void onCreate() {
    super.onCreate();
    globalState = (_GlobalState) getApplication();
   // toastShow("PushService created");
    Log.d("DEBUG","PushService created");
    timer = new Timer();
    timer.scheduleAtFixedRate(new MyTimerTask(), 3000, 20000);
    globalState.load_my_user();
    sendPushInfoNotification(globalState.getApplicationContext(),"Connected to server");

  }
  
  private class MyTimerTask extends TimerTask {
    public void run() {
      if(globalState.my_user==null){
        sendMessageToHandler("trouble","cant find local user");

        return;
      }
      ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo networkInfo = conMan.getActiveNetworkInfo();
      if (networkInfo != null && networkInfo.isConnected() && !connectedToServer) {
        connectToServer();
      }
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flag, int startId) {
    super.onStartCommand(intent, flag, startId);
    return Service.START_STICKY;
  }



  @Override
  public void onDestroy() {
    super.onDestroy();
    closeAllNotifications();
    toastShow("PushService destroyed");
    Log.d("DEBUG", "onDestroy pushService method called");
    disconnectFromServer();
    if(timer!=null)
      timer.cancel();
    new Thread(new Runnable() {
      public void run() {
        disconnectFromServer();
      }
    }).start();
  }

  private final IBinder myBinder = new MyBinder();
  public class MyBinder extends Binder {
    PushService getService() {
      return PushService.this;
    }
  }
  @Override
  public IBinder onBind(Intent intent) {
    toastShow("PushService bound");
    return myBinder;
  }


//ENDPOINT
  private void connectToServer(){
    try {
      ClientManager client = ClientManager.createClient();
      session=client.connectToServer(new PushService.MyEndPoint(),ClientEndpointConfig.Builder.create().build(),URI.create("ws://"+PUSHSERVER+"/push"));
      sendMessageToHandler("open","connected to push");
      connectedToServer=true;

    }
    catch (Exception e) {
      //e.printStackTrace();
      sendMessageToHandler("error","connectToServer error");
      connectedToServer = false;
      session = null;
    }
  }
  private void disconnectFromServer(){
    Log.d("DEBUG","Session;"+session);
    if(session!=null){
      Log.d("DEBUG","Disconnect from server called...");
      try {
        session.close();
       // sendMessageToHandler("close","disconnectFromServer");

      } catch (IOException e) {
        //e.printStackTrace();
        sendMessageToHandler("error","disconnectFromServer error");
      }
    }
  }
  //this is executed by an independent thread:


  public class MyEndPoint extends Endpoint {

    @Override
    public void onOpen(Session session, EndpointConfig EndpointConfig) {
      try {
        Gson gson = new Gson();
        session.getBasicRemote().sendText(gson.toJson(globalState.my_user));
        //sendMessageToHandler("open","Push connection opened");
        session.addMessageHandler(new MessageHandler.Whole<String>() {

          @Override
          public void onMessage(String message) {
        sendMessageToHandler("message",message);
          }
        });

      }
      catch (Exception e) {
        //e.printStackTrace();
        sendMessageToHandler("error","onOpen error: "+e.getMessage());
      }
    }

    @Override
    public void onError(Session session, Throwable t) {
     // t.printStackTrace();
      sendMessageToHandler("error","onError error: "+t.getMessage());
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
      //sendMessageToHandler("close","connection closed");
      Log.d("DEBUG","PushService: Connection Closed");
      disconnectFromServer();
      connectedToServer = false;
      PushService.this.session = null;
    }
  }


  private void sendMessageToHandler(String type, String content){
    android.os.Message msg = handler.obtainMessage();
    Bundle bundle = new Bundle();
    bundle.putCharSequence("type", type);
    bundle.putCharSequence("content", content);
    msg.setData(bundle);
    handler.sendMessage(msg);
  }

  @SuppressLint("HandlerLeak")
  Handler handler = new Handler() {

  @Override
    public void handleMessage(android.os.Message msg) {
      String type = msg.getData().getCharSequence("type").toString();
      String content = msg.getData().getCharSequence("content").toString();

      if(type.equals("message")){

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
        Message message = (Message) gson.fromJson(content,Message.class);
        Log.d("DEBUG", "Got push from " + message.getUserSender().getName());

          globalState.user_to_talk_to=message.getUserSender();
          globalState.save_user_to_talk_to(); // in case of app closed while notification still exist

        Gson  gsonMsg = new Gson();
        String parsedMsg = gsonMsg.toJson(message);

        Intent intent= new Intent("localBroadcastMessage");
        intent.putExtra("messageContent",parsedMsg);
        LocalBroadcastManager.getInstance(PushService.this).sendBroadcast(intent);
        Log.d("DEBUG", "Sending boradcast!");

        if (!globalState.newMessages.contains(message.getUserSender().getId())){
          globalState.newMessages.add(message.getUserSender().getId());
        }

        sendPushNotification(globalState.getApplicationContext(),message.getUserSender().getName()+": "+ MadSecurity.decrypt(message.getContent()),msg.toString());
        globalState.save_new_msgs();

      }
      else{
        toastShow(content);
      }
    }
  };


  
  private void sendPushNotification(Context context, String content, String json_msg){

    Intent mIntent = new Intent(context, e_MessagesActivity.class);
    mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    //with this, MessagesActivity gets with whom I'm talking to on entering on that screen:
    mIntent.putExtra("message", json_msg);
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_CANCEL_CURRENT);

    Notification.Builder mBuilder = new Notification.Builder(context)
      .setContentTitle("Hurray, new message!")
      .setContentText(content)
      .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.signal))
      .setContentIntent(pendingIntent)
      .setContentInfo("Info")
      .setSmallIcon(R.drawable.signal)
      .setAutoCancel(true);
    
    Notification notification = mBuilder.build();
    notification.defaults |= Notification.DEFAULT_SOUND;
    
    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    nm.notify(2, notification);
  }
  private void sendPushInfoNotification(Context context, String content){

    Intent mIntent = new Intent(context, a_WelcomeActivity.class);
    mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_CANCEL_CURRENT);

    Notification.Builder mBuilder = new Notification.Builder(context)
            .setContentTitle("Push service working!")
            .setContentText("Tap to get started")
            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.signal))
            .setContentIntent(pendingIntent)

            .setContentInfo("aldrimer")
            .setSmallIcon(R.drawable.signal)
            .setAutoCancel(true);

    Notification notification = mBuilder.build();
    notification.defaults |= Notification.DEFAULT_SOUND;

    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    nm.notify(3, notification);
  }
  private  void closeAllNotifications(){
    NotificationManager notifManager= (NotificationManager) this.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    notifManager.cancelAll();
  }

  private void toastShow(String text) {
    Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
    toast.setGravity(0, 0, 200);
    toast.setDuration(Toast.LENGTH_SHORT);
    toast.show();
  }
}
