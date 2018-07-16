package com.laguna.university.homeappliancecontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by W10 on 03/11/2017.
 */

public class ReceiverCall extends BroadcastReceiver {
    boolean isHaveNotification = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        if(isHaveNotification == false) {
            isHaveNotification = true;
            context.startService(new Intent(context, BackgroundService.class));
        }
    }
}
