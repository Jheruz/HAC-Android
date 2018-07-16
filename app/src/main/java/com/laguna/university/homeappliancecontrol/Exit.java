package com.laguna.university.homeappliancecontrol;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Exit extends AppCompatActivity {

    TextView hidden,time;
    Handler mHandler = new Handler();
    int progressvalue = 5, maxprogress = 0;
    RelativeLayout exit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_exit);
        hidden = (TextView) findViewById(R.id.done);
        time = (TextView) findViewById(R.id.timer);
        exit = (RelativeLayout) findViewById(R.id.activity_exit);

        new Thread(new Runnable() {
            public void run() {
                while (progressvalue >= maxprogress) {
                    progressvalue --;
                    mHandler.post(new Runnable() {
                        public void run() {
                            time.setText(""+progressvalue);
                            if(progressvalue == maxprogress){
                                hidden.setVisibility(View.VISIBLE);
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                time.setVisibility(View.GONE);
                            }
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hidden.getVisibility() == View.VISIBLE){
                    finishAffinity();
                }
            }
        });
    }
}
