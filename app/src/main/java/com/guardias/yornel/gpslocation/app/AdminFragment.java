package com.guardias.yornel.gpslocation.app;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.guardias.yornel.gpslocation.R;
import com.guardias.yornel.gpslocation.db.DataHelper;
import com.guardias.yornel.gpslocation.entity.ControlPosition;
import com.guardias.yornel.gpslocation.util.AppPreferences;
import com.guardias.yornel.gpslocation.util.GpsTestListener;

import java.util.ArrayList;

import io.realm.RealmResults;

import static com.guardias.yornel.gpslocation.util.Const.CAMERA_ANCHOR_ZOOM;

public class AdminFragment extends Fragment implements OnMapReadyCallback, GpsTestListener,
        LocationSource, GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLoadedCallback, GoogleMap.OnCameraMoveListener {

    private static final int NEW_MARKER = 1;
    private static final int NO_SYNC_MARKER = 2;
    private static final int SYNC_MARKER = 3;

    Bundle mSavedInstanceState;

    private MapView mMapView;
    private GoogleMap mMap;
    private LatLng mLatLng;

    private boolean timeLoading;
    private boolean mGotFix;
    private boolean canMove;

    private EditText place;

    private TextView namePosition;
    private TextView distancePosition;
    private View containerDetails;
    private View layoutContent;
    private View layoutSearching;
    private CircularProgressButton circularButton;

    private Marker mark;
    private Integer EDITION;

    private OnLocationChangedListener mListener; //Used to update the map with new location

    ArrayList<Marker> markers;
    RealmResults<ControlPosition> positions;

    private long mLastMapTouchTime = 0;

    private ImageButton moveButton;
    private ImageButton cancelButton;
    private Button saveButton;
    private Button deleteButton;

    private AppPreferences preferences;
    private AdminActivity adminActivity;

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

                updateCameraFromMyPosition();
            }
            if (timeLoading)
                enableRegister();
            mGotFix = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        adminActivity = (AdminActivity) getActivity();

        View rootView = inflater.inflate(R.layout.fragment_admin, container, false);

        layoutContent = rootView.findViewById(R.id.layout_main_content);
        layoutSearching = rootView.findViewById(R.id.layout_main_searching);
        containerDetails = rootView.findViewById(R.id.container_position_details);

        place = (EditText) rootView.findViewById(R.id.place);

        moveButton = (ImageButton) rootView.findViewById(R.id.move);
        cancelButton = (ImageButton) rootView.findViewById(R.id.cancel);
        saveButton = (Button) rootView.findViewById(R.id.save);
        deleteButton = (Button) rootView.findViewById(R.id.delete);

        namePosition = (TextView) rootView.findViewById(R.id.name_position);
        distancePosition = (TextView) rootView.findViewById(R.id.distance_position);
        circularButton = (CircularProgressButton) rootView.findViewById(R.id.circularButton);
        circularButton.setIndeterminateProgressMode(true);
        circularButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!place.getText().toString().isEmpty()
                        && mark == null) {
                    EDITION = NEW_MARKER;
                    hideKeyboard();
                    ControlPosition position = new ControlPosition(place.getText().toString());
                    mark = mMap.addMarker(new MarkerOptions()
                            .position(mLatLng));
                    mark.setTag(position);
                    mark.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.yellow_marker_32));
                    markers.add(mark);

                    containerDetails.setVisibility(View.VISIBLE);
                }
            }
        });

        moveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!canMove) {
                    mark.setPosition(mMap.getCameraPosition().target);
                    canMove = true;
                    moveButton.setBackground(getResources()
                            .getDrawable(R.drawable.button_effect_press));
                } else {
                    canMove = false;
                    moveButton.setBackground(getResources()
                            .getDrawable(R.drawable.button_effect));
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mark != null && !place.getText().toString().isEmpty()) {
                    String text = place.getText().toString();
                    ControlPosition position = (ControlPosition) mark.getTag();
                    switch (EDITION) {
                        case NEW_MARKER:
                            position = new ControlPosition();
                            position.setPlaceName(text);
                            position.setLatitude(mark.getPosition().latitude);
                            position.setLongitude(mark.getPosition().longitude);
                            position.setActive(true);
                            position.save();
                            containerDetails.setVisibility(View.GONE);
                            canMove = false;
                            moveButton.setBackground(getResources()
                                    .getDrawable(R.drawable.button_effect));
                            place.setText("");
                            mark = null;
                            addMarkers(true);
                            break;
                        case NO_SYNC_MARKER:
                            DataHelper.updateControlPos(mark.getPosition().latitude,
                                    mark.getPosition().longitude, text, position);
                            containerDetails.setVisibility(View.GONE);
                            canMove = false;
                            moveButton.setBackground(getResources()
                                    .getDrawable(R.drawable.button_effect));
                            place.setText("");
                            mark = null;
                            addMarkers(true);
                            break;
                    }
                }

            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (EDITION) {
                    case NEW_MARKER:
                        containerDetails.setVisibility(View.GONE);
                        canMove = false;
                        moveButton.setBackground(getResources()
                                .getDrawable(R.drawable.button_effect));
                        mark.remove();
                        mark = null;
                        break;
                    case NO_SYNC_MARKER:
                        DataHelper.deleteControlPos((ControlPosition) mark.getTag());
                        containerDetails.setVisibility(View.GONE);
                        canMove = false;
                        moveButton.setBackground(getResources()
                                .getDrawable(R.drawable.button_effect));
                        mark.remove();
                        mark = null;
                        addMarkers(false);
                        break;
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                containerDetails.setVisibility(View.GONE);
                canMove = false;
                moveButton.setBackground(getResources()
                        .getDrawable(R.drawable.button_effect));
                mark = null;
                place.setText("");
                addMarkers(false);
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

    public  void addMarkers(Boolean moveCamera) {

        positions = DataHelper.getAllControlPositions();

        markers = new ArrayList<>();

        mMap.clear();

        for (ControlPosition position: positions) {
            Marker markerPosition = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(position.getLatitude(), position.getLongitude())));
            markerPosition.setTag(position);
            if (position.getId() == null)
                markerPosition.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.red_marker_32));
            else {
                markerPosition.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.green_marker_32));
            }
            markers.add(markerPosition);
        }
        if (!markers.isEmpty() && moveCamera)
            updateCameraFromAllPoints();
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
        // Listener for camera changes
        mMap.setOnCameraMoveListener(this);
        // Listener for map / My Location button clicks, to disengage map camera control
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMyLocationButtonClickListener(this);

        mMap.setOnMarkerClickListener(this);

        AdminActivity.getInstance().addListener(this);
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
    public void onStartRegister() {

    }

    @Override
    public void onStopRegister() {

    }

    @Override
    public void onUpdateMapView() {
        if (markers != null && markers.size() > 0) {
            updateCameraFromAllPoints();
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
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
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
        if (position != null && mark == null) {
            if (position.getId() == null) {
                EDITION = NO_SYNC_MARKER;
                mark = marker;
                mark.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.yellow_marker_32));
                containerDetails.setVisibility(View.VISIBLE);
                place.setText(position.getPlaceName());
                return false;
            }
        }
        if (marker != null) {
            return true;
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
                    try {
                        ((TextView) adminActivity.findViewById(R.id.loading_text)).setText(text);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {}
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
        addMarkers(true);
    }

    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(place.getWindowToken(), 0);
    }

    @Override
    public void onCameraMove() {
        if (mark != null && canMove)
            mark.setPosition(mMap.getCameraPosition().target);
    }
}
