package com.jordann.maptest;

import android.media.Image;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

/**
 * Created by jordan_n on 8/22/2014.
 */
public class DrawerItem {
    private static final String TAG = "DrawerItem";

    private int mTypeId;

    private Shuttle mShuttle;

    private String mTitle;

    private Image mImage;

     private ArrayList<Integer> mStopsIndex;

    //Section constructor
    public DrawerItem(int typeId, String title) {
        mTypeId = typeId;
        mTitle = title;
    }

    //Shuttle Row constructor
    public DrawerItem(int typeId, String title, Shuttle shuttle) {
        mTypeId = typeId;
        mTitle = title;
        mShuttle = shuttle;
       // mLatLng = new LatLng(shuttle.getMarker().getPosition().latitude, shuttle.getMarker().getPosition().longitude);
    }

    //Route Row constructor
    public DrawerItem(int typeId, String title, ArrayList<Integer> stopsIndex) {
        mTypeId = typeId;
        mTitle = title;
        mStopsIndex = stopsIndex;
        //mLatLng = new LatLng(stop.getMarker().getPosition().latitude, stop.getMarker().getPosition().longitude);
    }


    /*
    //Image constructor
    public DrawerItem(int typeId, Image image, String title) {
        mTypeId = typeId;
        mImage = image;
        mTitle = title;
    }
    */

    public int getTypeId() {
        return mTypeId;
    }

    public Shuttle getShuttle() {
        return mShuttle;
    }

    public void setShuttle(Shuttle shuttle) {
        mShuttle = shuttle;
    }

    public ArrayList<Integer> getStopsIndex() {
        return mStopsIndex;
    }

    public void setStopsIndex(ArrayList<Integer> stopsIndex) {
        mStopsIndex = stopsIndex;
    }

    public String getTitle() {
        return mTitle;
    }


    public Image getImage() {
        return mImage;
    }

  /*  public LatLng getLatLng() {
        return mLatLng;
    }
    */
}
