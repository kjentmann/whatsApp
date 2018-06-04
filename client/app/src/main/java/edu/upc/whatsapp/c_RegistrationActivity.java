package edu.upc.whatsapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import edu.upc.whatsapp.comms.RPC;
import entity.User;
import entity.UserInfo;

public class c_RegistrationActivity extends Activity implements View.OnClickListener {

  _GlobalState globalState;
  ProgressDialog progressDialog;
  User user;
  OperationPerformer operationPerformer;

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    globalState = (_GlobalState)getApplication();
    setContentView(R.layout.c_registration);
    ((Button) findViewById(R.id.editregistrationButton)).setOnClickListener(this);

  }

  private String parameterCheck(User user){
      if (user.getLogin().length()<3)
          return "ERROR: 'User' must be more than 5 characters.";
      else if (user.getPassword().length()<6)
          return "ERROR: 'Password' must be more than 3 characters.";
      else if (!user.getEmail().contains("@") || !user.getEmail().contains(".")|| user.getEmail().length()<7)
          return "ERROR: 'Email format not accepted.";
      else if (user.getUserInfo().getName().length()<2)
        return "ERROR: 'Name' format not accepted";
      else if (user.getUserInfo().getSurname().length()<2)
          return "ERROR: 'Surname' format not accepted";
      return "";
  }


  public void onClick(View arg0) {
    if (arg0 == findViewById(R.id.editregistrationButton)) {

      EditText login_input      = (EditText)findViewById(R.id.username_input);
      EditText password_input   = (EditText)findViewById(R.id.password_input);
      EditText name_input       = (EditText)findViewById(R.id.name_input);
      EditText surname_input    = (EditText)findViewById(R.id.surname_input);
      EditText email_input      = (EditText)findViewById(R.id.email_input);



      user = new User();
      UserInfo usrInfo = new UserInfo();
      usrInfo.setName(name_input.getText().toString());
      usrInfo.setSurname(surname_input.getText().toString());
      user.setLogin(login_input.getText().toString());
      user.setPassword(MadSecurity.encrypt(password_input.getText().toString())); //encrypt?
      user.setId(usrInfo.getId());                           //hash?
      user.setEmail(email_input.getText().toString());
      user.setUserInfo(usrInfo);

      String paramTest = parameterCheck(user);
      if (paramTest!=""){
          toastShow(paramTest);
         // user=null;
         // return; //FIX enable to increase security
        }

      progressDialog = ProgressDialog.show(this, "RegistrationActivity", "Registering for service...");
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
     // UserInfo registrationCandidate = new UserInfo();
      UserInfo registrationCandidate = RPC.registration(user);
      b.putSerializable("userInfo",registrationCandidate);

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
        toastShow("Registration successful");


        finish();
      }
      else if (userInfo.getId() == -1) {
        toastShow("Registration unsuccessful,\nlogin already used by another user");
      }
      else if (userInfo.getId() == -2) {
        toastShow("Not registered, connection problem due to: " + userInfo.getName());
        System.out.println("--------------------------------------------------");
        System.out.println("error!!!");
        System.out.println(userInfo.getName());
        System.out.println("--------------------------------------------------");
      }
    }
  };

  private void toastShow(String text) {
    Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
    toast.setGravity(0, 0, 200);
    toast.show();
  }
}
