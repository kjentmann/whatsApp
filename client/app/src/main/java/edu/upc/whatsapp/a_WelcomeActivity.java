package edu.upc.whatsapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class a_WelcomeActivity extends Activity implements View.OnClickListener {

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    _GlobalState globalState = (_GlobalState)getApplication();
    setContentView(R.layout.a_welcome);

    ((Button) findViewById(R.id.welcomeLoginButton)).setOnClickListener(this);
    ((Button) findViewById(R.id.welcomeRegisterButton)).setOnClickListener(this);

    if (globalState.my_user!=null){
      startActivity(new Intent(this, d_UsersListActivity.class));
      globalState.pushStart();

    }
  }

  public void onClick(View arg0) {

    if (arg0 == findViewById(R.id.welcomeLoginButton)) {
      startActivity(new Intent(this, b_LoginActivity.class));
    }
    if (arg0 == findViewById(R.id.welcomeRegisterButton)) {
      startActivity(new Intent(this, c_RegistrationActivity.class));
    }
  }

}
