package com.laguna.university.homeappliancecontrol;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.TypefaceProvider;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class Login extends AppCompatActivity {

    //1 if login success 0 if not
    int loginStatus = 0;
    //0 is open BT failed
    int REQUEST_ENABLE_BT = 0;

    EditText username,password;
    BootstrapButton btBtn,smsBtn,findDeviceBtn,listPairedBtn;
    CardView btModeLayout;
    ListView deviceList;

    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";
    ArrayList<BluetoothDevice> mDeviceList = new ArrayList<>();
    ArrayList<BluetoothDevice> deviceSave = new ArrayList<>();
    ArrayList mDeviceListString = new ArrayList<>();
    ArrayList list = new ArrayList<>();

    private ProgressDialog progress;
    AlertDialog dialogCancel;

    SQLiteDBcontroller db = new SQLiteDBcontroller(this);

    String phoneNumber = "09090067944";

    boolean isFindDeviceBtnPressed = false, isLoginSend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceProvider.registerDefaultIconSets();
        //full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        checkPermission();
        setAppAsDefaultMessaging();

        View backgroundImage = findViewById(R.id.activity_login);
        Drawable background = backgroundImage.getBackground();
        background.setAlpha(80);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        smsBtn = (BootstrapButton) findViewById(R.id.sms);
        btBtn = (BootstrapButton) findViewById(R.id.bt);
        findDeviceBtn = (BootstrapButton) findViewById(R.id.fdevice);
        listPairedBtn = (BootstrapButton) findViewById(R.id.dlist);
        deviceList = (ListView) findViewById(R.id.lv);
        btModeLayout = (CardView) findViewById(R.id.btCard);

        //username.setText("jerome");
        //password.setText("jerome");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try{
            String prompt = getIntent().getExtras().getString("error").toString();
            showToast(prompt);
        } catch(Exception e){

        }

        db.getNumber();
        if(db.numberList.size() == 0){
            addGSMNumber("create");
        } else {
            phoneNumber = db.numberList.get(0);
        }

        smsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(username.getText().toString(), password.getText().toString());
                if(loginStatus == 1) {
                    LayoutInflater inflate = Login.this.getLayoutInflater();
                    View newView = inflate.inflate(R.layout.edit_name, null);
                    TextView label = (TextView) newView.findViewById(R.id.textView9);
                    TextView label2 = (TextView) newView.findViewById(R.id.textView2);
                    final EditText name = (EditText) newView.findViewById(R.id.name);
                    final BootstrapButton save = (BootstrapButton) newView.findViewById(R.id.save);
                    final BootstrapButton cancel = (BootstrapButton) newView.findViewById(R.id.cancel);

                    label.setText("Use SMS Mode?\nNote: You need Load to use this function");
                    label.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    label.setTextSize(18);
                    label2.setVisibility(View.GONE);
                    name.setVisibility(View.GONE);
                    save.setMarkdownText("Yes");
                    cancel.setMarkdownText("No");

                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                if (mBluetoothAdapter.isEnabled()) {
                                    mBluetoothAdapter.disable();
                                    btModeLayout.setVisibility(View.GONE);
                                }
                            } catch(Exception ex){
                                ex.printStackTrace();
                            }
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            intent.putExtra("useMode", true);
                            startActivity(intent);
                            finish();
                        }
                    });
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialogCancel.dismiss();
                        }
                    });

                    AlertDialog.Builder dialog = new AlertDialog.Builder(Login.this);
                    dialog.setView(newView);
                    dialogCancel = dialog.show();
                }
            }
        });

        btBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(username.getText().toString(), password.getText().toString());
                if(loginStatus == 1) {
                    try {
                        if (mBluetoothAdapter.isEnabled()) {
                            mBluetoothAdapter.disable();
                        }
                    } catch(Exception e){

                    }
                    Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnOn, REQUEST_ENABLE_BT);
                }
            }
        });

        findDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findDeviceBtn.setShowOutline(false);
                listPairedBtn.setShowOutline(true);
                //set listview find BT device
                progress = ProgressDialog.show(Login.this, "Searching Devices...", "Please wait!");
                progress.setCancelable(true);
                progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        mBluetoothAdapter.cancelDiscovery();
                    }
                });
                startSearchingDevice();
                isFindDeviceBtnPressed = true;
            }
        });

        listPairedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findDeviceBtn.setShowOutline(true);
                listPairedBtn.setShowOutline(false);
                //set listView paired device
                pairedDevicesList();
                isFindDeviceBtnPressed = false;
            }
        });

        db.getAccount();
        if(db.username.get(0).equals("CAPSTONE_HACv1.0") && db.password.get(0).equals("18_02_20_07")) {
            toastOnly("See the Manual for the default Username and Password");
        }
    }

    public void startSearchingDevice(){
        mBluetoothAdapter.startDiscovery();
        deviceList.setAdapter(null);
        mDeviceListString.clear();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_ENABLE_BT){
            if (resultCode == RESULT_OK) {
                showToast("Bluetooth Mode Selected.\nPlease Select Home Appliances Control Device");
                btModeLayout.setVisibility(View.VISIBLE);
                btBtn.setShowOutline(false);
                pairedDevicesList();
            } else if (resultCode == RESULT_CANCELED) {
                btModeLayout.setVisibility(View.GONE);
                btBtn.setShowOutline(true);
            }
        }
        if(requestCode == 1){
            if (resultCode == RESULT_OK) {
                toastOnly("Permission Granted!");
            } else if (resultCode == RESULT_CANCELED) {
                deniedDialog();
            }
        }
    }

    public void login(String username, String password){
        try {
            db.getAccount();
            if (username.equals(db.username.get(0)) && password.equals(db.password.get(0))) {
                loginStatus = 1;
            } else {
                showToast("Incorrect username or password.");
            }
        } catch (Exception e){
            showToast("username or password Cannot be empty.");
        }
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
            findDeviceBtn.setShowOutline(true);
            listPairedBtn.setShowOutline(false);
            pairedDevicesList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pairedDevicesList() {
        list.clear();
        deviceSave.clear();

        pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size()>0) {
            for(BluetoothDevice bt : pairedDevices) {
                deviceSave.add(bt);
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            showToast("No Paired Bluetooth Devices Found.");
        }
        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,list);


        int x = list.size();
        int newDp = x * 75;
        ViewGroup.LayoutParams params = deviceList.getLayoutParams();
        params.height = newDp;
        deviceList.setLayoutParams(params);
        deviceList.requestLayout();
        deviceList.setMinimumHeight(75);

        deviceList.setAdapter(adapter);
        deviceList.setOnItemClickListener(myListClickListener);
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick (AdapterView av, final View v, int arg2, long arg3) {
            try{
                login(username.getText().toString(), password.getText().toString());
                BluetoothDevice device;
                if(isFindDeviceBtnPressed) {
                    device = mDeviceList.get(arg2);
                }else{
                    device = deviceSave.get(arg2);
                }
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    if (loginStatus == 1) {
                        LayoutInflater inflate = Login.this.getLayoutInflater();
                        View BTview = inflate.inflate(R.layout.edit_name, null);
                        TextView label = (TextView) BTview.findViewById(R.id.textView9);
                        TextView label2 = (TextView) BTview.findViewById(R.id.textView2);
                        final EditText name = (EditText) BTview.findViewById(R.id.name);
                        final BootstrapButton save = (BootstrapButton) BTview.findViewById(R.id.save);
                        final BootstrapButton cancel = (BootstrapButton) BTview.findViewById(R.id.cancel);

                        label.setText("Bluetooth Already Activated in Device?");
                        label2.setVisibility(View.GONE);
                        name.setVisibility(View.GONE);
                        save.setMarkdownText("Yes");
                        cancel.setMarkdownText("No");

                        save.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String info = ((TextView) v).getText().toString();
                                String address = info.substring(info.length() - 17);
                                Intent i = new Intent(Login.this, MainActivity.class);
                                i.putExtra(EXTRA_ADDRESS, address);
                                startActivity(i);
                            }
                        });

                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                progress = ProgressDialog.show(Login.this, "Requesting Bluetooth Mode", "Please wait...");
                                isLoginSend = false;
                                sendSMS("Request BT Mode", v);
                                dialogCancel.dismiss();
                            }
                        });

                        AlertDialog.Builder dialog = new AlertDialog.Builder(Login.this);
                        dialog.setView(BTview);
                        dialogCancel = dialog.show();
                    } else {
                        showToast("Make Sure To Enter username and password before connecting to bluetooth device.");
                    }
                } else {
                    Toast.makeText(Login.this,"Pairing...", Toast.LENGTH_SHORT).show();
                    pairDevice(device);
                }
            } catch (Exception ex){
                showToast(ex.toString());
            }
        }
    };

    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {
                    //status("Discovering...");
                }
            } else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                showToast("Done Discovering.");
                progress.dismiss();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                progress.setMessage("Found device " + device.getName());
                if(mDeviceList.size() == 0) {
                    mDeviceList.add(device);
                    mDeviceListString.add(device.getName() + "\n" + device.getAddress());
                } else {
                    if (!(mDeviceList.get(0).getAddress() == device.getAddress())) {
                        mDeviceList.add(device);
                        mDeviceListString.add(device.getName() + "\n" + device.getAddress());
                    }
                }
                ArrayAdapter<String> deviceAdapter = new ArrayAdapter(Login.this, android.R.layout.simple_list_item_1, mDeviceListString);

                int x = mDeviceList.size();
                int newDp = x * 75;
                ViewGroup.LayoutParams params = deviceList.getLayoutParams();
                params.height = newDp;
                deviceList.setLayoutParams(params);
                deviceList.requestLayout();
                deviceList.setMinimumHeight(75);

                deviceList.setAdapter(deviceAdapter);
            }
        }
    };
    public void showToast(String s){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(s);
        dialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.show();
    }

    @Override
    public void onDestroy(){
        try {
            mBluetoothAdapter.disable();
        } catch(Exception e){

        }
        super.onDestroy();
    }
    @Override
    public void onBackPressed(){
        YesNoOption("Do you want to exit?");
    }

    public void YesNoOption(String title){
        LayoutInflater inflate = Login.this.getLayoutInflater();
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
                Intent intent = new Intent(Login.this, Exit.class);
                startActivity(intent);
                finish();
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


    private static final int PERMISSION_COARSE_LOCATION = 123;
    public void checkPermission(){
        if (ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_COARSE_LOCATION}, PERMISSION_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted
        } else {
            deniedDialog();
        }
    }

    public void sendSMS(final String text, final View v) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String SENT = "SMS_SENT";
                String DELIVERED = "SMS_DELIVERED";

                PendingIntent sentPI = PendingIntent.getBroadcast(Login.this, 0, new Intent(SENT), 0);

                PendingIntent deliveredPI = PendingIntent.getBroadcast(Login.this, 0, new Intent(DELIVERED), 0);

                //---when the SMS has been sent---
                registerReceiver(new BroadcastReceiver(){
                    @Override
                    public void onReceive(Context arg0, Intent arg1) {
                        switch (getResultCode()) {
                            case Activity.RESULT_OK:
                                //dito ung intent
                                if(!isLoginSend) {
                                    progress.dismiss();
                                    String info = ((TextView) v).getText().toString();
                                    String address = info.substring(info.length() - 17);
                                    Intent i = new Intent(Login.this, MainActivity.class);
                                    i.putExtra(EXTRA_ADDRESS, address);
                                    startActivity(i);
                                    deleteConversation();
                                    isLoginSend = true;
                                }
                                break;
                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                progress.dismiss();
                                showToast("Please Try Again.");
                                deleteConversation();
                                break;
                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                progress.dismiss();
                                showToast("No Signal");
                                deleteConversation();
                                break;
                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                progress.dismiss();
                                showToast("Please Try Again.");
                                deleteConversation();
                                break;
                            case SmsManager.RESULT_ERROR_RADIO_OFF:
                                progress.dismiss();
                                showToast("Airplane Mode\nPlease Disable Airplane Mode to send message to device");
                                deleteConversation();
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
                                break;
                            case Activity.RESULT_CANCELED:
                                deleteConversation();
                                break;
                        }
                    }
                }, new IntentFilter(DELIVERED));

                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(phoneNumber, null, text, sentPI, deliveredPI);
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

    public void addGSMNumber(final String CreateOrUpdate){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(Login.this);
        LayoutInflater inflate = Login.this.getLayoutInflater();
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
                                showToast("GSM Number Save Successfully.");
                            } else if(CreateOrUpdate.equalsIgnoreCase("update")){
                                db.updateNumber(name.getText().toString());
                                showToast("GSM Number Update Successfully.");
                            }
                            dialogCancel.dismiss();
                            db.getNumber();
                            phoneNumber = db.numberList.get(0);
                        } else {
                            toastOnly("Invalid Number! phone number should exactly 11 number.");
                        }
                    } else {
                        toastOnly("Please Enter Valid Number. Ex. 09*********");
                    }
                } else {
                    toastOnly("Please Enter Number.");
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

    public void toastOnly(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    public void setAppAsDefaultMessaging(){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String mDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this);
            if (!getPackageName().equals(mDefaultSmsApp)) {
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
                startActivityForResult(intent, 1);
            }
        }
    }

    public void deniedDialog(){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(Login.this);
        LayoutInflater inflate = Login.this.getLayoutInflater();
        View newView = inflate.inflate(R.layout.edit_name, null);

        TextView label = (TextView) newView.findViewById(R.id.textView9);
        TextView label2 = (TextView) newView.findViewById(R.id.textView2);
        final EditText name = (EditText) newView.findViewById(R.id.name);
        final BootstrapButton save = (BootstrapButton) newView.findViewById(R.id.save);
        final BootstrapButton cancel = (BootstrapButton) newView.findViewById(R.id.cancel);

        label.setText("Permission Denied!");
        label2.setText("This application need to accept the permission in order to use this application.");
        name.setVisibility(View.GONE);
        cancel.setVisibility(View.GONE);
        save.setMarkdownText("OK");

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        AlertDialog.Builder deniedDialog = new AlertDialog.Builder(this);
        deniedDialog.setView(newView);
        deniedDialog.setCancelable(false);
        deniedDialog.show();
    }
}
