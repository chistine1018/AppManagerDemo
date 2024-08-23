package com.asn.appmanagerdemo.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.asn.appmanagerdemo.ui.MainActivity;


public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //接收卸載廣播
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            //Activity activity= (Activity) context;
            //activity.finish();
//            finish();
            MainActivity.Input.publishSubject1.onNext(true);
            context.startActivity(new Intent(context, MainActivity.class));

        }
    }
}
