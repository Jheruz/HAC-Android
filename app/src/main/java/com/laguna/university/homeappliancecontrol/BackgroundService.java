package com.laguna.university.homeappliancecontrol;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

/**
 * Created by W10 on 27/10/2017.
 */

public class BackgroundService extends Service {

    SQLiteDBcontroller db = new SQLiteDBcontroller(this);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notifyThis();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
    }

    public void notifyThis() {
        db.getSetting();
        int arr[] = {db.light1.get(0), db.light2.get(0), db.light3.get(0), db.light4.get(0), db.light5.get(0),
                db.outlet1.get(0), db.outlet2.get(0)};
        int appOn = 0;
        for(int i=0;i<arr.length;i++){
            if(arr[i] == 1){
                appOn += 1;
            }
        }
        NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if(appOn != 0) {
            NotificationCompat.Builder notif = new NotificationCompat.Builder(this);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Login.class), PendingIntent.FLAG_UPDATE_CURRENT);

            notif.setAutoCancel(false)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker("You still have " + appOn + " appliances still on")
                    .setContentTitle(appOn + " Appliances still open")
                    .setContentText("Click to open the application")
                    .setContentInfo(String.valueOf(appOn))
                    .setContentIntent(contentIntent)
                    .setOngoing(true);

            nm.notify(1, notif.build());
        } else {
            nm.cancelAll();
        }
    }
}