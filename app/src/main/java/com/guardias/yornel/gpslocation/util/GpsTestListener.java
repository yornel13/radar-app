package com.guardias.yornel.gpslocation.util;

import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.LocationListener;

/**
 * Interface used by GpsTestActivity to communicate with Gps*Fragments
 */
public interface GpsTestListener extends LocationListener {

    void gpsStart();

    void gpsStop();

    @Deprecated
    void onGpsStatusChanged(int event, GpsStatus status);

    void onStartRegister();

    void onStopRegister();

    void onUpdateMapView();
}