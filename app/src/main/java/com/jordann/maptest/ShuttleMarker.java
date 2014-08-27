package com.jordann.maptest;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by sellersk on 8/25/2014.
 */
public class ShuttleMarker {
    private Marker mMarker;
    private int mVehicleId;

    private int mTypeId;

    public ShuttleMarker(Marker marker, int vehicleId){
        mMarker = marker;
        mVehicleId = vehicleId;
    }

    public Marker getMarker() {
        return mMarker;
    }

    public void setMarker(Marker marker) {
        mMarker = marker;
    }

    public void updateMarker(LatLng position){
        mMarker.setPosition(position);
    }

    public int getVehicleId() {
        return mVehicleId;
    }

    public void setVehicleId(int vehicleId) {
        mVehicleId = vehicleId;
    }
}
