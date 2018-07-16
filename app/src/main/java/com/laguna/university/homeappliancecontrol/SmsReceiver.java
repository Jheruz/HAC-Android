package com.laguna.university.homeappliancecontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

/**
 * Created by W10 on 10/01/2018.
 */

public class SmsReceiver extends BroadcastReceiver {
    boolean isHaveNotification = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        SQLiteDBcontroller db = new SQLiteDBcontroller(context);
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get("pdus");
            for (int i = 0; i < sms.length; ++i){
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);
                String phone = smsMessage.getOriginatingAddress();
                String message = smsMessage.getMessageBody().toString();
                phone = "0"+phone.substring(3, phone.length());
                db.getNumber();
                if(db.numberList.size() > 0){
                    if(phone.equals(db.numberList.get(0))){
                        boolean isAppliancesSwitch = false;
                        db.getSetting();
                        if(message.contains("Outlet 1 is now turned on")){
                            isAppliancesSwitch = true;
                            if(db.outlet1.size() > 0 && db.outlet1.get(0) != 1){
                                db.updateSetting("outlet1", 1);
                                if(MainActivity.isOpen){
                                    showMessageOnApp(db, "outlet1", "on");
                                } else {
                                    showToast(context, db, "outlet1", "on");
                                }
                            }
                        } else if(message.contains("Outlet 1 is now turned off")){
                            isAppliancesSwitch = true;
                            if(db.outlet1.size() > 0 && db.outlet1.get(0) != 0){
                                db.updateSetting("outlet1", 0);
                                if(MainActivity.isOpen){
                                    showMessageOnApp(db, "outlet1", "off");
                                } else {
                                    showToast(context, db, "outlet1", "off");
                                }
                            }
                        }
                        if(message.contains("Outlet 2 is now turned on")){
                            isAppliancesSwitch = true;
                            if(db.outlet2.size() > 0 && db.outlet2.get(0) != 1){
                                db.updateSetting("outlet2", 1);
                                if(MainActivity.isOpen){
                                    showMessageOnApp(db, "outlet2", "on");
                                } else {
                                    showToast(context, db, "outlet2", "on");
                                }
                            }
                        } else if(message.contains("Outlet 2 is now turned off")){
                            isAppliancesSwitch = true;
                            if(db.outlet2.size() > 0 && db.outlet2.get(0) != 0){
                                db.updateSetting("outlet2", 0);
                                if(MainActivity.isOpen){
                                    showMessageOnApp(db, "outlet2", "off");
                                } else {
                                    showToast(context, db, "outlet2", "off");
                                }
                            }
                        }

                        if(isAppliancesSwitch && MainActivity.isOpen){
                            MainActivity.isRunning = true;
                        }
                    } else {
                        //kapag ibang number nag text handle here
                    }
                } else {
                    //kapag wla pang nka save na arduino number handle here
                }
                deleteSMS(context, phone);
            }
        }

        if(isHaveNotification == false) {
            isHaveNotification = true;
            context.startService(new Intent(context, BackgroundService.class));
        }
    }

    public void showToast(Context context, SQLiteDBcontroller db, String device, String onOff){
        db.getDeviceName(device);
        if(db.dn.size() > 0){
            Toast.makeText(context,db.dn.get(0)+ " has been turned "+onOff, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, device.toUpperCase()+" has been turned "+onOff, Toast.LENGTH_SHORT).show();
        }
    }

    public void showMessageOnApp(SQLiteDBcontroller db, String device, String onOff){
        db.getDeviceName(device);
        if(db.dn.size() > 0){
            MainActivity.appliancesName += db.dn.get(0)+" has been turned "+onOff+"\n";
        } else {
            MainActivity.appliancesName += device.toUpperCase()+" has been turned "+onOff+"\n";
        }
    }

    public void deleteSMS(Context ctx, String number) {
        try {
            Uri uriSms = Uri.parse("content://sms/sent");
            Cursor c = ctx.getContentResolver().query(uriSms, null, null, null, null);
            if (c != null && c.moveToFirst()) {
                do {
                    long threadId = c.getLong(1);
                    ctx.getContentResolver().delete(Uri.parse("content://sms/conversations/" + threadId), "address=?", new String[]{number});
                } while (c.moveToNext());
            }
            c.close();
        }catch (Exception e) {
            System.out.println("Exception:: "+e);
        }
    }
}