package com.jordann.maptest;

import android.graphics.Camera;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
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
    private static ArrayList<Shuttle> mShuttles;

    private static ArrayList<Stop> mStops;
    private static MapState sMapState;

    private static GoogleMap mMap;
    private static ArrayList<ShuttleMarker> mShuttleMarkerList;

    private static ArrayList<DrawerItem> mDrawerItems;
    private static DrawerAdapter mDrawerAdapter;

    private MapState(){
        mDrawerItems = new ArrayList<DrawerItem>();
        //  mShuttles = new Shuttle[4];
        mShuttles = new ArrayList<Shuttle>();

        mShuttleMarkerList = new ArrayList<ShuttleMarker>();

        mStops = new ArrayList<Stop>();
    }

/*
    public static void initialize(){
        sMapState = null;
        mMap = null;
        mDrawerItems = new ArrayList<DrawerItem>();
        //  mShuttles = new Shuttle[4];
        mShuttles = new ArrayList<Shuttle>();

        mShuttleMarkerList = new ArrayList<ShuttleMarker>();

        mStops = new ArrayList<Stop>();

    }
*/


    public static MapState get(){
        if (sMapState == null){
            sMapState = new MapState();
        }
        return sMapState;
    }


    public void animateMap(LatLng latLng){
        //mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng), 500, null);
        CameraPosition cameraPosition = new CameraPosition(latLng, mMap.getCameraPosition().zoom, 45, 0);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

       // mMap.setMapType(4);
    }

    public void showInfoWindow(int position){
        mDrawerItems.get(position).getMarker().getMarker().showInfoWindow();
    }


    public void addShuttleMarker(LatLng latLng, int vehicleId){

        ShuttleMarker shuttleMarker = new ShuttleMarker(mMap.addMarker(new MarkerOptions().position(latLng).title("booya")), vehicleId);
       // Log.d(TAG, "NEW shuttleMarker : " + shuttleMarker + ". adding to map : " + mMap);
        mShuttleMarkerList.add(shuttleMarker);

        for (DrawerItem drawerItem : mDrawerItems){
            if (drawerItem.getTypeId() != 0) {
                if (vehicleId == drawerItem.getMarker().getVehicleId()) {
                    drawerItem.setMarker(shuttleMarker);
                }
            }
        }

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

    public void clearShuttleMarkerArrayList(){
       // Log.d(TAG, "shuttleMarkerList CLEARED");
        mShuttleMarkerList.clear();
    }

    public ShuttleMarker getShuttleMarkerOfMarker(Marker marker){
        for(ShuttleMarker shuttleMarker : mShuttleMarkerList){
            //Log.d(TAG, "        marker: " + marker + " .equals( shuttleMarker.getMarker : " + shuttleMarker.getMarker() + ")");
            if(marker.equals(shuttleMarker.getMarker())){
                return shuttleMarker;
            }
        }
        return null;
    }

    public GoogleMap getMap() {
        return mMap;
    }

    public void setMap(GoogleMap map) {
        mMap = map;
    }

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
        mDrawerItems.clear();
        //"Shuttles" section header
        mDrawerItems.add(new DrawerItem(0, "Shuttles"));
        //Shuttle items
        int i;
        for(i = 0; i < mShuttles.size(); i ++){
            mDrawerItems.add(new DrawerItem(1, mShuttles.get(i).getName(), mShuttleMarkerList.get(i)));
        }

        //"Stops" section header
        mDrawerItems.add(new DrawerItem(0, "Stops"));

        //Stop items   //TODO Add Stop Items
        for(i = 0; i < mStops.size(); i++){
            //   drawerItems.add(new DrawerItem(2, "Test Stop#" + i, shuttleMarkers[i%2]));
        }

    }

    public void removeDrawerItem(int vehicleId){
        for(DrawerItem drawerItem : mDrawerItems){
            if(drawerItem.getTypeId() != 0) {
                if (drawerItem.getMarker().getVehicleId() == vehicleId) {
                    mDrawerItems.remove(drawerItem);
                    break;
                }
            }
        }

    }


}
