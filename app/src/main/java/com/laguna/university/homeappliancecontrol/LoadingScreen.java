package com.laguna.university.homeappliancecontrol;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LoadingScreen extends AppCompatActivity {

    private Boolean exit = false;
    RelativeLayout click;
    ProgressBar progressBar;
    TextView percent;
    Handler mHandler = new Handler();
    int maxprogress,progressvalue;

    SQLiteDBcontroller db = new SQLiteDBcontroller(this);

    boolean isDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_loading_screen);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        percent = (TextView) findViewById(R.id.percentage);
        click = (RelativeLayout) findViewById(R.id.clickhere);

        maxprogress = progressBar.getMax();
        progressvalue = progressBar.getProgress();

        progressBar.setScaleY(9f);

        db.getAccount();
        if(db.username.size() == 0){
            db.saveAccount("Admin","Admin");
        }

        new Thread(new Runnable() {
            public void run() {
                while (progressvalue < maxprogress) {
                    progressvalue += 1;
                    mHandler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressvalue);
                            String prog = progressvalue+"%";
                            percent.setText(""+prog);
                            if(progressvalue == maxprogress){
                                percent.setText("Click screen to continue");
                                isDone = true;
                            }
                        }
                    });
                    try {
                        Thread.sleep(50);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isDone){
                    Intent newtab = new Intent(LoadingScreen.this,Login.class);
                    startActivity(newtab);
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed(){
        if (exit) {
            finish();
        }
    }
}
