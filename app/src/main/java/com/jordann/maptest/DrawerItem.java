package com.jordann.maptest;

import android.media.Image;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by jordan_n on 8/22/2014.
 */
public class DrawerItem {
    private static final String TAG = "DrawerItem";

    private int mTypeId;

    private ShuttleMarker mMarker;
    private String mTitle;

    private Image mImage;

    private LatLng mLatLng;

    //Section constructor
    public DrawerItem(int typeId, String title) {
        mTypeId = typeId;
        mTitle = title;
    }

    //Row constructor
    public DrawerItem(int typeId, String title, ShuttleMarker marker) {
        mTypeId = typeId;
        mTitle = title;
        mMarker = marker;
        mLatLng = new LatLng(marker.getMarker().getPosition().latitude, marker.getMarker().getPosition().longitude);
    }

    //Image constructor
    public DrawerItem(int typeId, Image image, String title) {
        mTypeId = typeId;
        mImage = image;
        mTitle = title;
    }

    public int getTypeId() {
        return mTypeId;
    }

    public ShuttleMarker getMarker() {
        return mMarker;
    }

    public void setMarker(ShuttleMarker marker) {
        mMarker = marker;
    }

    public String getTitle() {
        return mTitle;
    }


    public Image getImage() {
        return mImage;
    }

    public LatLng getLatLng() {
        return mLatLng;
    }
}
