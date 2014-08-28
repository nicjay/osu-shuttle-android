package com.jordann.maptest;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/*
  Created by jordan_n on 8/22/2014.
 */
public class Shuttle {
    private double Latitude;
    private double Longitude;
    private int Heading;
    private double GroundSpeed;
    private String Name;
    private int VehicleId;
    private int RouteID;
    private int IsOnRoute;
    private int Seconds;
    private String TimeStamp;

    private Marker mMarker;
    private boolean isOnline;

    public Shuttle(String name, boolean isOnline) {
        this.isOnline = isOnline;
        this.Name = name;
    }

    public void updateAll(Shuttle shuttle){
        Latitude = shuttle.getLatitude();
        Longitude = shuttle.getLongitude();
        Heading = shuttle.getHeading();
        GroundSpeed = shuttle.getGroundSpeed();
        VehicleId = shuttle.getVehicleId();
        RouteID = shuttle.getRouteID();
        IsOnRoute = shuttle.getIsOnRoute();
        Seconds = shuttle.getSeconds();
        TimeStamp = shuttle.getTimeStamp();
        isOnline = shuttle.isOnline();
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public Marker getMarker() {
        return mMarker;
    }

    public void setMarker(Marker marker) {
        mMarker = marker;
    }

    public void updateMarker(){
        mMarker.setPosition(getLatLng());
    }

    public int getRouteID() {
        return RouteID;
    }

    public LatLng getLatLng(){
        return new LatLng(Latitude, Longitude);
    }

    public double getLatitude() {
        return Latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public int getHeading() {
        return Heading;
    }

    public double getGroundSpeed() {
        return GroundSpeed;
    }

    public String getName() {
        return Name;
    }

    public int getVehicleId() {
        return VehicleId;
    }

    public int getSeconds() {
        return Seconds;
    }

    public String getTimeStamp() {
        return TimeStamp;
    }

    public int getIsOnRoute() {
        return IsOnRoute;
    }
}
