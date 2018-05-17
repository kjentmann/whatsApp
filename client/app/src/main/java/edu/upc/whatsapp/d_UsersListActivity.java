package edu.upc.whatsapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.upc.whatsapp.comms.RPC;
import edu.upc.whatsapp.adapter.MyAdapter_users;
import edu.upc.whatsapp.service.PushService;
import entity.UserInfo;
import java.util.List;

public class d_UsersListActivity extends Activity implements ListView.OnItemClickListener, View.OnClickListener {

  _GlobalState globalState;
  MyAdapter_users adapter;
  ProgressDialog progressDialog;


    @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    globalState = (_GlobalState) getApplication();
    setContentView(R.layout.d_userslist);
    globalState.load_my_user();
    new DownloadUsers_Task().execute();
    setTitle("Users list. Logged in as " + globalState.my_user.getName() + " "+globalState.my_user.getSurname());

    ((Button) findViewById(R.id.logOutButton)).setOnClickListener(this);


    }
    public void onClick(View arg0) {
        globalState.remove_my_user();
        //globalState.remove_messages();
        globalState.pushStop();
        globalState.my_user=null;
        //unbindService(new Intent(this,PushService.class));
        stopService(new Intent(this,PushService.class));
        finish();
    }

  @Override
  public void onItemClick(AdapterView<?> l, View v, int position, long id) {
      Log.d("DEBUG","clicked id : "+id+"pos: "+position);
      UserInfo clickedUser = (UserInfo) adapter.getItem(position);
      globalState.user_to_talk_to = clickedUser;
      Log.d("DEBUG","I am  :"+globalState.my_user.getName() +". And I want to talk to  : "+ globalState.user_to_talk_to.getName());
      startActivity(new Intent(this, e_MessagesActivity.class));
      //finish();

  }

  private class DownloadUsers_Task extends AsyncTask<Void, Void, List<UserInfo>> {

    @Override
    protected void onPreExecute() {
      progressDialog = ProgressDialog.show(d_UsersListActivity.this, "UsersListActivity",
        "downloading the users...");
    }

    @Override
    protected List<UserInfo> doInBackground(Void... nothing) {

     return RPC.allUserInfos();
      //...
    }

    @Override
    protected void onPostExecute(List<UserInfo> users) {
      progressDialog.dismiss();
      if (users == null) {
        toastShow("There's been an error downloading the users");
      } else {
          Log.d("DEBUG","Downloaded users. They exist!" );
          ListView listView =  findViewById(R.id.listView);
          adapter = new MyAdapter_users(d_UsersListActivity.this,users);
          listView.setAdapter(adapter);
          listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
          listView.setOnItemClickListener(d_UsersListActivity.this);
          Log.d("DEBUG","On click listener ready!");
          //...
      }
    }
  }

  private void toastShow(String text) {
    Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
    toast.setGravity(0, 0, 200);
    toast.show();
  }
}
