package edu.oregonstate.beaverbus;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import android.view.animation.Interpolator;
import android.os.Handler;

import java.util.LinkedList;

/*
  Created by jordan_n on 8/22/2014.
 */
public class Shuttle {
    public final String TAG = "Shuttle";

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
    private int VehicleID;
    private int RouteID;
    private boolean IsOnRoute;
    private int Seconds;
    private String TimeStamp;
    private int colorID;

    private Marker mMarker;
    private boolean isOnline;
    private static MapState sMapState;

    private LinkedList<ShuttleEta> upcomingStops;

    public Shuttle(String name, boolean isOnline) {
        this.isOnline = isOnline;
        this.Name = name;
        sMapState = MapState.get();
    }

    public void updateAll(Shuttle shuttle){
        Latitude = shuttle.getLatitude();
        Longitude = shuttle.getLongitude();
        Heading = shuttle.getHeading();
        GroundSpeed = shuttle.getGroundSpeed();
        VehicleID = shuttle.getVehicleId();
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

    public void setOnline(boolean newOnline) {
        if (isOnline && !newOnline){
            sMapState.setDrawerShuttleStatus(this, false);
        }else if(!isOnline && newOnline){
            sMapState.setDrawerShuttleStatus(this, true);
        }
        this.isOnline = newOnline;
    }

    public Marker getMarker() {
        return mMarker;
    }

    public void setMarker(Marker marker) {
        mMarker = marker;
    }

    //Controls the animation, repositioning and rotation of shuttle marker
    public void updateMarker(boolean animate){
        final GoogleMap map = sMapState.getMap();

        final LatLng startLatLng = mMarker.getPosition();

        final LatLng endLatLng = getLatLng();

        final float startHeading = mMarker.getRotation();
        final float endHeading = (float)getHeading();

        Log.d(TAG, "!@ Starting position: " + startLatLng.latitude + " / " + startLatLng.longitude + "\n Marker: " + getName() + " end:  "+ endLatLng.latitude + " / " + endLatLng.longitude);
        long value = 1000;
        if (!mMarker.isVisible()){
            Log.d(TAG, "OH! marker not visible");
            mMarker.setVisible(true);
            value = 0;
        }
        final long duration = value;
        final long start = SystemClock.uptimeMillis();

        final Handler handler = new Handler();
        final Interpolator interpolator = new LinearInterpolator();

        //Place the marker at interval points every X milliseconds, for smooth movement effect
        if(!startLatLng.equals(new LatLng(0, 0)) && animate){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed / duration);
                    double lng = t * endLatLng.longitude + (1 - t) * startLatLng.longitude;
                    double lat = t * endLatLng.latitude + (1 - t) * startLatLng.latitude;
                    mMarker.setPosition(new LatLng(lat, lng));

                    float newRotation = t * endHeading + (1 - t) * startHeading;
                    //mMarker.setRotation(newRotation);

                    //Make the camera follow along if shuttle is selected, with correct offset
//                    if(sMapState.getSelectedStopMarker() != null) {
//                        if (sMapState.getSelectedStopMarker().equals(mMarker)) {
//                            Display display = ((WindowManager) sMapState.getCurrentContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//
//                            LatLng newPosition = new LatLng(mMarker.getPosition().latitude, mMarker.getPosition().longitude);
//
//                            CameraPosition cameraPosition = new CameraPosition(newPosition, map.getCameraPosition().zoom, map.getCameraPosition().tilt, map.getCameraPosition().bearing);
//                            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//                        }
//                    }
                    if (t < 1.0) {
                        // Post again 10ms later.
                        handler.postDelayed(this, 10);
                    } else {
                        // animation ended
                    }
                }
            });
        }else{
            mMarker.setPosition(endLatLng);
        }
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
        return VehicleID;
    }

    public int getSeconds() {
        return Seconds;
    }

    public String getTimeStamp() {
        return TimeStamp;
    }

    public boolean getIsOnRoute() {
        return IsOnRoute;
    }
}
