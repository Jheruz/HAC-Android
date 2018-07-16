package com.laguna.university.homeappliancecontrol;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.telephony.gsm.SmsManager;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static android.Manifest.permission.RECEIVE_SMS;
import static android.Manifest.permission.SEND_SMS;

public class MainActivity extends AppCompatActivity {

    String nameDevice = "";
    AlertDialog dialogCancel;

    BootstrapButton useSMS, light1, light1Off, outlet1,  outlet1Off, outlet2, outlet2Off;
    ImageView imgLight1,imgOutlet1,imgOutlet2;
    ImageView lightEdit1,outletEdit1,outletEdit2;
    CardView addUser, appinfo, sync;
    SQLiteDBcontroller db = new SQLiteDBcontroller(this);
    TextView lName1,oName1,oName2;
    TextView timer,mode;

    String address = null;
    public static String appliancesName="";
    public static String isISend = "";

    String phoneNumber = "09090067944";
    String lightSmsOn1 = "LGHT1_ON";
    String outletSmsOn1 = "APP1_ON";
    String outletSmsOn2 = "APP2_ON";

    String lightSmsOff1 = "LGHT1_OFF";
    String outletSmsOff1 = "APP1_OFF";
    String outletSmsOff2 = "APP2_OFF";

    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false, isUseSMS = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    SmsManager smsManager;

    private PendingIntent pendingIntent;

    private static final int PERMISSION_SEND_SMS = 1;
    private static final int PERMISSION_RECEIVE_SMS = 2;

    public static boolean isOpen = false;
    public static boolean isRunning = false;
    public static boolean isShowing = false;
    public static boolean isProgressShowing = false;

    AlertDialog dialog = null;

    @Override
    public void onStart(){
        super.onStart();
        isOpen = true;
    }

    @Override
    public void onStop(){
        super.onStop();
        isOpen = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isOpen = true;
        super.onCreate(savedInstanceState);
        //full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        checkPermission();

        lName1 = (TextView) findViewById(R.id.lightName1);
        oName1 = (TextView) findViewById(R.id.outletName1);
        oName2 = (TextView) findViewById(R.id.outletName2);
        mode = (TextView) findViewById(R.id.mode);
        useSMS = (BootstrapButton) findViewById(R.id.useSMS);
        light1 = (BootstrapButton) findViewById(R.id.light1);
        light1Off = (BootstrapButton) findViewById(R.id.light1Off);
        outlet1 = (BootstrapButton) findViewById(R.id.outlet1);
        outlet1Off = (BootstrapButton) findViewById(R.id.outlet1Off);
        outlet2 = (BootstrapButton) findViewById(R.id.outlet2);
        outlet2Off = (BootstrapButton) findViewById(R.id.outlet2Off);
        addUser = (CardView) findViewById(R.id.adduser);
        appinfo = (CardView) findViewById(R.id.appinfo);
        sync = (CardView) findViewById(R.id.sync);
        imgLight1 = (ImageView) findViewById(R.id.lightImage1);
        imgOutlet1 = (ImageView) findViewById(R.id.outletImage1);
        imgOutlet2 = (ImageView) findViewById(R.id.outletImage2);
        lightEdit1 = (ImageView) findViewById(R.id.lightEdit1);
        outletEdit1 = (ImageView) findViewById(R.id.outletEdit1);
        outletEdit2 = (ImageView) findViewById(R.id.outletEdit2);

        smsManager = SmsManager.getDefault();

        Intent alarmIntent = new Intent(MainActivity.this, ReceiverCall.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);

        try{
            isUseSMS = getIntent().getExtras().getBoolean("useMode");
        } catch (Exception e){
            e.printStackTrace();
        }

        if(!isUseSMS) {
            mode.setText("Mode: Bluetooth");
            useSMS.setText("Use SMS Mode");

            Intent newint = getIntent();
            address = newint.getStringExtra(Login.EXTRA_ADDRESS);
            myBluetooth = BluetoothAdapter.getDefaultAdapter();
            new ConnectBT().execute();
        } else {
            mode.setText("Mode: SMS");
            sync.setVisibility(View.VISIBLE);
            useSMS.setVisibility(View.GONE);

            db.getNumber();
            if(db.numberList.size() > 0){
                phoneNumber = db.numberList.get(0);
            }
        }

        db.getSetting();
        if(db.light1.size() == 0){
            db.insertSetting(0,0,0,0,0,0,0);
        }

        updateButton();
        updateName();

        useSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YesNoOption("sms","Switch to SMS Mode?",null);
            }
        });

        light1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                msg("No Action");
            }
        });

        light1Off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                msg("No Action");
            }
        });

        outlet1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isUseSMS) {
                    switchAppliances(outletSmsOn1,"outlet1",1);
                    startService();
                    countdownStart();
                    updateButton();
                } else{
                    phoneNumber = db.numberList.get(0);
                    sendSMS("On", phoneNumber, outletSmsOn1);
                }
            }
        });

        outlet1Off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isUseSMS) {
                    switchAppliances(outletSmsOff1,"outlet1",0);
                    startService();
                    countdownStart();
                    updateButton();
                } else{
                    phoneNumber = db.numberList.get(0);
                    sendSMS("Off", phoneNumber, outletSmsOff1);
                }
            }
        });

        outlet2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isUseSMS) {
                    switchAppliances(outletSmsOn2,"outlet2",1);
                    startService();
                    countdownStart();
                    updateButton();
                } else{
                    phoneNumber = db.numberList.get(0);
                    sendSMS("On", phoneNumber, outletSmsOn2);
                }
            }
        });

        outlet2Off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isUseSMS) {
                    switchAppliances(outletSmsOff2,"outlet2",0);
                    startService();
                    countdownStart();
                    updateButton();
                } else{
                    phoneNumber = db.numberList.get(0);
                    sendSMS("Off", phoneNumber, outletSmsOff2);
                }
            }
        });

        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean showDialog = false;
                final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflate = MainActivity.this.getLayoutInflater();
                View newView = inflate.inflate(R.layout.user_data, null);
                final TextView gsmNumber = (TextView) newView.findViewById(R.id.gsmNum);
                final ImageView editNumber = (ImageView) newView.findViewById(R.id.editNum);
                final TextView username = (TextView) newView.findViewById(R.id.username);
                final TextView password = (TextView) newView.findViewById(R.id.password);
                final ImageView editAccount = (ImageView) newView.findViewById(R.id.editAccount);
                final EditText num = (EditText) newView.findViewById(R.id.num);
                final BootstrapButton save = (BootstrapButton) newView.findViewById(R.id.save);
                final BootstrapButton cancel = (BootstrapButton) newView.findViewById(R.id.cancel);
                final BootstrapButton delete = (BootstrapButton) newView.findViewById(R.id.delete);
                final ListView lv = (ListView) newView.findViewById(R.id.numList);
                final LinearLayout bluetoothView = (LinearLayout) newView.findViewById(R.id.inBluetooth);
                ArrayList<String> numList = new ArrayList<>();
                numList.clear();
                String word = "";

                db.getAccount();
                db.getNumber();
                if (db.numberList.size() > 0) {
                    gsmNumber.setText(db.numberList.get(0));
                } else {
                    gsmNumber.setText("No Number Yet Please Add Number");
                    gsmNumber.setTextColor(Color.parseColor("#F44336"));
                }

                String temp = "";
                username.setText(db.username.get(0));
                for(int i=0;i<db.password.get(0).length();i++){
                    temp += "*";
                }
                password.setText(temp);

                if(isUseSMS == true) {
                    bluetoothView.setVisibility(View.GONE);
                    showDialog = true;
                } else {
                    bluetoothView.setVisibility(View.VISIBLE);
                    try {
                        btSocket.getOutputStream().write("Request_Number".toString().getBytes());
                        byte[] buffer = new byte[1024];
                        Thread.sleep(1500);
                        int bytes = btSocket.getInputStream().read(buffer);
                        word = new String(buffer, 0, bytes);
                        if (word.charAt(0) == '+' && word.length() > 12 || word.equals("No Number")) {
                            showDialog = true;
                            int numberCount = 13;
                            if (word.length() == 0 || word.equals("No Number")) {
                                numList.add("No Contact Save in GSM Module");
                            } else {
                                for (int i = 0; i < (word.length() / numberCount); i++) {
                                    numList.add(word.substring(i * numberCount, (i * numberCount) + numberCount));
                                }
                            }
                        } else {
                            showDialog = false;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        numList.add("No Contact Save in GSM Module");
                    }
                    int x = 0;
                    x = numList.size();
                    int newDp = x * 75;
                    ViewGroup.LayoutParams params = lv.getLayoutParams();
                    params.height = newDp;
                    lv.setLayoutParams(params);
                    lv.requestLayout();
                    lv.setMinimumHeight(75);
                    lv.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, numList));
                }

                editNumber.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogCancel.dismiss();
                        addGSMNumber("update");
                    }
                });

                editAccount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogCancel.dismiss();
                        editUserPass();
                    }
                });

                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(num.getText().length() == 9){
                            dialogCancel.dismiss();
                            YesNoOption("save", "Do you want to save number?", num.getText().toString());
                        } else {
                            msg("Please Complete Number");
                        }
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogCancel.dismiss();
                    }
                });

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogCancel.dismiss();
                        YesNoOption("delete", "Do you want to delete all contacts?", null);
                    }
                });

                if (showDialog == true) {
                    dialog.setView(newView);
                    dialogCancel = dialog.show();
                } else {
                    msg("Please Try Again.");
                }
            }
        });

        appinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder appinfoDialog = new AlertDialog.Builder(MainActivity.this);

                LayoutInflater inflate = MainActivity.this.getLayoutInflater();
                View infoView = inflate.inflate(R.layout.info, null);
                BootstrapButton cancel = (BootstrapButton) infoView.findViewById(R.id.cancel);
                final TextView hannasheen = (TextView) infoView.findViewById(R.id.hannasheenEmail);
                final TextView jerome = (TextView) infoView.findViewById(R.id.jeromeEmail);
                final TextView jommel = (TextView) infoView.findViewById(R.id.jommelEmail);
                final TextView dennis = (TextView) infoView.findViewById(R.id.dennisEmail);
                hannasheen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openEmail(hannasheen.getText().toString());
                    }
                });
                jerome.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openEmail(jerome.getText().toString());
                    }
                });
                jommel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openEmail(jommel.getText().toString());
                    }
                });
                dennis.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openEmail(dennis.getText().toString());
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogCancel.dismiss();
                    }
                });
                appinfoDialog.setView(infoView);
                dialogCancel = appinfoDialog.show();
            }
        });

        lightEdit1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.getDeviceName("light1");
                if(db.dn.size() == 0) {
                    editNameShow("light1", "Enter Name for Light1");
                } else {
                    nameDevice = "light1";
                    editNameShow(nameDevice, "Edit Name for "+db.dn.get(0));
                }
            }
        });

        outletEdit1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.getDeviceName("outlet1");
                if(db.dn.size() == 0) {
                    editNameShow("outlet1", "Enter Name for Outlet1");
                } else {
                    nameDevice = "outlet1";
                    editNameShow(nameDevice, "Edit Name for "+db.dn.get(0));
                }
            }
        });

        outletEdit2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.getDeviceName("outlet2");
                if(db.dn.size() == 0) {
                    editNameShow("outlet2", "Enter Name for Outlet2");
                } else {
                    nameDevice = "outlet2";
                    editNameShow(nameDevice, "Edit Name for "+db.dn.get(0));
                }
            }
        });

        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSMS("Syncing", phoneNumber, "Syncing");
            }
        });

        new Thread(syncing).start();
        startService();
    }

    Runnable syncing = new Runnable() {
        @Override
        public void run() {
            while(true){
                if(isRunning){
                    isRunning = false;
                    Syncing();
                }
            }
        }
    };

    public void Syncing(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateButton();
                if(isShowing){
                    dialog.dismiss();
                }
                if(isProgressShowing){
                    isProgressShowing = false;
                    progress.dismiss();
                }
                AlertDialog.Builder sync = new AlertDialog.Builder(MainActivity.this);
                if(isISend.isEmpty()){
                    sync.setTitle("Appliances has been switch by other user");
                    sync.setMessage(appliancesName);
                } else if(isISend.equalsIgnoreCase("Syncing")){
                    sync.setTitle("Sync Successfully");
                } else {
                    sync.setTitle("Appliances turn "+isISend+" successfully");
                    sync.setMessage(appliancesName);
                }
                sync.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isISend = "";
                        appliancesName = "";
                        isShowing = false;
                    }
                });
                dialog = sync.create();
                dialog.setCancelable(false);
                dialog.show();
                isShowing = true;
            }
        });
    }

    public void openEmail(String email){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.parse("mailto:"+email);
        intent.setData(data);
        startActivity(intent);
    }

    public void createAccount(String num){
        try {
            String completeReg = "Create\nMobile Number:09" + num;
            btSocket.getOutputStream().write(completeReg.toString().getBytes());
            byte[] buffer = new byte[1024];
            Thread.sleep(1500);
            int bytes = btSocket.getInputStream().read(buffer);
            String result = new String(buffer, 0, bytes);
            if(result.equalsIgnoreCase("Save")){
                msg("Number Successfully Register.");
            } else {
                msg("Failed to save Number. Maximum save number cannot exceed 10");
            }
        } catch (Exception e) {
            Intent intent = new Intent(this, Login.class);
            intent.putExtra("error", "It looks like something wrong with bluetooth.");
            startActivity(intent);
        }
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private void switchAppliances(String command, String name, int value) {
        try {
            btSocket.getOutputStream().write(command.getBytes());
            db.updateSetting(name, value);
            updateButton();
        }
        catch (IOException e) {
            Intent intent = new Intent(this, Login.class);
            intent.putExtra("error", "It looks like something wrong with bluetooth");
            startActivity(intent);
        }
    }

    public void updateName(){
        db.getDeviceName("light1");
        if(db.dn.size() != 0){
            lName1.setText(db.dn.get(0));
        }
        db.getDeviceName("outlet1");
        if(db.dn.size() != 0){
            oName1.setText(db.dn.get(0));
        }
        db.getDeviceName("outlet2");
        if(db.dn.size() != 0){
            oName2.setText(db.dn.get(0));
        }
    }

    public void updateButton(){
        db.getSetting();
        //Light 1
        if(db.light1.get(0) == 1){
            imgLight1.setImageResource(R.drawable.bulb_on);
            light1.setEnabled(false);
            light1.setAlpha(0.3f);
            light1Off.setEnabled(true);
            light1Off.setAlpha(1f);
        }else{
            imgLight1.setImageResource(R.drawable.bulb_off);
            light1.setEnabled(true);
            light1.setAlpha(1f);
            light1Off.setEnabled(false);
            light1Off.setAlpha(0.3f);
        }
        //Outlet 1
        if(db.outlet1.get(0) == 1){
            imgOutlet1.setImageResource(R.drawable.outlet_on);
            outlet1.setEnabled(false);
            outlet1.setAlpha(0.3f);
            outlet1Off.setEnabled(true);
            outlet1Off.setAlpha(1f);
        } else{
            imgOutlet1.setImageResource(R.drawable.outlet_off);
            outlet1.setEnabled(true);
            outlet1.setAlpha(1f);
            outlet1Off.setEnabled(false);
            outlet1Off.setAlpha(0.3f);
        }
        //Outlet 2
        if(db.outlet2.get(0) == 1){
            imgOutlet2.setImageResource(R.drawable.outlet_on);
            outlet2.setEnabled(false);
            outlet2.setAlpha(0.3f);
            outlet2Off.setEnabled(true);
            outlet2Off.setAlpha(1f);
        } else{
            imgOutlet2.setImageResource(R.drawable.outlet_off);
            outlet2.setEnabled(true);
            outlet2.setAlpha(1f);
            outlet2Off.setEnabled(false);
            outlet2Off.setAlpha(0.3f);
        }
    }

    public void addGSMNumber(final String CreateOrUpdate){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflate = MainActivity.this.getLayoutInflater();
        View view = inflate.inflate(R.layout.edit_name,null);
        TextView label = (TextView) view.findViewById(R.id.textView9);
        TextView label2 = (TextView) view.findViewById(R.id.textView2);
        final EditText name = (EditText) view.findViewById(R.id.name);
        final BootstrapButton save = (BootstrapButton) view.findViewById(R.id.save);
        final BootstrapButton cancel = (BootstrapButton) view.findViewById(R.id.cancel);
        label.setText("Add Number");
        label2.setText("Enter GSM module");
        label2.setTextSize(14);
        name.setHint("Enter Number (ex. 09*********)");
        name.setInputType(InputType.TYPE_CLASS_NUMBER);
        name.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = name.getText().toString();
                if(!phoneNumber.equals("")) {
                    String pn = ""+phoneNumber.charAt(0) + phoneNumber.charAt(1);
                    if(pn.equals("09")){
                        if(phoneNumber.length() == 11){
                            if(CreateOrUpdate.equalsIgnoreCase("create")){
                                db.saveNumber(name.getText().toString());
                                msg("GSM Number Save Successfully.");
                            } else if(CreateOrUpdate.equalsIgnoreCase("update")){
                                db.updateNumber(name.getText().toString());
                                msg("GSM Number Update Successfully.");
                            }
                            dialogCancel.dismiss();
                            db.getNumber();
                            phoneNumber = db.numberList.get(0);
                        } else {
                            msg("Invalid Number! phone number should exactly 11 number.");
                        }
                    } else {
                        msg("Please Follow the format. Ex. 09*********");
                    }
                } else {
                    msg("Please Enter Number.");
                }
            }
        });
        if(CreateOrUpdate.equalsIgnoreCase("update")){
            cancel.setVisibility(View.VISIBLE);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogCancel.dismiss();
                }
            });
        } else {
            cancel.setVisibility(View.GONE);
        }
        dialog.setCancelable(false);
        dialog.setView(view);
        dialogCancel = dialog.show();
    }

    public void editUserPass(){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflate = MainActivity.this.getLayoutInflater();
        View nv = inflate.inflate(R.layout.edit_account,null);
        final EditText username = (EditText) nv.findViewById(R.id.userInput);
        final EditText password = (EditText) nv.findViewById(R.id.passwordInput);
        final BootstrapButton save = (BootstrapButton) nv.findViewById(R.id.save);
        final BootstrapButton cancel = (BootstrapButton) nv.findViewById(R.id.cancel);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if(username.getText().toString().length() > 5 && password.getText().toString().length() > 5) {
                        db.updateAccount(username.getText().toString(), password.getText().toString());
                        msg("Account Updated Successfully.");
                        dialogCancel.dismiss();
                    } else {
                        msg("Username and Password must be 6 character and above.");
                    }
                } catch(Exception ex){
                    ex.printStackTrace();
                    msg("Username or Password cannot be empty.");
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogCancel.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.setView(nv);
        dialogCancel = dialog.show();
    }

    public void editNameShow(final String deviceDefaultName, String labelName){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflate = MainActivity.this.getLayoutInflater();
        View view = inflate.inflate(R.layout.edit_name,null);
        TextView label = (TextView) view.findViewById(R.id.textView9);
        final EditText name = (EditText) view.findViewById(R.id.name);
        final BootstrapButton save = (BootstrapButton) view.findViewById(R.id.save);
        final BootstrapButton cancel = (BootstrapButton) view.findViewById(R.id.cancel);
        label.setText(labelName);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!name.getText().toString().equals("")) {
                    if(nameDevice.equals("")) {
                        db.saveName(deviceDefaultName, name.getText().toString());
                        updateName();
                        dialogCancel.dismiss();
                        nameDevice = "";
                    } else {
                        db.updateDeviceName(nameDevice, name.getText().toString());
                        updateName();
                        dialogCancel.dismiss();
                        nameDevice = "";
                    }
                } else {
                    msg("Please Enter Name.");
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogCancel.dismiss();
                nameDevice = "";
            }
        });

        dialog.setView(view);
        dialogCancel = dialog.show();
    }

    public void failedDialog(String msg){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Sending SMS code failed");
        dialog.setMessage("Cause: "+msg);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.show();
    }

    public void msg(String s) {
        Toast.makeText(MainActivity.this,s,Toast.LENGTH_SHORT).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>{
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(MainActivity.this, "Connecting", "Please wait!");
        }

        @Override
        protected Void doInBackground(Void... devices){
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e) {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result){
            super.onPostExecute(result);
            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                isBtConnected = true;
            }
            progress.dismiss();
            countdownStart();
        }
    }

    public void sendSMS(String onOrOff, final String pn, final String message) {
        isProgressShowing = true;
        isISend = onOrOff.toLowerCase();
        if(onOrOff.equalsIgnoreCase("Syncing")){
            progress = ProgressDialog.show(MainActivity.this, "Syncing to other devices", "Please wait...");
        } else {
            progress = ProgressDialog.show(MainActivity.this, "Sending sms code to turn "+onOrOff.toLowerCase()+" the appliances", "Please wait...");
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String SENT = "SMS_SENT";
                String DELIVERED = "SMS_DELIVERED";

                PendingIntent sentPI = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(SENT), 0);

                PendingIntent deliveredPI = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(DELIVERED), 0);

                //---when the SMS has been sent---
                registerReceiver(new BroadcastReceiver(){
                    @Override
                    public void onReceive(Context arg0, Intent arg1) {
                        switch (getResultCode()) {
                            case Activity.RESULT_OK:
                                startService();
                                deleteConversation();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progress.setTitle("SMS Send Successfully");
                                        progress.setMessage("Please Wait the device to respond.");
                                    }
                                });
                                break;
                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                failedDialog("Insufficient Load\nPlease Check your balance and try again.");
                                deleteConversation();
                                progress.dismiss();
                                break;
                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                failedDialog("No Signal or Low Signal\nPlease Check network signal and try again.");
                                deleteConversation();
                                progress.dismiss();
                                break;
                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                msg("Null PDU");
                                deleteConversation();
                                progress.dismiss();
                                break;
                            case SmsManager.RESULT_ERROR_RADIO_OFF:
                                failedDialog("Sim Card not Available or Airplane Mode is on\nPlease Check if your sim is available or Turn off the Airplane Mode and try again.");
                                deleteConversation();
                                progress.dismiss();
                                break;
                        }
                    }
                }, new IntentFilter(SENT));

                //---when the SMS has been delivered---
                registerReceiver(new BroadcastReceiver(){
                    @Override
                    public void onReceive(Context arg0, Intent arg1) {
                        switch (getResultCode()) {
                            case Activity.RESULT_OK:
                                msg("Appliances received the code");
                                break;
                            case Activity.RESULT_CANCELED:
                                failedDialog("Device not ready\nPlease Check the device if is already power on and try again.");
                                deleteConversation();
                                break;
                        }
                    }
                }, new IntentFilter(DELIVERED));

                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(pn, null, message, sentPI, deliveredPI);
            }
        });
    }

    public void deleteConversation(){
        try {
            Uri uriSms = Uri.parse("content://sms/sent");
            Cursor c = getContentResolver().query(uriSms, null, null, null, null);
            if (c != null && c.moveToFirst()) {
                do {
                    long threadId = c.getLong(1);
                    System.out.println("threadId:: "+threadId);
                    getContentResolver().delete(Uri.parse("content://sms/conversations/" + threadId), "address=?", new String[]{phoneNumber});
                } while (c.moveToNext());
            }
            c.close();
        }catch (Exception e) {
            System.out.println("Exception:: "+e);
        }
    }

    @Override
    public void onDestroy(){
        isProgressShowing = false;
        requestSMSmode();
        super.onDestroy();
    }

    @Override
    public void onBackPressed(){
        YesNoOption("exit","Do you want to exit?",null);
    }

    public void YesNoOption(final String status, String title, final String num){
        LayoutInflater inflate = MainActivity.this.getLayoutInflater();
        View newView = inflate.inflate(R.layout.edit_name, null);
        TextView label = (TextView) newView.findViewById(R.id.textView9);
        TextView label2 = (TextView) newView.findViewById(R.id.textView2);
        final EditText name = (EditText) newView.findViewById(R.id.name);
        final BootstrapButton save = (BootstrapButton) newView.findViewById(R.id.save);
        final BootstrapButton cancel = (BootstrapButton) newView.findViewById(R.id.cancel);

        label.setText(title);
        label2.setVisibility(View.GONE);
        name.setVisibility(View.GONE);
        save.setMarkdownText("Yes");
        cancel.setMarkdownText("No");

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(status.equals("exit")) {
                    requestSMSmode();
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    startActivity(intent);
                    finish();
                } else if(status.equals("delete")) {
                    try {
                        btSocket.getOutputStream().write("Reset".toString().getBytes());
                        msg("GSM Module Contacts Cleared.");
                        dialogCancel.dismiss();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if(status.equals("sms")) {
                    requestSMSmode();
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.putExtra("useMode", true);
                    finish();
                    startActivity(intent);
                } else {
                    createAccount(num);
                    dialogCancel.dismiss();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogCancel.dismiss();
            }
        });

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setView(newView);
        dialogCancel = dialog.show();
    }

    public void requestSMSmode(){
        try {
            if(!isUseSMS) {
                btSocket.getOutputStream().write("Request SMS Mode".getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startService(){
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000, pendingIntent);
    }

    public void countdownStart(){
        LayoutInflater inflate = MainActivity.this.getLayoutInflater();
        View newView = inflate.inflate(R.layout.progress_loading, null);
        timer = (TextView) newView.findViewById(R.id.timer);
        progress.setContentView(newView);
        progress.show();
        countdown.start();
    }

    CountDownTimer countdown = new CountDownTimer(4000,1000) {
        @Override
        public void onTick(long l) {
            timer.setText(""+(((l+1)/1000)-1));
        }

        @Override
        public void onFinish() {
            progress.dismiss();
        }
    };

    public void checkPermission(){
        if (ActivityCompat.checkSelfPermission(this, SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_SEND_SMS);
        }

        if (ActivityCompat.checkSelfPermission(this, RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{RECEIVE_SMS}, PERMISSION_RECEIVE_SMS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_SEND_SMS: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    deniedDialog();
                } else {
                    msg("Permission Granted!");
                }
                return;
            }

            case PERMISSION_RECEIVE_SMS: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    deniedDialog();
                } else {
                    msg("Permission Granted!");
                }
                return;
            }
        }
    }

    public void deniedDialog(){
        AlertDialog.Builder deniedDialog = new AlertDialog.Builder(this);
        deniedDialog.setTitle("Permission Denied");
        deniedDialog.setMessage("This Application Need to accept the permission in order to use the full functionality of this application");
        deniedDialog.setPositiveButton("", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(MainActivity.this,Login.class);
                startActivity(intent);
                finish();
            }
        });
        deniedDialog.setCancelable(false);
        deniedDialog.show();
    }
}
