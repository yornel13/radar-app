package com.guardias.yornel.gpslocation.app;


import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.guardias.yornel.gpslocation.R;
import com.guardias.yornel.gpslocation.entity.ControlPosition;
import com.guardias.yornel.gpslocation.entity.Position;
import com.guardias.yornel.gpslocation.util.GpsTestListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yornel on 25/7/2017.
 */

public class ListViewFragment extends Fragment implements GpsTestListener, PositionAdapter.OnCardViewClick {

    private RecyclerView mRecyclerView;
    private PositionAdapter adapter;
    private RecyclerView.LayoutManager mLayoutManager;
    public TextView emptyText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_view, container,
                false);

        GuardActivity.getInstance().addListener(this);

        emptyText = (TextView) v.findViewById(R.id.text_empty);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        adapter = new PositionAdapter(new ArrayList<Position>(), this);
        mRecyclerView.setAdapter(adapter);

        return v;
    }

    public void setAdapterPositions(List<Position> positionList) {
        adapter.replaceAll(positionList);
        if (positionList == null || positionList.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
        } else {
            emptyText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void gpsStart() {

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

    }

    @Override
    public void onLocationChanged(Location location) {

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

    @Override
    public void onClick(int position) {
        ControlPosition control = adapter.getPositions().get(position).getControlPosition();
        LatLng latLng = new LatLng(control.getLatitude(), control.getLongitude());
        GuardActivity.getInstance().mViewPager.setCurrentItem(0, true);
        GuardActivity.getInstance().mSectionsPagerAdapter.map.updateCameraFromPosition(latLng);
    }
}
