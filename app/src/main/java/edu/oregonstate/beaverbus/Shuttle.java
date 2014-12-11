package edu.oregonstate.beaverbus;

import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/*
  Created by jordan_n on 8/22/2014.
 */
public class Shuttle {
    public final String TAG = "Shuttle";

    public static class ShuttleEta {
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

    private Marker mMarker;
    private boolean isOnline;
    private static MapState sMapState;


    public Shuttle(String name, boolean isOnline) {
        this.isOnline = isOnline;
        this.Name = name;
        sMapState = MapState.get();
    }

    public void updateAll(Shuttle shuttle) {
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

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean newOnline) {
        if (isOnline && !newOnline) {
            sMapState.setDrawerShuttleStatus(this, false);
        } else if (!isOnline && newOnline) {
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
    public void updateMarker(boolean animate) {

        final LatLng startLatLng = mMarker.getPosition();
        final LatLng endLatLng = getLatLng();

        final long duration = 1000;
        final long start = SystemClock.uptimeMillis();

        final Handler handler = new Handler();
        final Interpolator interpolator = new LinearInterpolator();

        if (GroundSpeed != 0) {
            switch (RouteID) {
                case 7:
                    mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bus_green_marker));
                    break;
                case 9:
                    mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bus_orange_marker));
                    break;
                case 8:
                    mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bus_purple_marker));
                    break;
            }
        }

        //Place the marker at interval points every X milliseconds, for smooth movement effect
        if (!startLatLng.equals(new LatLng(0, 0)) && animate) {
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
                        if (GroundSpeed == 0) {
                            switch (RouteID) {
                                case 7:
                                    mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bus_green_square));
                                    break;
                                case 9:
                                    mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bus_orange_square));
                                    break;
                                case 8:
                                    mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bus_purple_square));
                                    break;
                            }
                            Heading = 0;
                            mMarker.setRotation(Heading);
                        }// animation ended
                    }
                }
            });
        } else {
            mMarker.setPosition(endLatLng);
        }
        if (Heading != 0 && GroundSpeed != 0) {
            mMarker.setRotation(Heading);
        }
    }

    public void updateMarkerWithoutAnim() {
        mMarker.setPosition(getLatLng());
        mMarker.setRotation(getHeading());
    }

    public int getRouteID() {
        return RouteID;
    }

    public LatLng getLatLng() {
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
