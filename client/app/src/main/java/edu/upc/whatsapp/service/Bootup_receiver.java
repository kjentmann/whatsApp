package edu.upc.whatsapp.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Bootup_receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent myIntent = new Intent(context, PushService.class);
        context.startService(myIntent);

    }
}
