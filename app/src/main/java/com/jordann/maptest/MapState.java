package com.jordann.maptest;

import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by jordan_n on 8/22/2014.
 */
public class MapState {
    private static final String TAG = "MapState";
    //private static Shuttle[] mShuttles;
    private ArrayList<Shuttle> mShuttles;

    private ArrayList<Stop> mStops;
    private static MapState sMapState;

    private GoogleMap mMap;
    private ArrayList<ShuttleMarker> mShuttleMarkerList;

    private ArrayList<DrawerItem> mDrawerItems;

    private MapState(){

        mDrawerItems = new ArrayList<DrawerItem>();
      //  mShuttles = new Shuttle[4];
        mShuttles = new ArrayList<Shuttle>();

        mShuttleMarkerList = new ArrayList<ShuttleMarker>();

        mStops = new ArrayList<Stop>();
    }

    public static MapState get(){
        if (sMapState == null){
            sMapState = new MapState();
        }
        return sMapState;
    }


    public void animateMap(LatLng latLng){
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    public void showInfoWindow(int position){
        mDrawerItems.get(position).getMarker().getMarker().showInfoWindow();
    }


    public void addShuttleMarker(LatLng latLng, int vehicleId){
        mShuttleMarkerList.add(new ShuttleMarker(mMap.addMarker(new MarkerOptions().position(latLng).title("booya")), vehicleId));
    }

    public void updateShuttleMarker(int position, LatLng latLng){
        mShuttleMarkerList.get(position).updateMarker(latLng);
    }

    public void removeShuttleMarker(ShuttleMarker marker){
        mShuttleMarkerList.remove(mShuttleMarkerList.indexOf(marker));
    }

    public ArrayList<ShuttleMarker> getShuttleMarkerList() {
        return mShuttleMarkerList;
    }

    public GoogleMap getMap() {
        return mMap;
    }

    public void setMap(GoogleMap map) {
        mMap = map;
    }


/*
    public Shuttle[] getShuttles() {
        return mShuttles;
    }


    public void setShuttle(int index, Shuttle shuttle) {
        mShuttles[index] = shuttle;
    }
    */


    public ArrayList<Shuttle> getShuttles() {
        return mShuttles;
    }

    public void setShuttles(ArrayList<Shuttle> shuttles) {
        mShuttles = shuttles;
    }

    public ArrayList<Stop> getStops() {
        return mStops;
    }

    public void setStops(ArrayList<Stop> stops) {
        mStops = stops;
    }

    public ArrayList<DrawerItem> getDrawerItems() {
        return mDrawerItems;
    }

    public void setDrawerItems() {
        //"Shuttles" section header
        mDrawerItems.add(new DrawerItem(0, "Shuttles"));



        //Shuttle items
        int i;
        for(i = 0; i < 4; i ++){
            mDrawerItems.add(new DrawerItem(1, mShuttles.get(i).getName(), mShuttleMarkerList.get(i)));
        }

        //"Stops" section header
        mDrawerItems.add(new DrawerItem(0, "Stops"));

        //Stop items   //TODO Add Stop Items
        for(i = 0; i < 8; i++){
            //   drawerItems.add(new DrawerItem(2, "Test Stop#" + i, shuttleMarkers[i%2]));
        }

    }

    public void removeDrawerItem(int vehicleId){
        for(DrawerItem drawerItem : mDrawerItems){
            if(drawerItem.getTypeId() != 0) {
                if (drawerItem.getMarker().getVehicleId() == vehicleId) {
                    Log.d(TAG, "Removing drawerItem: " + drawerItem.getTitle());
                    mDrawerItems.remove(drawerItem);
                    break;
                }
            }
        }

    }

    public void addDrawerItem(){
        double a = Math.random()%10;
        Random random = new Random();
        int b = random.nextInt(100);

        String str = String.valueOf(b);


        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("booya"));
        ShuttleMarker shuttleMarker = new ShuttleMarker(marker, b);

        mShuttleMarkerList.add(shuttleMarker);
        DrawerItem drawerItem = new DrawerItem(1, str, shuttleMarker);
        mDrawerItems.add(drawerItem);
        Log.d(TAG, "Adding drawerItem: " + drawerItem.getTitle());
    }
}
