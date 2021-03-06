package com.guardias.yornel.gpslocation.app;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import static com.guardias.yornel.gpslocation.util.Const.SECONDS_TO_MILLISECONDS;

public class GuardActivity extends BaseActivity implements LocationListener {

    private static final String TAG = "GuardActivity";

    public static final long SPLASH_SCREEN_DELAY = 1500;

    private static GuardActivity sInstance;

    private LocationManager mLocationManager;
    private LocationProvider mProvider;
    private Location mLastLocation;

    private RelativeLayout container;
    private RelativeLayout containerLogin;

    // Listeners for Fragments
    private ArrayList<GpsTestListener> mGpsTestListeners = new ArrayList<>();

    private long minTime; // Min Time between location updates, in milliseconds
    private float minDistance; // Min Distance between location updates, in meters

    boolean mStarted;
    boolean mLogNmea;
    boolean mWriteNmeaTimestampToLog;
    boolean locating;

    static GuardActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        sInstance = this;

        container = (RelativeLayout) findViewById(R.id.container_image);
        containerLogin = (RelativeLayout) findViewById(R.id.container_principal);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mProvider = mLocationManager.getProvider(LocationManager.GPS_PROVIDER);
        if (mProvider == null) {
            Log.e(TAG, "Unable to get GPS_PROVIDER");
            Toast.makeText(this, getString(R.string.gps_not_supported),
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        checkWatch();
    }

    public void checkWatch() {
        Watch watch = preferences.getWatch();
        User guard = preferences.getUser();
        if (watch != null &&
                watch.getUser().getDni().equals(guard.getDni())) { // if is same guard
            continueCreate();
        } else if (watch == null) {                            // if guard don't has previous watch
            watch = new Watch();
            watch.setStartTime(System.currentTimeMillis());
            watch.setUser(guard);
            preferences.clearWatchs();
            preferences.save(watch);
            Snackbar.make(containerLogin, R.string.init_new_watch, Snackbar.LENGTH_LONG).show();
            continueCreate();
        } else if (watch != null &&
                !watch.getUser().getDni().equals(guard.getDni())) {    // if is other guard
            Toast.makeText(this, "no es el mismo guardia", Toast.LENGTH_LONG).show();
                finish();
        }
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

        checkNmeaLog();

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
                finishDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void finishDialog() {
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
        for (Position position: positionsList) {
            position.setWatch(watch);
            position.setControlPosition(DataHelper
                    .getControlPositionByLat(position.getControlPosition().getLatitude(),
                            position.getControlPosition().getLongitude()));
        }
        for (Position position: positionsList) {
            position.save();
        }
        preferences.clearAll();
        finish();
        Toast.makeText(this, "Guardia terminada", Toast.LENGTH_LONG).show();
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
                        .requestLocationUpdates(mProvider.getName(), minTime, minDistance, this);
                Toast.makeText(this, String.format(getString(R.string.gps_set_location_listener),
                        String.valueOf(tempMinTimeDouble), String.valueOf(minDistance)),
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private void checkNmeaLog() {
        mLogNmea = true;
        mWriteNmeaTimestampToLog = true;
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
                    .requestLocationUpdates(mProvider.getName(), minTime, minDistance, this);
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

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        MapFragment fragment = new MapFragment();
        fragmentTransaction.add(R.id.container_principal, fragment);
        fragmentTransaction.commit();

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        // Reset the options menu to trigger updates to action bar menu items
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

    public void san() {
        Watch watch = new Watch();
        watch.setUser(preferences.getUser());
        watch.setStartTime(System.currentTimeMillis());
        watch.save();
        System.out.println("Guardias "+DataHelper.getAllWatches().size());
        System.out.println("Usuarios "+DataHelper.getAllUsers().size());

        List<Watch> watches = DataHelper.getAllWatches();

        System.out.println("guardias array "+new Gson().toJson(realm.copyFromRealm(watches)));
        System.out.println("guardias array "+watches.toString());
        //Snackbar.make(containerLogin, "Esto es otra prueba", Snackbar.LENGTH_LONG).show();

    }

}
