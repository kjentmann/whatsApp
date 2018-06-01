package edu.upc.whatsapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class a_WelcomeActivity extends Activity implements View.OnClickListener {
  _GlobalState globalState;
  Integer loggedIn;


  @Override
  public void onCreate(Bundle icicle) {
    loggedIn=0;
    super.onCreate(icicle);
    setContentView(R.layout.a_welcome);
    globalState= (_GlobalState)getApplication();
    setTitle("Welcome page. You are not logged in");


    ((Button) findViewById(R.id.welcomeLoginButton)).setOnClickListener(this);
    ((Button) findViewById(R.id.welcomeRegisterButton)).setOnClickListener(this);

    if (globalState.my_user!=null){
      loggedIn=1;
      startActivity(new Intent(this, d_UsersListActivity.class));
      globalState.pushStart();

    }
  }

  @Override
  public void onResume(){
    super.onResume();
    if (globalState.my_user!=null){
      loggedIn = 1;
      setTitle("Welcome page. Logged in as " + globalState.my_user.getName() + " " + globalState.my_user.getSurname());
      Button oldLogin = (Button)findViewById(R.id.welcomeLoginButton);
      Button oldRegister = (Button)findViewById(R.id.welcomeRegisterButton);

      oldLogin.setText("CONTINUE");
      oldRegister.setText("LOGOUT");

    }



  }

  public void onClick(View arg0) {
    switch (loggedIn){
      case 0:
      if (arg0 == findViewById(R.id.welcomeLoginButton)) {
        startActivity(new Intent(this, b_LoginActivity.class));
      }
      if (arg0 == findViewById(R.id.welcomeRegisterButton)) {
        startActivity(new Intent(this, c_RegistrationActivity.class));
      }
      break;
      case 1:
        if (arg0 == findViewById(R.id.welcomeLoginButton)) {
          startActivity(new Intent(this, d_UsersListActivity.class));
        }
        if (arg0 == findViewById(R.id.welcomeRegisterButton)) {
          loggedIn=0;
          globalState.logOut();
          startActivity(new Intent(this, a_WelcomeActivity.class));
          }
      break;
    }
  }
}
