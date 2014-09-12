package com.jordann.maptest;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/*
  Created by jordan_n on 8/22/2014.
 */
public class Stop {
    private double mLatitude;
    private double mLongitude;
    private String mName;
    private int[] mShuttleETAs = new int[4];

    private Marker mMarker;

    public Stop(double latitude, double longitude, String name, int[] shuttleETAs){
        mLatitude = latitude;
        mLongitude = longitude;
        mName = name;
        mShuttleETAs = shuttleETAs;
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

    public int getShuttleETA(int index){
        if (index < mShuttleETAs.length)
            return mShuttleETAs[index];
        else return 0;
    }

    public void setShuttleETAs(int[] shuttleETAs) {
        mShuttleETAs = shuttleETAs;
    }

    public void setShuttleETA(int index, int ETA){
        mShuttleETAs[index] = ETA;
    }

    public LatLng getLatLng(){
        return new LatLng(mLatitude, mLongitude);
    }

    public String getName() {
        return mName;
    }

}
