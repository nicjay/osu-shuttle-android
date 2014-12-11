package edu.oregonstate.beaverbus;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

/*
  Created by jordan_n on 8/22/2014.
 */
public class Stop {
    private static final String TAG = "Stop";

    private LatLng mLatLng;
    private String mName;
    private int[] mShuttleETAs = new int[4];
    private ArrayList<Integer> mServicedRoutes;
    private ArrayList<Integer> mStopIds;


    private Marker mMarker;

    public Stop(LatLng latLng, String name, int routeId, int stopId, int[] shuttleETAs) {
        mLatLng = latLng;
        mName = name;
        mShuttleETAs = shuttleETAs;
        mServicedRoutes = new ArrayList<Integer>();
        mServicedRoutes.add(routeId);
        mStopIds = new ArrayList<Integer>();
        mStopIds.add(stopId);
    }

    public Marker getMarker() {
        return mMarker;
    }

    public void setMarker(Marker marker) {
        mMarker = marker;
    }

    public int[] getShuttleETAs() {
        return mShuttleETAs;
    }

    public int getShuttleETA(int index) {
        if (index < mShuttleETAs.length)
            return mShuttleETAs[index];
        else return 0;
    }

    public void setShuttleETAs(int[] shuttleETAs) {
        mShuttleETAs = shuttleETAs;
    }

    public void invalidateShuttleETAs() {
        for (int i = 0; i < mShuttleETAs.length; i++) {
            mShuttleETAs[i] = -1;
        }
    }

    public void setShuttleETA(int index, int ETA) {
        mShuttleETAs[index] = ETA;
    }

    public LatLng getLatLng() {
        return mLatLng;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setLatLng(LatLng latLng) {
        mLatLng = latLng;
    }

    public void addServicedRoute(int routeId) {
        mServicedRoutes.add(routeId);
    }

    public void addStopId(int stopId) {
        mStopIds.add(stopId);
    }

    public boolean areLatLngEqual(LatLng latLng) {
        if (mLatLng.longitude == latLng.longitude && mLatLng.latitude == latLng.latitude)
            return true;
        return false;
    }
}
