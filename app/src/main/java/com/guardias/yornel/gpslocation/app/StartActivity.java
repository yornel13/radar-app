package com.guardias.yornel.gpslocation.app;

import android.content.Intent;
import android.os.Bundle;

import com.guardias.yornel.gpslocation.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Yornel on 17/7/2017.
 */

public class StartActivity extends BaseActivity {

    private static final Integer USER_ADMIN = 1;
    private static final Integer USER_GUARD = 2;

    // Set the duration of the splash screen
    private static final long SPLASH_SCREEN_DELAY = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_screen);

        final Integer userTime = 1;

        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                runActivity(userTime);
            }
        };

        // Simulate a long loading process on application startup.
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }

    void runActivity(int userTime) {
        if (userTime == USER_ADMIN) {
            startActivity(new Intent(this, MainAdminActivity.class));
            finish();
        } else if (userTime == USER_GUARD) {
            startActivity(new Intent(this, GuardActivity.class));
            finish();
        }
    }

}
