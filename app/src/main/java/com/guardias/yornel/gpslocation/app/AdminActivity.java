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
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.guardias.yornel.gpslocation.R;
import com.guardias.yornel.gpslocation.util.GpsTestListener;

import java.util.ArrayList;

import static com.guardias.yornel.gpslocation.util.Const.SECONDS_TO_MILLISECONDS;

public class AdminActivity extends BaseActivity implements LocationListener {

    private static final String TAG = "AdminActivity";

    public static final long SPLASH_SCREEN_DELAY = 1500;

    private static AdminActivity sInstance;

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

    static AdminActivity getInstance() {
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

        checkNmeaLog();

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
        AdminFragment fragment = new AdminFragment();
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

}

