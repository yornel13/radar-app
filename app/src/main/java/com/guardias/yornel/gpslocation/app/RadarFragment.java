package com.guardias.yornel.gpslocation.app;


import android.annotation.SuppressLint;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.guardias.yornel.gpslocation.R;
import com.guardias.yornel.gpslocation.util.GpsTestListener;
import com.guardias.yornel.gpslocation.util.SingleShotLocationProvider;

import java.text.SimpleDateFormat;

/**
 * Created by Yornel on 14/7/2017.
 */

public class RadarFragment extends Fragment implements GpsTestListener {

    private TextView mLatitudeView;
    private TextView mLongitudeView;
    private TextView mFixTimeView;
    private TextView mAltitudeView;
    private TextView mAccuracyView;
    private TextView mSpeedView;

    private TextView latitudeValueGPS;
    private TextView longitudeValueGPS;

    private View layoutContent;
    private View layoutSearching;

    private Button button;

    private boolean mNavigating, mGotFix;
    private long mFixTime;

    SimpleDateFormat mDateFormat = new SimpleDateFormat("hh:mm:ss a");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_radar, container,
                false);

        mLatitudeView = (TextView) v.findViewById(R.id.latitude);
        mLongitudeView = (TextView) v.findViewById(R.id.longitude);
        mFixTimeView = (TextView) v.findViewById(R.id.fix_time);
        mAltitudeView = (TextView) v.findViewById(R.id.altitude);
        mAccuracyView = (TextView) v.findViewById(R.id.accuracy);
        mSpeedView = (TextView) v.findViewById(R.id.speed);

        layoutContent = v.findViewById(R.id.layout_main_content);
        layoutSearching = v.findViewById(R.id.layout_main_searching);

        mLatitudeView.setText("");
        mLongitudeView.setText("");

        disableRegister();

        GuardActivity.getInstance().addListener(this);

        return v;
    }

    void disableRegister() {
        layoutContent.setVisibility(View.GONE);
        layoutSearching.setVisibility(View.VISIBLE);
        button.setBackground(getResources()
                .getDrawable(R.drawable.button_effect_press));
        button.setEnabled(false);
    }

    void enableRegister() {
        layoutContent.setVisibility(View.VISIBLE);
        layoutSearching.setVisibility(View.GONE);
        button.setBackground(getResources()
                .getDrawable(R.drawable.button_effect));
        button.setEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        GuardActivity gta = GuardActivity.getInstance();
        setStarted(gta.mStarted);
    }

    private void setStarted(boolean navigating) {
        if (navigating != mNavigating) {
            if (navigating) {

            } else {
                disableRegister();

                mLatitudeView.setText("");
                mLongitudeView.setText("");
                mFixTime = 0;
                updateFixTime();
                mAltitudeView.setText("");
                mAccuracyView.setText("");
                mSpeedView.setText("");
            }
            mNavigating = navigating;
        }
    }

    private void updateFixTime() {
        if (mFixTime == 0 || !GuardActivity.getInstance().mStarted) {
            mFixTimeView.setText("");
        } else {
            mFixTimeView.setText(mDateFormat.format(mFixTime));
        }
    }

    @SuppressLint("NewApi")
    public void gpsStart() {
        //Reset flag for detecting first fix
        mGotFix = false;
    }

    @Override
    public void gpsStop() {
    }

    @Override
    public void onGpsStatusChanged(int event, GpsStatus status) {
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                setStarted(true);
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                setStarted(false);
                break;

            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;

            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                //updateLegacyStatus(status);
                break;
        }
    }

    @Override
    public void onGnssFirstFix(int ttffMillis) {

    }

    @Override
    public void onSatelliteStatusChanged(GnssStatus status) {

    }

    @Override
    public void onGnssStarted() {
        setStarted(true);
    }

    @Override
    public void onGnssStopped() {
        setStarted(false);
    }

    @Override
    public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {

    }

    @Override
    public void onOrientationChanged(double orientation, double tilt) {

    }

    @Override
    public void onNmeaMessage(String message, long timestamp) {

    }

    @Override
    public void onStartRegister() {
        buttonToCancel();

        longitudeValueGPS.setText("0.0000");
        latitudeValueGPS.setText("0.0000");

        SingleShotLocationProvider.requestSingleUpdate(getActivity(),
                new SingleShotLocationProvider.LocationCallback() {
                    @Override public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                        if (((GuardActivity) getActivity()).getLocating()) {
                            longitudeValueGPS.setText(String.valueOf(location.getLongitude()));
                            latitudeValueGPS.setText(String.valueOf(location.getLatitude()));
                            button.setBackground(getResources().getDrawable(R.drawable.button_effect));
                            button.setText(R.string.register);
                            ((GuardActivity) getActivity()).invertLocating();
                        }
                    }
                });
    }

    @Override
    public void onStopRegister() {
        buttonToRegister();
    }

    @Override
    public void onUpdateMapView() {


    }

    void buttonToCancel() {
        button.setBackground(getResources()
                .getDrawable(R.drawable.button_effect_press));
        button.setText(R.string.cancel);
    }

    void buttonToRegister() {
        button.setBackground(getResources()
                .getDrawable(R.drawable.button_effect));
        button.setText(R.string.register);
    }

    @Override
    public void onLocationChanged(Location location) {

        enableRegister();

        mLatitudeView.setText(getString(R.string.gps_latitude_value, location.getLatitude()));
        mLongitudeView.setText(getString(R.string.gps_longitude_value, location.getLongitude()));
        mFixTime = location.getTime();
        if (location.hasAltitude()) {
            mAltitudeView.setText(getString(R.string.gps_altitude_value, location.getAltitude()));
        } else {
            mAltitudeView.setText("");
        }
        if (location.hasAccuracy()) {
            mAccuracyView.setText(getString(R.string.gps_accuracy_value, location.getAccuracy()));
        } else {
            mAccuracyView.setText("");
        }
        if (location.hasSpeed()) {
            mSpeedView.setText(getString(R.string.gps_speed_value, location.getSpeed()));
        } else {
            mSpeedView.setText("");
        }
        updateFixTime();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
