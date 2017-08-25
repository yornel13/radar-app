package com.guardias.yornel.gpslocation.app;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.guardias.yornel.gpslocation.R;
import com.guardias.yornel.gpslocation.db.DataHelper;
import com.guardias.yornel.gpslocation.entity.Position;
import com.guardias.yornel.gpslocation.entity.User;
import com.guardias.yornel.gpslocation.entity.Watch;
import com.guardias.yornel.gpslocation.util.GpsTestListener;
import com.guardias.yornel.gpslocation.util.NetworkUtility;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import static com.guardias.yornel.gpslocation.util.Const.SECONDS_TO_MILLISECONDS;

public class GuardActivity extends BaseActivity implements LocationListener {

    private static final String TAG = "GuardActivity";

    private static GuardActivity sInstance;

    private LocationManager mLocationManager;
    private String mProvider;

    // Listeners for Fragments
    private ArrayList<GpsTestListener> mGpsTestListeners = new ArrayList<>();

    private long minTime; // Min Time between location updates, in milliseconds
    private float minDistance; // Min Distance between location updates, in meters

    boolean mStarted;
    boolean locating;

    static GuardActivity getInstance() {
        return sInstance;
    }

    GuardPagerAdapter mSectionsPagerAdapter;

    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guard);

        mViewPager = (ViewPager) findViewById(R.id.container);

        sInstance = this;

        getProvider();
        if (mProvider == null) {
            Log.e(TAG, "Unable to get GPS_PROVIDER");
            Toast.makeText(this, getString(R.string.gps_not_supported),
                    Toast.LENGTH_SHORT).show();
            goLoginActivity();
            return;
        } else {
            Log.i(TAG, "PROVIDER: "+mProvider);
        }
        checkWatch();
    }

    private void getProvider() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (NetworkUtility.isNetworkAvailable(this)) {
            Log.i(TAG, "*********NETWORK SELECTED PROVIDER*********");
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            mProvider = mLocationManager.getBestProvider(criteria, true);
        } else {
            Log.i(TAG, "*********AUTO SELECTED GPS LIKE PROVIDER*********");
            mProvider = mLocationManager.getProvider(LocationManager.GPS_PROVIDER).getName();
        }
    }

    public void checkWatch() {
        Watch watch = preferences.getWatch();
        User guard = preferences.getUser();
        if (watch != null &&
                watch.getUser().getDni().equals(guard.getDni())) { // if is same guard
            continueCreate();
        } else if (watch == null) {                            // if guard don't has previous watch
            createNewWatch();
        } else if (watch != null &&
                !watch.getUser().getDni().equals(guard.getDni())) {    // if is other guard
            warningDialog();
        }
    }

    public void warningDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(R.string.warning)
                .setMessage(R.string.warning_watch)
                .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        createNewWatch();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(GuardActivity.this, "no es el mismo guardia", Toast.LENGTH_LONG).show();
                        goLoginActivity();
                    }
                }).show();
    }

    private void createNewWatch() {
        Watch watch = new Watch();
        watch.setStartTime(System.currentTimeMillis());
        watch.setUser(preferences.getUser());
        preferences.clearWatchs();
        preferences.save(watch);
        Snackbar.make(mViewPager, getString(R.string.init_new_watch), Snackbar.LENGTH_LONG).show();
        continueCreate();
    }


    public void continueCreate() {

        initMapFragment();

        double tempMinTime = 1;

        minTime = (long) (tempMinTime * SECONDS_TO_MILLISECONDS);
        minDistance = (float) 0;

        gpsStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            promptEnableGps();
        }

        checkTimeAndDistance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.finish:
                finishDialog(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void finishDialog(View view) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setMessage(R.string.finish_watch_text_dialog)
                .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finishWatch();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // nothing to do
                    }
                }).show();
    }

    public void finishWatch() {
        Watch watchPre = preferences.getWatch();

        User user = DataHelper.getUser(watchPre.getUser().getDni());
        if (user == null) {
            user = watchPre.getUser();
            user.save();
        }

        realm.beginTransaction();
        Watch watch = realm.createObject(Watch.class);
        watch.setUser(DataHelper.getUser(user.getDni()));
        watch.setStartTime(watchPre.getStartTime());
        watch.setEndTime(System.currentTimeMillis());
        realm.commitTransaction();

        ArrayList<Position> positionsList = preferences.getPositions();

        if (positionsList != null) {
            for (Position position : positionsList) {
                position.setWatch(watch);
                position.setControlPosition(DataHelper
                        .getControlPositionByLat(position.getControlPosition().getLatitude(),
                                position.getControlPosition().getLongitude()));
            }
            for (Position position : positionsList) {
                position.save();
            }
        }
        preferences.clearAll();
        finish();
        startActivity(new Intent(this, LoginActivity.class));
        Toast.makeText(this, R.string.watch_finish, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 1) {
            mViewPager.setCurrentItem(0);
        } else {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setMessage(R.string.close_user)
                    .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            goLoginActivity();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // nothing to do
                        }
                    }).show();
        }
    }

    private void checkTimeAndDistance() {

        double tempMinTimeDouble = 1d;
        long minTimeLong = (long) (tempMinTimeDouble * SECONDS_TO_MILLISECONDS);

        if (minTime != minTimeLong ||
                minDistance != (float) 0) {
            // User changed preference values, get the new ones
            minTime = minTimeLong;
            minDistance = (float) 0;
            // If the GPS is started, reset the location listener with the new values
            if (mStarted) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    return;
                }
                mLocationManager
                        .requestLocationUpdates(mProvider, minTime, minDistance, this);
                Toast.makeText(this, String.format(getString(R.string.gps_set_location_listener),
                        String.valueOf(tempMinTimeDouble), String.valueOf(minDistance)),
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private synchronized void gpsStart() {
        if (!mStarted) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            mLocationManager
                    .requestLocationUpdates(mProvider, minTime, minDistance, this);

            mStarted = true;

            // Show Toast only if the user has set minTime or minDistance to something other than default values
            if (minTime != (long) 1 * SECONDS_TO_MILLISECONDS || minDistance != 0) {
                Toast.makeText(this, String.format(getString(R.string.gps_set_location_listener),
                        String.valueOf((double) minTime / SECONDS_TO_MILLISECONDS),
                        String.valueOf(minDistance)), Toast.LENGTH_SHORT).show();
            }

            // Show the indeterminate progress bar on the action bar until first GPS status is shown
            //setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);

            // Reset the options menu to trigger updates to action bar menu items
            invalidateOptionsMenu();
        }
        for (GpsTestListener listener : mGpsTestListeners) {
            listener.gpsStart();
        }
    }

    private void initMapFragment() {

        mSectionsPagerAdapter = new GuardPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onLocationChanged(Location location) {

        invalidateOptionsMenu();

        for (GpsTestListener listener : mGpsTestListeners) {
            listener.onLocationChanged(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        for (GpsTestListener listener : mGpsTestListeners) {
            listener.onStatusChanged(provider, status, extras);
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        for (GpsTestListener listener : mGpsTestListeners) {
            listener.onProviderEnabled(provider);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        for (GpsTestListener listener : mGpsTestListeners) {
            listener.onProviderDisabled(provider);
        }
    }

    void addListener(GpsTestListener listener) {
        mGpsTestListeners.add(listener);
    }

    @Override
    protected void onDestroy() {
        mLocationManager.removeUpdates(this);
        super.onDestroy();
    }

    /**
     * Ask the user if they want to enable GPS
     */
    private void promptEnableGps() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.enable_gps_message))
                .setPositiveButton(getString(R.string.enable_gps_positive_button),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        }
                )
                .setNegativeButton(getString(R.string.enable_gps_negative_button),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }
                )
                .show();
    }

    public void search(View view) {
        if (locating) {
            invertLocating();
            for (GpsTestListener listener : mGpsTestListeners) {
                listener.onStopRegister();
            }
            return;
        }

        // todo start call

        for (GpsTestListener listener : mGpsTestListeners) {
            listener.onStartRegister();
        }
        invertLocating();
    }


    public void updateMapView(View view) {
        for (GpsTestListener listener : mGpsTestListeners) {
            listener.onUpdateMapView();
        }
    }

    public boolean getLocating() {
        return locating;
    }

    public void invertLocating() {
        locating = !locating;
    }

}
