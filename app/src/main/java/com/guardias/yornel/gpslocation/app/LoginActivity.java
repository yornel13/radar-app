package com.guardias.yornel.gpslocation.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guardias.yornel.gpslocation.R;
import com.guardias.yornel.gpslocation.db.DataHelper;
import com.guardias.yornel.gpslocation.db.LoginAdminTask;
import com.guardias.yornel.gpslocation.db.LoginUserTask;
import com.guardias.yornel.gpslocation.entity.Admin;
import com.guardias.yornel.gpslocation.entity.User;
import com.guardias.yornel.gpslocation.util.Password;

import java.util.Timer;
import java.util.TimerTask;

import jp.wasabeef.blurry.Blurry;

public class LoginActivity extends BaseActivity implements LoginAdminTask.AdminLogin, LoginUserTask.UserLogin {

    public static final String TAG = "LoginActivity";

    private static final long SPLASH_SCREEN_DELAY = 1500;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private ImageView background;
    private RelativeLayout container;
    private RelativeLayout containerLogin;
    private EditText userField;
    private EditText passwordField;
    private TextView titleLogin;
    private Boolean loginGuard = true;

    private LoginAdminTask loginAdmin;
    private LoginUserTask loginUser;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        background = (ImageView) findViewById(R.id.image);
        container = (RelativeLayout) findViewById(R.id.container_image);
        containerLogin = (RelativeLayout) findViewById(R.id.container_login);
        userField = (EditText) findViewById(R.id.input_user);
        passwordField = (EditText) findViewById(R.id.input_password);
        titleLogin = (TextView) findViewById(R.id.login_title);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                blurBackground();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (user != null) {
                            loginUserSuccessful(user);
                        } else {
                            Animation fadeInAnimation = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade_in);
                            containerLogin.startAnimation(fadeInAnimation);
                            containerLogin.setVisibility(View.VISIBLE);
                        }

                    }
                });

            }
        };

        passwordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_ACTION_DONE)
                        || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                        && (event.getAction() == KeyEvent.ACTION_DOWN))) {
                    login(null);
                    return true;
                } else {
                    return false;
                }
            }
        });
        userField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_ACTION_DONE)
                        || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                        && (event.getAction() == KeyEvent.ACTION_DOWN))) {
                    login(null);
                    return true;
                } else {
                    return false;
                }
            }
        });

        // Simulate a long loading process on application startup.
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);

        user = preferences.getUser();

    }

    void blurBackground() {
        Blurry.with(this)
                .radius(10)
                .sampling(8)
                .color(getResources().getColor(R.color.colorPrimaryDark))
                .async()
                .animate(1000)
                .onto(container);
    }

    public void login(View view) {
        checkPermission();

        if (userField.getText().toString().isEmpty()) {
            userField.setError(getString(R.string.introduce_user));
            userField.requestFocus();
        } else if (passwordField.getText().toString().isEmpty()) {
            passwordField.setError(getString(R.string.introduce_password));
            passwordField.requestFocus();
        } else {
            hideKeyboard();

            String userText = userField.getText().toString();
            String passText = Password.MD5(passwordField.getText().toString());
            if (loginGuard) {
                showProgressDialog(getString(R.string.loading));
                loginUser = new LoginUserTask(userText, passText, this, this);
                loginUser.execute((Void) null);
            } else {
                showProgressDialog(getString(R.string.loading));
                loginAdmin = new LoginAdminTask(userText, passText, this, this);
                loginAdmin.execute((Void) null);
            }
        }
    }

    public void changeUserType(View view) {
        ImageButton button = (ImageButton) view;
        if (loginGuard) {
            button.setImageResource(R.drawable.admin_user);
            titleLogin.setText(getString(R.string.title_login_admin));
        } else {
            button.setImageResource(R.drawable.policeman);
            titleLogin.setText(getString(R.string.title_login_guard));
        }
        loginGuard = !loginGuard;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setMessage(R.string.close_app)
                .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onCloseApp();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // nothing to do
                    }
                }).show();
    }

    public void onCloseApp() {
        super.onBackPressed();
    }

    public void hideKeyboard() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(userField.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(passwordField.getWindowToken(), 0);
    }

    @Override
    public void loginAdminSuccessful(Admin admin) {
        hideProgressDialog();
        startActivity(new Intent(this, MainAdminActivity.class));
        finish();
    }

    @Override
    public void loginAdminFailure(String message) {
        hideProgressDialog();
        Snackbar.make(containerLogin, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void loginUserSuccessful(User user) {
        checkPermission();

        if (user.getGroup() == null
                || user.getGroup().getRoute() == null) {
            Snackbar.make(containerLogin,
                    R.string.this_user_dont_have_route, Snackbar.LENGTH_LONG).show();
            hideProgressDialog();
        } else {
            preferences.save(user);
            hideProgressDialog();
            startActivity(new Intent(this, GuardActivity.class));
            finish();
        }
    }

    @Override
    public void loginUserFailure(String message) {
        hideProgressDialog();
        Snackbar.make(containerLogin, message, Snackbar.LENGTH_LONG).show();
    }


    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            }
        }
        System.out.println(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE));

    }
}
