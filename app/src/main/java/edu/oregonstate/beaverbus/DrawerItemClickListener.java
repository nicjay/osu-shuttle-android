package edu.oregonstate.beaverbus;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

/*
  Created by sellersk on 8/28/2014.
 */
public class DrawerItemClickListener implements ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {
    private static final String TAG = "DrawerItemClickListener";

    private static MapState sMapState;  //Reference to singleton
    private DrawerLayout mDrawerLayout;
    private ExpandableListView mDrawerList;

    private MapsActivity activity;

    private static final int NORTH = 6, WEST = 7, EAST = 8; //Indices for expandable list headers

    public DrawerItemClickListener(MapsActivity activity, DrawerLayout layout, ExpandableListView listView) {
        super();
        this.activity = activity;
        sMapState = MapState.get();
        mDrawerLayout = layout;
        mDrawerList = listView;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        selectItemGroup(groupPosition); //Call helper function
        return false;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        selectItemChild(groupPosition, childPosition); //Call helper function
        return false;
    }


    private void selectItemGroup(int groupPosition) {
        ArrayList<DrawerItem> drawerItems = sMapState.getDrawerItems();

        if (drawerItems.get(groupPosition).getTypeId() == 1){
            //Jump to shuttle location, hide Nav drawer
            sMapState.animateMap(drawerItems.get(groupPosition).getShuttle().getLatLng());


            //sMapState.getSelectedMarkerManager().animateSelectedStopTitle(drawerItems.get(groupPosition).getShuttle().getName(), true, false, null);
            activity.onMapMarkerClick(drawerItems.get(groupPosition).getShuttle().getMarker());
            mDrawerLayout.closeDrawer(mDrawerList);
        }

    }

    private void selectItemChild(int groupPosition, int childPosition){
        Marker marker;
        switch (groupPosition) {    //Switch to find which stopsIndex map to search in for childPosition
            case NORTH:
                marker = sMapState.getNorthStops().get(childPosition).getMarker();
                break;
            case WEST:
                marker = sMapState.getWestStops().get(childPosition).getMarker();
                break;
            case EAST:
                marker = sMapState.getEastStops().get(childPosition).getMarker();
                break;
            default:
                marker = null;
        }
        if(marker != null) {
            //sMapState.animateMap(marker.getPosition());
            activity.onMapMarkerClick(marker);
            activity.animateSelectedStopTitle(marker.getTitle(), true, false, null, false);
        }

        mDrawerLayout.closeDrawer(mDrawerList);
    }
}