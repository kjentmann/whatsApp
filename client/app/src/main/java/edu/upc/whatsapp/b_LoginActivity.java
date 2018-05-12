package edu.upc.whatsapp;

import edu.upc.whatsapp.comms.RPC;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.Serializable;

import edu.upc.whatsapp.service.PushService;
import entity.User;
import entity.UserInfo;

public class b_LoginActivity extends Activity implements View.OnClickListener {

  _GlobalState globalState;
  ProgressDialog progressDialog;
  User user;
  OperationPerformer operationPerformer;


  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    globalState = (_GlobalState)getApplication();
    setContentView(R.layout.b_login);
    //WindowManager.LayoutParams params = getWindow().getAttributes();
    //params.x = -100;
    //params.height = 300;
    //params.width = 1000;
    //params.y = -50;

    //this.getWindow().setAttributes(params);
    ((Button) findViewById(R.id.editloginButton)).setOnClickListener(this);


  }

  public void onClick(View arg0) {
    if (arg0 == findViewById(R.id.editloginButton)) {
        EditText login_input   = (EditText)findViewById(R.id.login_input);
        EditText password_input   = (EditText)findViewById(R.id.password_input);
        user = new User();
        user.setLogin(login_input.getText().toString());
        user.setPassword(password_input.getText().toString());
              //...
        progressDialog = ProgressDialog.show(this, "LoginActivity", "Logging into the server...");
        // if there's still a running thread doing something, we don't create a new one
        if (operationPerformer == null) {
          operationPerformer = new OperationPerformer();
          operationPerformer.start();
        }
    }
  }

  private class OperationPerformer extends Thread {

    @Override
    public void run() {
      Message msg = handler.obtainMessage();
      Bundle b = new Bundle();
      UserInfo userRequest = new UserInfo();
      userRequest = RPC.login(user);
      b.putSerializable("userInfo",userRequest);
      //...
      msg.setData(b);
      handler.sendMessage(msg);
    }
  }

  Handler handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {

      operationPerformer = null;
      progressDialog.dismiss();

      UserInfo userInfo = (UserInfo) msg.getData().getSerializable("userInfo");

      if (userInfo.getId() >= 0) {

        globalState.my_user=userInfo;
        globalState.save_my_user();

        toastShow("Login successful for "+globalState.my_user.getName());

        startActivity(new Intent(b_LoginActivity.this, d_UsersListActivity.class));


        finish();
      }
      else if (userInfo.getId() == -1){
        toastShow("Login unsuccessful, try again please.");
      }
      else if (userInfo.getId() == -2){
        toastShow("Not logged in, connection problem due to: " + userInfo.getName());
      }

    }
  };

  private void toastShow(String text) {
    Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
    toast.setGravity(0, 0, 200);
    toast.show();
  }
}
