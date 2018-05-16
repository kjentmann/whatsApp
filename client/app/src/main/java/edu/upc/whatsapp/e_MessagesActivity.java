package edu.upc.whatsapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import edu.upc.whatsapp.comms.RPC;
import edu.upc.whatsapp.adapter.MyAdapter_messages;
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
    TextView title = (TextView) findViewById(R.id.title);
    title.setText("Conversation with " + globalState.user_to_talk_to.getName());
    conversation = (ListView) findViewById(R.id.conversation);
    conversation.setAdapter(adapter);
    setup_input_text();

    timer = new Timer(true);
    Log.d("DEBUG","I am user ID  :"+globalState.my_user.getId() +"Want to talk to user id : "+ globalState.user_to_talk_to.getId());
    new fetchAllMessages_Task().execute(globalState.my_user.getId(), globalState.user_to_talk_to.getId());

  }

  @Override
  protected void onResume() {
    super.onResume();
    globalState.MessagesActivity_visible=true;
    //...

  }

  @Override
  protected void onPause() {
    globalState.MessagesActivity_visible=false;
    super.onPause();

    //...

  }

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
        Log.d("DEBUG", "Network error while trying to download new messages");
        all_messages = globalState.load_messages();
      }
      /*
      if(msgList.isEmpty()){
        String content ="Hi "+globalState.my_user.getName()+"! Nice to see you using Mads' app! "+ globalState.user_to_talk_to.getName();
        Date date = new Date();
        Message msg = new Message();
        msg.setContent(content);
        msg.setUserReceiver(globalState.my_user);
        msg.setUserSender(globalState.my_user);
        msg.setDate(date);
        new SendMessage_Task().execute(msg);
      }
*/
      Log.d("DEBUG", "Returning messageslist: " + all_messages);
      return all_messages;
    }

    @Override
    protected void onPostExecute(List<Message> all_messages) {
      progressDialog.dismiss();
      if (all_messages == null) {
        toastShow("There's been an error downloading the messages");
      } else {
       adapter = new MyAdapter_messages(e_MessagesActivity.this,all_messages, globalState.my_user);
       conversation.setAdapter(adapter);
        timer.scheduleAtFixedRate(new fetchNewMessagesTimerTask(),0,5 * 1000);
        Log.d("DEBUG","Timer task activated to fetch new messages..");
        toastShow(all_messages.size()+" messages loaded/downloaded.");
      }
    }
  }

  private class fetchNewMessages_Task extends AsyncTask<Integer, Void, List<Message>> {

    @Override
    protected List<Message> doInBackground(Integer... userIds) {
            if(adapter.getLastMessage()!=null) // To aviod trouble with the very first message in a conversation (last message dont exist yet).
              return  RPC.retrieveNewMessages(globalState.user_to_talk_to.getId(), globalState.my_user.getId(), adapter.getLastMessage());
            else
              return RPC.retrieveMessages(globalState.user_to_talk_to.getId(), globalState.my_user.getId());
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
        //...
      }

    }
  }

  public void sendText(final View view) {

    //...
    String content = input_text.getText().toString();
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
     /// if (adapter.getLastMessage()==null){
       // adapter.addMessage(messages[0]);
      //}
      return RPC.postMessage(messages[0]);
    }      //

    @Override
    protected void onPostExecute(Boolean resultOk) {
      if (resultOk) {
//        adapter.addMessage(messages[0]); FIX: Read sendt message form db instead. less dirty
      //  adapter.notifyDataSetChanged();

        new fetchNewMessages_Task().execute();
        toastShow("message sent");

        //...
      } else {
        toastShow("There has been an network error while sending the message.");

      }
    }
  }

  private class fetchNewMessagesTimerTask extends TimerTask {

    @Override
    public void run() {
        new fetchNewMessages_Task().execute();
      //...

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
  //  toast.setDuration(Toast.LENGTH_SHORT);
    toast.show();
  }
}
