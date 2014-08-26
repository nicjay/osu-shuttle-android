package com.jordann.maptest;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by jordan_n on 8/22/2014.
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

    public int getRouteID() {
        return RouteID;
    }

    public void setRouteID(int routeID) {
        RouteID = routeID;
    }

    public LatLng getLatLng(){
        return new LatLng(Latitude, Longitude);
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public int getHeading() {
        return Heading;
    }

    public void setHeading(int heading) {
        Heading = heading;
    }

    public double getGroundSpeed() {
        return GroundSpeed;
    }

    public void setGroundSpeed(double groundSpeed) {
        GroundSpeed = groundSpeed;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getVehicleId() {
        return VehicleId;
    }

    public void setVehicleId(int vehicleId) {
        VehicleId = vehicleId;
    }

    public int isOnRoute() {
        return IsOnRoute;
    }

    public void setOnRoute(int isOnRoute) {
        IsOnRoute = isOnRoute;
    }

    public int getSeconds() {
        return Seconds;
    }

    public void setSeconds(int seconds) {
        Seconds = seconds;
    }

    public String getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        TimeStamp = timeStamp;
    }
}
