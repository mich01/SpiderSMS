package com.mich01.spidersms.Receivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mich01.spidersms.Backend.BackendFunctions;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(BackendFunctions.isConnectedOnline(context))
        {
            new BackendFunctions().UpdateContactPrivateKeys(context);
        }
    }
}