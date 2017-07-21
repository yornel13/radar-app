package com.guardias.yornel.gpslocation.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.guardias.yornel.gpslocation.R;
import com.guardias.yornel.gpslocation.util.AppPreferences;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmConfiguration;

import static com.guardias.yornel.gpslocation.util.Const.DB_NAME;


/**
 * Created by Yornel on 07-jul-16.
 */
public class BaseActivity extends AppCompatActivity {

    protected View viewContainer;
    protected View progressBar;
    protected AppPreferences preferences;
    protected ProgressDialog progressDialog;
    protected Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = new AppPreferences(this);

        Realm.init(getApplicationContext());
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "databases");
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name(DB_NAME)
                .schemaVersion(0)
                //.directory(mediaStorageDir)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        realm = Realm.getDefaultInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_empty, menu);
        return true;
    }

    protected void setUpToolbarWithTitle(boolean hasBackButton){
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(hasBackButton);
            getSupportActionBar().setDisplayHomeAsUpEnabled(hasBackButton);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            /*case R.id.exit:
                preferences.clearUser();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;*/
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * Shows the progress UI and hides the form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    protected void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            viewContainer.setVisibility(show ? View.GONE : View.VISIBLE);
            viewContainer.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    viewContainer.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            progressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            viewContainer.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }



    public void showProgressDialog(String text) {
        progressDialog = new ProgressDialog(this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(text);
        progressDialog.show();
    }

    public void hideProgressDialog() {
        progressDialog.dismiss();
    }
}
