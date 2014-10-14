package edu.oregonstate.beaverbus;

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

    private static MapState sMapState;
    private DrawerLayout mDrawerLayout;
    private ExpandableListView mDrawerList;

    private static final int NORTH = 6, WEST = 7, EAST = 8;

    public DrawerItemClickListener(DrawerLayout layout, ExpandableListView listView) {
        super();
        sMapState = MapState.get();
        mDrawerLayout = layout;
        mDrawerList = listView;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

        selectItemGroup(groupPosition);
        Log.d(TAG, "DrawableStates: " + v.getDrawableState().length);
        return false;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        selectItemChild(groupPosition, childPosition);
        return false;
    }


    private void selectItemGroup(int groupPosition) {
        Log.d(TAG, "selectItemGroup");
        ArrayList<DrawerItem> drawerItems = sMapState.getDrawerItems();

        if(drawerItems.get(groupPosition).getTypeId() == 1){
            sMapState.animateMap(drawerItems.get(groupPosition).getShuttle().getLatLng());
            Log.d(TAG, "The marker is: " + drawerItems.get(groupPosition).getShuttle().getMarker());
            //drawerItems.get(groupPosition).getShuttle().getMarker().showInfoWindow();
            //sMapState.setSelectedStopMarker(drawerItems.get(groupPosition).getShuttle().getMarker());
            if(sMapState.getSelectedStopMarker() != null) {
                if (!sMapState.isStopsVisible()){
                    sMapState.getSelectedStopMarker().setVisible(false);
                }
                sMapState.getSelectedStopMarker().hideInfoWindow();
                sMapState.setSelectedStopMarker(null, false);
            }
            mDrawerLayout.closeDrawer(mDrawerList);
        }

    }

    private void selectItemChild(int groupPosition, int childPosition){
        Log.d(TAG, "selectItemChild");
        ArrayList<DrawerItem> drawerItems = sMapState.getDrawerItems();
        ArrayList<Stop> stops = sMapState.getStops();



        if (sMapState.getSelectedStopMarker() != null && !sMapState.isStopsVisible()){
            sMapState.setSelectedStopMarkerVisibility(false);
        }

        Marker marker;
        Log.d(TAG, "!@ groupPos " + groupPosition + " childPos: " + childPosition + ":"+ sMapState.getNorthMap().get(childPosition+1));

        switch (groupPosition) {
            case NORTH:
                marker = sMapState.getNorthMap().get(childPosition+1).getMarker();

                break;
            case WEST:
                marker = sMapState.getWestMap().get(childPosition+1).getMarker();

                break;
            case EAST:
                if (childPosition == 7){
                    Log.d(TAG, "muhah");
                }
                marker = sMapState.getEastMap().get(childPosition+1).getMarker();

                break;
            default:
                marker = null;
        }
        sMapState.animateMap(marker.getPosition());
        sMapState.setSelectedStopMarker(marker, true);
        marker.setVisible(true);
        marker.showInfoWindow();

        /*
        int index = drawerItems.get(groupPosition).getStopsIndex().get(childPosition);
        sMapState.animateMap(stops.get(index).getLatLng());

        sMapState.setSelectedStopMarker(stops.get(index).getMarker());

        stops.get(index).getMarker().setVisible(true);
        stops.get(index).getMarker().showInfoWindow();

         */


        mDrawerLayout.closeDrawer(mDrawerList);
    }
}