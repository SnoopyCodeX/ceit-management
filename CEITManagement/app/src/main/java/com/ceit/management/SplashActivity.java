package com.ceit.management;

import android.os.Bundle;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        startTimer();
    }

    private void startTimer()
    {
        (new Thread() {
            @Override
            public void run()
            {
                try {
                    Thread.sleep(6000);
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    SplashActivity.this.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}