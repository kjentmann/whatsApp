package edu.upc.whatsapp;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Date;
import java.util.List;
import java.util.Timer;

import edu.upc.whatsapp.adapter.MyAdapter_messages;
import edu.upc.whatsapp.comms.RPC;
import entity.Message;

public class e_MessagesActivity extends Activity {

  _GlobalState globalState;
  ProgressDialog progressDialog;
  private ListView conversation;
  private MyAdapter_messages adapter;
  private EditText input_text;
  private Button button;
  private boolean enlarged = false, shrunk = true;

  private Timer timer;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.e_messages);
    globalState = (_GlobalState) getApplication();
    globalState.load_my_user();
    if (globalState.user_to_talk_to==null){ // In case of app has been closed
      globalState.load_user_to_talk_to();
    }
    TextView title = (TextView) findViewById(R.id.title);
    title.setText(globalState.user_to_talk_to.getName() + " " + globalState.user_to_talk_to.getSurname());
    conversation = (ListView) findViewById(R.id.conversation);
    conversation.setAdapter(adapter);
    setTitle("Private conversation. Logged in as " + globalState.my_user.getName()+" "+globalState.my_user.getSurname()+"");
    setup_input_text();
    Log.d("DEBUG","I am user ID  :"+globalState.my_user.getId() +"Want to talk to user id : "+ globalState.user_to_talk_to.getId());
    new fetchAllMessages_Task().execute(globalState.my_user.getId(), globalState.user_to_talk_to.getId());
  }

  @Override
  protected void onResume() {

    new fetchNewMessages_Task().execute();
    LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("localBroadcastMessage"));
    super.onResume();
    globalState.MessagesActivity_visible=true;
    if (!globalState.isPushRunning()) globalState.pushStart();
    Log.d("DEBUG","On Resume");
    //...
  }

  @Override
  protected void onPause() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver); // Not needed on pause -> Push will do the work
    globalState.MessagesActivity_visible=false;
    super.onPause();
    Log.d("DEBUG","On Pause");

  }

  private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      Gson gson = new Gson();
      String msg = intent.getStringExtra("messageContent");
      Message message =  gson.fromJson(msg,Message.class);
      if(message.getUserSender().getId()==adapter.getPartnerId()){
        adapter.addMessage(message);
        adapter.notifyDataSetChanged();
        globalState.newMessages.remove(adapter.getPartnerId());
        globalState.save_new_msgs();
        NotificationManager notifManager= (NotificationManager) globalState.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();
      }

      Log.d("DEBUG", "Got broadcast msg. Sender: " + message.getUserSender().getId()+ "  talkto: "+ globalState.user_to_talk_to.getId() + "current Parnter " + adapter.getPartnerId() );
    }
  };


  private class fetchAllMessages_Task extends AsyncTask<Integer, Void, List<Message>> {

    @Override
    protected void onPreExecute() {
      progressDialog = ProgressDialog.show(e_MessagesActivity.this,
          "MessagesActivity", "downloading messages...");
    }

    @Override
    protected List<Message> doInBackground(Integer... userIds) {

      List<Message> all_messages;
      try {
        if (globalState.isThere_messages()) {
          all_messages = globalState.load_messages();
          all_messages.addAll(RPC.retrieveNewMessages(globalState.user_to_talk_to.getId(), globalState.my_user.getId(), all_messages.get(all_messages.size() - 1)));

        } else {
          all_messages = RPC.retrieveMessages(globalState.user_to_talk_to.getId(), globalState.my_user.getId());
          globalState.save_new_messages(all_messages);
        }
      }
      catch (Exception e){
        all_messages = globalState.load_messages();
      }

      Log.d("DEBUG", "Returning messageslist: " + all_messages);
      return all_messages;
    }

    @Override
    protected void onPostExecute(List<Message> all_messages) {
      progressDialog.dismiss();
      if (all_messages == null) {
        toastShow("There's been an error downloading the messages");
      } else {
       adapter = new MyAdapter_messages(e_MessagesActivity.this,all_messages, globalState.my_user,globalState.user_to_talk_to.getId());
       conversation.setAdapter(adapter);
        toastShow(all_messages.size()+" messages loaded/downloaded.");
      }
    }
  }



  private class fetchNewMessages_Task extends AsyncTask<Integer, Void, List<Message>> {

    @Override
    protected List<Message> doInBackground(Integer... userIds) {
      // To avoid trouble with the very first message in a conversation (last message don't exist yet).
      if(adapter !=null && adapter.getLastMessage()==null)
        return RPC.retrieveMessages(globalState.user_to_talk_to.getId(), globalState.my_user.getId());
      else if (adapter != null)
        return RPC.retrieveNewMessages(globalState.user_to_talk_to.getId(),globalState.my_user.getId(), adapter.getLastMessage());
      return null;
    }


    @Override
    protected void onPostExecute(List<Message> new_messages) {
      if (new_messages == null) {
        //toastShow("There's been an error downloading new messages");
        Log.d("DEBUG","There's been an error downloading new messages");

      } else if (new_messages.size()>0){
        Log.d("DEBUG", "Fetched "+new_messages.size() + " new msgs. Sending to adapter");
        adapter.addMessages(new_messages);
        adapter.notifyDataSetChanged();
        globalState.save_new_messages(new_messages);
      }
    }
  }

  public void sendText(final View view)  {

    String content = input_text.getText().toString();
    String encrypted = MadSecurity.encrypt(content);
    content=encrypted.toString();


    Date date = new Date();
    Message msg = new Message();
    msg.setContent(content);
    msg.setUserReceiver(globalState.user_to_talk_to);
    msg.setUserSender(globalState.my_user);
    msg.setDate(date);
    new SendMessage_Task().execute(msg);

    input_text.setText("");

    //to hide the soft keyboard after sending the message:
    InputMethodManager inMgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    inMgr.hideSoftInputFromWindow(input_text.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
  }
  private class SendMessage_Task extends AsyncTask<Message, Void, Boolean> {

    @Override
    protected void onPreExecute() {
      toastShow("sending message");
    }

    @Override
    protected Boolean doInBackground(Message... messages) {

      return RPC.postMessage(messages[0]);
    }      //

    @Override
    protected void onPostExecute(Boolean resultOk) {
      if (resultOk) {
        new fetchNewMessages_Task().execute();
        toastShow("message sent");

      } else {
        toastShow("There has been an network error while sending the message.");

      }
    }
  }

  private void setup_input_text(){

    input_text = (EditText) findViewById(R.id.input);
    button = (Button) findViewById(R.id.mybutton);
    button.setEnabled(false);

    //to be notified when the content of the input_text is modified:
    input_text.addTextChangedListener(new TextWatcher() {

      public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
      }

      public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
      }

      public void afterTextChanged(Editable arg0) {
        if (arg0.toString().equals("")) {
          button.setEnabled(false);
        } else {
          button.setEnabled(true);
        }
      }
    });
    //to program the send soft key of the soft keyboard:
    input_text.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_SEND) {
          sendText(null);
          handled = true;
        }
        return handled;
      }
    });
    //to detect a change on the height of the window on the screen:
    input_text.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        int screenHeight = input_text.getRootView().getHeight();
        Rect r = new Rect();
        input_text.getWindowVisibleDisplayFrame(r);
        int visibleHeight = r.bottom - r.top;
        int heightDifference = screenHeight - visibleHeight;
        if (heightDifference > 50 && !enlarged) {
          LayoutParams layoutparams = input_text.getLayoutParams();
          layoutparams.height = layoutparams.height * 2;
          input_text.setLayoutParams(layoutparams);
          enlarged = true;
          shrunk = false;
          conversation.post(new Runnable() {
            @Override
            public void run() {
              conversation.setSelection(conversation.getCount() - 1);
            }
          });
        }
        if (heightDifference < 50 && !shrunk) {
          LayoutParams layoutparams = input_text.getLayoutParams();
          layoutparams.height = layoutparams.height / 2;
          input_text.setLayoutParams(layoutparams);
          shrunk = true;
          enlarged = false;
        }
      }
    });
  }

  private void toastShow(String text) {
    Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
    toast.setGravity(0, 0, 200);
    toast.show();
  }
}
