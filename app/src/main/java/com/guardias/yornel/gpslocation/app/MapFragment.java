package com.guardias.yornel.gpslocation.app;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.guardias.yornel.gpslocation.R;
import com.guardias.yornel.gpslocation.db.DataHelper;
import com.guardias.yornel.gpslocation.entity.ControlPosition;
import com.guardias.yornel.gpslocation.entity.Position;
import com.guardias.yornel.gpslocation.util.AppPreferences;
import com.guardias.yornel.gpslocation.util.GpsTestListener;
import com.guardias.yornel.gpslocation.util.SingleShotLocationProvider;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import io.realm.RealmResults;

import static com.guardias.yornel.gpslocation.util.Const.CAMERA_ANCHOR_ZOOM;
import static com.guardias.yornel.gpslocation.util.Const.CAMERA_INITIAL_ZOOM;
import static com.guardias.yornel.gpslocation.util.Const.MOVE_MAP_INTERACTION_THRESHOLD;

public class MapFragment extends Fragment implements OnMapReadyCallback, GpsTestListener,
        LocationSource, GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLoadedCallback {

    private long mFixTime;
    SimpleDateFormat mDateFormat = new SimpleDateFormat("hh:mm:ss a");

    Bundle mSavedInstanceState;

    private MapView mMapView;
    private GoogleMap mMap;
    private LatLng mLatLng;

    private boolean timeLoading;
    private boolean mGotFix;

    private TextView mLatitudeView;
    private TextView mLongitudeView;
    private TextView mFixTimeView;

    private TextView namePosition;
    private TextView distancePosition;
    private View containerDetails;
    private View layoutContent;
    private View layoutSearching;
    private CircularProgressButton circularButton;

    private AppPreferences preferences;
    private GuardActivity guardActivity;

    private OnLocationChangedListener mListener; //Used to update the map with new location

    ArrayList<Marker> markers;
    RealmResults<ControlPosition> positions;

    private long mLastMapTouchTime = 0;

    @Override
    public void onLocationChanged(Location loc) {
        if (mListener != null) {
            mListener.onLocationChanged(loc);
        }

        mLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());

        if (mMap != null) {
            //Get bounds for detection of real-time location within bounds
            LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            if (!mGotFix /*&&
                    (!bounds.contains(mLatLng) ||
                            mMap.getCameraPosition().zoom < (mMap.getMaxZoomLevel() / 2))*/) {

                if (markers == null)
                    checkMarkers(false);
                updateCameraFromMyPosition();
            }
            if (timeLoading)
                enableRegister();
            mGotFix = true;
        }
        setToWindows(loc);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        guardActivity = (GuardActivity) getActivity();

        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        layoutContent = rootView.findViewById(R.id.layout_main_content);
        layoutSearching = rootView.findViewById(R.id.layout_main_searching);
        containerDetails = rootView.findViewById(R.id.container_position_details);

        mLatitudeView = (TextView) rootView.findViewById(R.id.latitude);
        mLongitudeView = (TextView) rootView.findViewById(R.id.longitude);
        mFixTimeView = (TextView) rootView.findViewById(R.id.fix_time);

        namePosition = (TextView) rootView.findViewById(R.id.name_position);
        distancePosition = (TextView) rootView.findViewById(R.id.distance_position);
        circularButton = (CircularProgressButton) rootView.findViewById(R.id.circularButton1);
        circularButton.setIndeterminateProgressMode(true);
        circularButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (circularButton.getProgress() == 0) {
                    circularButton.setProgress(50);
                    saveActualPosition();
                    //getPosition();
                } else if (circularButton.getProgress() == 100
                        || circularButton.getProgress() == -1 ) {
                    circularButton.setProgress(0);
                } else {
                    circularButton.setProgress(100);
                }
            }
        });

        preferences = new AppPreferences(getActivity());

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isGoogleMapsInstalled()) {
            // Save the savedInstanceState
            mSavedInstanceState = savedInstanceState;
            // Register for an async callback when the map is ready
            mMapView.getMapAsync(this);
        }

        disableRegister();

        return rootView;
    }

    public void saveActualPosition() {
        ArrayList<Marker> nearMarkers = getInMeters();
        if (nearMarkers.isEmpty()) {
            Toast.makeText(getActivity(), "Ningun marcador cerca", Toast.LENGTH_SHORT).show();
            circularButton.setProgress(-1);
        } else if (nearMarkers.size() == 1) {
            ControlPosition controlPosition = ((ControlPosition) nearMarkers.get(0).getTag());
            Position position = new Position();
            position.setControlPosition(controlPosition);
            position.setLatitude(mLatLng.latitude);
            position.setLongitude(mLatLng.longitude);
            position.setTime(System.currentTimeMillis());
            position.setUpdateTime(System.currentTimeMillis());
            preferences.save(position);
            checkMarkers(false);
            Snackbar.make(layoutContent, R.string.position_saved, Snackbar.LENGTH_LONG).show();
            circularButton.setProgress(100);
        } else if (nearMarkers.size() > 1) {
            Toast.makeText(getActivity(), "mas de 1 marcador cercano", Toast.LENGTH_SHORT).show();
            circularButton.setProgress(-1);
        }
        updateButtonTemporary();
    }

    void setToWindows(Location location) {
        mLatitudeView.setText(getString(R.string.gps_latitude_value, location.getLatitude()));
        mLongitudeView.setText(getString(R.string.gps_longitude_value, location.getLongitude()));
        mFixTime = location.getTime();
        updateFixTime();
    }

    void getPosition() {
        ((GuardActivity) getActivity()).invertLocating();
        SingleShotLocationProvider.requestSingleUpdate(getActivity(),
                new SingleShotLocationProvider.LocationCallback() {
                    @Override public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                        if (((GuardActivity) getActivity()).getLocating()) {
                            Toast.makeText(getActivity(), "Latitud: "
                                    +String.valueOf(location.getLatitude())+" \nLongitud: "
                                    +String.valueOf(location.getLongitude()), Toast.LENGTH_SHORT).show();
                            circularButton.setProgress(100);
                            ((GuardActivity) getActivity()).invertLocating();
                            updateButtonTemporary();
                        }
                    }
                });
    }

    void disableRegister() {
        layoutContent.setVisibility(View.GONE);
        layoutSearching.setVisibility(View.VISIBLE);
        circularButton.setEnabled(false);
        startTimer();
    }

    void enableRegister() {
        layoutContent.setVisibility(View.VISIBLE);
        layoutSearching.setVisibility(View.GONE);
        circularButton.setEnabled(true);
        timeLoading = false;
    }

    public boolean isGoogleMapsInstalled() {
        try {
            ApplicationInfo info = getActivity().getPackageManager()
                    .getApplicationInfo("com.google.android.apps.maps", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Show the location on the map
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setOnMapLoadedCallback(this);
        mMap.setMyLocationEnabled(true);
        //Set location source
        mMap.setLocationSource(this);
        // Listener for map / My Location button clicks, to disengage map camera control
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMyLocationButtonClickListener(this);

        mMap.setOnMarkerClickListener(this);

        GuardActivity.getInstance().addListener(this);

    }

    public void checkMarkers(boolean updateViewMap) {
        addMarkers();
        addMarkersPosition();
        if (!markers.isEmpty() && updateViewMap)
            updateCameraFromAllPoints();
    }

    ArrayList<Marker> getInMeters() {

        int meters = 5;
        System.out.println("cantidad de markers "+markers.size());

        ArrayList<Marker> nearMarkers = new ArrayList<>();
        for (Marker marker : markers) {
            ControlPosition position = (ControlPosition) marker.getTag();

            Location loc1 = new Location("");
            loc1.setLatitude(position.getLatitude());
            loc1.setLongitude(position.getLongitude());

            Location loc2 = new Location("");
            loc2.setLatitude(mLatLng.latitude);
            loc2.setLongitude(mLatLng.longitude);

            Float distanceInMeters = loc1.distanceTo(loc2);

            System.out.println(distanceInMeters.intValue()+" metros");
            System.out.println("para "+position.getPlaceName());
            if (distanceInMeters.intValue() <= meters) {
                nearMarkers.add(marker);
            }
        }
        return nearMarkers;
    }

    public  void addMarkers() {
        positions = DataHelper.getAllControlPositions();

        int accuracyStrokeColor = Color.argb(255, 204, 0, 0);
        int accuracyFillColor = Color.argb(50, 204, 0, 0);

        markers = new ArrayList<>();

        mMap.clear();

        for (ControlPosition position: positions) {
            Marker markerPosition = mMap.addMarker(new MarkerOptions()
                    .anchor(0.5f,0.5f)
                .position(new LatLng(position.getLatitude(), position.getLongitude())));
            markerPosition.setTag(guardActivity.realm.copyFromRealm(position));
            markerPosition.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.red_ball_16));
            markers.add(markerPosition);

            CircleOptions accuracyCircleOptions = new CircleOptions()
                    .center(new LatLng(position.getLatitude(), position.getLongitude()))
                    .radius(6l)
                    .fillColor(accuracyFillColor)
                    .strokeColor(accuracyStrokeColor)
                    .strokeWidth(2.0f);
            mMap.addCircle(accuracyCircleOptions);
        }
    }

    public  void addMarkersPosition() {

        if (preferences.getPositions() != null)
            for (Position position: preferences.getPositions()) {
                Marker markerPosition = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(position.getLatitude(), position.getLongitude())));
                //markerPosition.setTag(guardActivity.realm.copyFromRealm(position).getControlPosition());
                markerPosition.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.flag_blue_32));
            }
    }

    public void updateCameraFromAllPoints() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker m : markers) {
            builder.include(m.getPosition());
        }
        if (mLatLng != null)
            builder.include(mLatLng);
        LatLngBounds bounds = builder.build();
        int padding = ((480 * 10) / 100); // offset from edges of the map
        // in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,
                padding);
        mMap.animateCamera(cu);
    }

    public void updateCameraFromMyPosition() {
        if (mMap == null || mLatLng == null) {
            return;
        }
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mLatLng)
                .zoom(CAMERA_ANCHOR_ZOOM)
                //.bearing(CAMERA_INITIAL_BEARING)
                //.tilt(CAMERA_INITIAL_TILT)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void gpsStart() {
        mGotFix = false;
    }

    @Override
    public void gpsStop() {

    }

    @Override
    public void onGpsStatusChanged(int event, GpsStatus status) {
    }

    @Override
    public void onGnssFirstFix(int ttffMillis) {

    }

    @Override
    public void onSatelliteStatusChanged(GnssStatus status) {

    }

    @Override
    public void onGnssStarted() {

    }

    @Override
    public void onGnssStopped() {

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

    }

    @Override
    public void onStopRegister() {

    }

    @Override
    public void onUpdateMapView() {
        if (markers != null) {
            updateCameraFromAllPoints();
        }
    }

    private void updateFixTime() {
        if (mFixTime == 0 || !GuardActivity.getInstance().mStarted) {
            mFixTimeView.setText("");
        } else {
            mFixTimeView.setText(mDateFormat.format(mFixTime));
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
        Toast.makeText(getActivity(), "provider enable", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(getActivity(), "provider disable", Toast.LENGTH_SHORT).show();
        disableRegister();
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
    }

    @Override
    public void deactivate() {
        mListener = null;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        containerDetails.setVisibility(View.GONE);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        containerDetails.setVisibility(View.GONE);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        mLastMapTouchTime = System.currentTimeMillis();
        // Return false, so button still functions as normal
        if (mLatLng != null) {
            updateCameraFromMyPosition();
            return true;
        }
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Retrieve the data from the marker.
        ControlPosition position = (ControlPosition) marker.getTag();

        // Check if a click count was set, then display the click count.
        if (position != null && mLatLng != null) {
            containerDetails.setVisibility(View.VISIBLE);
            namePosition.setText(position.getPlaceName());

            Location loc1 = new Location("");
            loc1.setLatitude(position.getLatitude());
            loc1.setLongitude(position.getLongitude());

            Location loc2 = new Location("");
            loc2.setLatitude(mLatLng.latitude);
            loc2.setLongitude(mLatLng.longitude);

            Float distanceInMeters = loc1.distanceTo(loc2);

            distancePosition.setText("a "+distanceInMeters.intValue()+" metros de distancia");
        }
        return false;
    }

    public void startTimer() {

        timeLoading = true;

        final int millis = 500;

        Thread splashTimer = new Thread(){
            public void run(){
                try{
                    int splashTime = 0;
                    while(timeLoading){

                        sleep(millis);

                        splashTime += millis;

                        switch (splashTime) {
                            case millis*1:
                                setText(".");
                                break;
                            case millis*2:
                                setText("..");
                                break;
                            case millis*3:
                                setText("...");
                                break;
                            case millis*4:
                                setText("....");
                                splashTime = 0;
                                break;
                        }
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        };
        splashTimer.start();
    }

    private void setText(final CharSequence text) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) getActivity().findViewById(R.id.loading_text)).setText(text);
                }
            });
        } catch (Exception e) {}

    }

    public void updateButtonTemporary() {

        final int millis = 1500;

        Thread splashTimer = new Thread(){
            public void run(){
                try{
                    sleep(millis);
                    updateButton();
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        };
        splashTimer.start();
    }

    private void updateButton() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (circularButton.getProgress() == 100
                        || circularButton.getProgress() == -1) {
                    circularButton.setProgress(0);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onMapLoaded() {
        checkMarkers(true);
    }
}
