package com.jordann.maptest;

import android.media.Image;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by jordan_n on 8/22/2014.
 */
public class DrawerItem {
    private static final String TAG = "DrawerItem";

    private int mTypeId;

    private Marker mMarker;
    private String mTitle;

    private Image mImage;

    //Section constructor
    public DrawerItem(int typeId, String title) {
        mTypeId = typeId;
        mTitle = title;
    }

    //Row constructor
    public DrawerItem(int typeId, String title, Marker marker) {
        mTypeId = typeId;
        mTitle = title;
        mMarker = marker;
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

    public Marker getMarker() {
        return mMarker;
    }

    public String getTitle() {
        return mTitle;
    }

    public Image getImage() {
        return mImage;
    }

    /*
        switch(drawerItemID)
            case 0:     //Section header
                convertView = inflator.inflate(R.layout.drawer_section, parent, false);
                var.addOnClickListerner(new OnCLickLister{
                    toggleActive();
                }


            case 1:    //Map Item
                convertView = inflator.inflate(R.layout.drawer_item, parent, false);
                var.addOnclickListener( new OnClickListener{
                    Map.ZoomTo(drawerItem[position].getMarker)
                }
            case 2:    //Extra Settings
                convertView = inflator.inflate(R.layout.extra_settings, parent, false);
                var.addOnclickListener( new OnClickListener{
                    startActivity();
                }

        }

         */
}
