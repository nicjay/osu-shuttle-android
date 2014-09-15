package edu.oregonstate.beaverbus;

import android.os.SystemClock;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import android.view.animation.Interpolator;
import android.os.Handler;

import java.util.LinkedList;

/*
  Created by jordan_n on 8/22/2014.
 */
public class Shuttle {
    public static class ShuttleEta
    {
        String name;
        int time;
    }

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
    private int colorID;

    private Marker mMarker;
    private boolean isOnline;

    private LinkedList<ShuttleEta> upcomingStops;

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

    public LinkedList<ShuttleEta> getUpcomingStops() {
        return upcomingStops;
    }

    public void setUpcomingStops(LinkedList<ShuttleEta> upcomingStops) {
        this.upcomingStops = upcomingStops;
    }

    public int getColorID() {
        return colorID;
    }

    public void setColorID(int colorID) {
        this.colorID = colorID;
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

    //Controls the animation, repositioning and rotation of shuttle marker
    public void updateMarker(){
        final LatLng startLatLng = mMarker.getPosition();
        final LatLng endLatLng = getLatLng();

        final long duration = 1000;
        final long start = SystemClock.uptimeMillis();

        final Handler handler = new Handler();
        final Interpolator interpolator = new LinearInterpolator();

        //Place the marker at interval points every X milliseconds, for smooth movement effect
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * endLatLng.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * endLatLng.latitude + (1 - t) * startLatLng.latitude;
                mMarker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    // Post again 10ms later.
                    handler.postDelayed(this, 10);
                } else {
                    // animation ended
                }
            }
        });

       // mMarker.setPosition(getLatLng());
        mMarker.setRotation(getHeading());
    }

    public void updateMarkerWithoutAnim(){
        mMarker.setPosition(getLatLng());
        mMarker.setRotation(getHeading());
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
