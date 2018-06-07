package edu.upc.whatsapp;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import edu.upc.whatsapp.service.PushService;
import entity.Message;
import entity.UserInfo;

import static edu.upc.whatsapp.comms.Comms.gson;

public class _GlobalState extends Application {

  public UserInfo my_user, user_to_talk_to;
  public boolean MessagesActivity_visible;
  public List<Integer> newMessages;


  @Override
  public void onCreate() {
    super.onCreate();
    newMessages = new ArrayList<Integer>();
    load_new_msgs();

    if(isThere_my_user()){
      load_my_user();
    }
  }

  public void logOut(){
    remove_my_user();
    remove_user_to_talk_to();
   // remove_messages();
    pushStop();
    my_user=null;
    toastShow("Logged out");
  }

  public void pushStart(){
    if (!isPushRunning()) {
      startService(new Intent(this, PushService.class));

    }
    }
  public void pushStop(){
    stopService((new Intent(this, PushService.class)));
    //stopService((new Intent(this, PushService.MyEndPoint.class)));

  }

  public boolean isPushRunning() {
    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (PushService.class.getName().equals(service.service.getClassName())) {
        return true;
      }
    }
    return false;
  }


  public void load_my_user(){
    try{
      FileInputStream fis = openFileInput("my_user");
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      my_user = gson.fromJson(br, UserInfo.class);

      br.close();
      fis.close();
    }
    catch(Exception e){
      //toastShow("Exception when loading whatsapp-user");
      Log.d("DEBUG","Exception when loading whatsapp-user");
     // e.printStackTrace();
    }
  }
  public void save_my_user(){
    try {
      FileOutputStream fos = openFileOutput("my_user", MODE_PRIVATE);
      PrintWriter pw = new PrintWriter(fos);

      pw.println(gson.toJson(my_user));

      pw.flush();
      pw.close();
      fos.close();
    } catch (Exception e) {
      toastShow("Exception at save_my_user");
      e.printStackTrace();
    }
  }

  public void load_new_msgs(){
    try{
      FileInputStream fis = openFileInput("new_messages");
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));
      newMessages =  gson.fromJson(br,  new TypeToken<ArrayList<Integer>>(){}.getType());
      br.close();
      fis.close();
      Log.d("DEBUG","Loaded new msgs" +  newMessages);

    }
    catch(Exception e){
      Log.d("DEBUG","Exception when loading newmsgsr");
    }
  }
  public void save_new_msgs(){
    try {
      FileOutputStream fos = openFileOutput("new_messages", MODE_PRIVATE);
      PrintWriter pw = new PrintWriter(fos);

      pw.println(gson.toJson(newMessages));
      pw.flush();
      pw.close();
      fos.close();
      Log.d("DEBUG","saved new msgs" +  newMessages);

    } catch (Exception e) {
      toastShow("Exception at save_ new msgs");
      e.printStackTrace();
    }
  }


  public void load_user_to_talk_to(){
    try{
      FileInputStream fis = openFileInput("user_to_talk_to");
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      user_to_talk_to = gson.fromJson(br, UserInfo.class);

      br.close();
      fis.close();
    }
    catch(Exception e){
      //toastShow("Exception at load_my_user");
      e.printStackTrace();
    }
  }
  public void save_user_to_talk_to(){
    try {
      FileOutputStream fos = openFileOutput("user_to_talk_to", MODE_PRIVATE);
      PrintWriter pw = new PrintWriter(fos);

      pw.println(gson.toJson(user_to_talk_to));

      pw.flush();
      pw.close();
      fos.close();
    } catch (Exception e) {
      toastShow("Exception at save_my_user");
      e.printStackTrace();
    }
  }
  public void remove_user_to_talk_to(){
    try{
      deleteFile("user_to_talk_to");
    }
    catch(Exception e){
      toastShow("Exception at remove_my_user");
      e.printStackTrace();
    }
  }
  public void remove_my_user(){
    try{
      deleteFile("my_user");
    }
    catch(Exception e){
      toastShow("Exception at remove_my_user");
      e.printStackTrace();
    }
  }
  public boolean isThere_my_user(){
    try{
      FileInputStream fis = openFileInput("my_user");
      fis.close();
      return true;
    }
    catch(Exception e){
      return false;
    }
  }

  public List<Message> load_messages(){
    try{
      FileInputStream fis = openFileInput("messages_"+my_user.getId()+"_"+user_to_talk_to.getId());
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));

      List<Message> messages = new ArrayList<>();
      String json_message;
      while ((json_message = br.readLine()) != null) {
        messages.add(gson.fromJson(json_message, Message.class));
      }

      br.close();
      fis.close();
      return messages;
    }
    catch(Exception e){
      Log.d("DEBUG","Exception at load_messages");
      e.printStackTrace();
    }
    return null;
  }
  public void save_new_messages(List<Message> messages){
    try {
      FileOutputStream fos = openFileOutput("messages_"+my_user.getId()+"_"+
          user_to_talk_to.getId(), MODE_APPEND);
      PrintWriter pw = new PrintWriter(fos);

      for (Message message : messages) {
        String json_message = gson.toJson(message);
        pw.println(json_message);
        pw.flush();
      }

      pw.close();
      fos.close();
    } catch (Exception e) {
      toastShow("Exception at save_new_messages");
      e.printStackTrace();
    }
  }
  public void save_new_message(Message message){
    try {
      FileOutputStream fos = openFileOutput("messages_"+my_user.getId()+"_"+
          user_to_talk_to.getId(), MODE_APPEND);
      PrintWriter pw = new PrintWriter(fos);

      String json_message = gson.toJson(message);
      pw.println(json_message);
      pw.flush();

      pw.close();
      fos.close();
    } catch (Exception e) {
      toastShow("Exception at save_new_message");
      e.printStackTrace();
    }
  }
  public void remove_messages(){
    try{
      deleteFile("messages_"+my_user.getId()+"_"+user_to_talk_to.getId());
    }
    catch(Exception e){
      toastShow("Exception at remove_messages");
      e.printStackTrace();
    }
  }
  public boolean isThere_messages(){
    try{
      FileInputStream fis = openFileInput("messages_"+my_user.getId()+"_"+user_to_talk_to.getId());
      fis.close();
      return true;
    }
    catch(Exception e){
      return false;
    }
  }

  private void toastShow(String text) {
    Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
    toast.setGravity(0, 0, 200);
    toast.show();
  }

}
