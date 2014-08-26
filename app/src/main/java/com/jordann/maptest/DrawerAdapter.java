package com.jordann.maptest;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by jordan_n on 8/22/2014.
 */
public class DrawerAdapter extends ArrayAdapter<DrawerItem> {
    private static final String TAG = "DrawerAdapter";

    private final Context mContext;
    private ArrayList<DrawerItem> mDrawerItems;
    private static MapState sMapState;


    public DrawerAdapter(Context context, ArrayList<DrawerItem> objects) {
        super(context, 0, objects);
        mContext = context;
        mDrawerItems = objects;
        sMapState = MapState.get();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final DrawerItem drawerItem = mDrawerItems.get(position);
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            switch (drawerItem.getTypeId()){
                case 0:
                    convertView = inflater.inflate(R.layout.drawer_section_header, parent, false);
                    ((TextView)convertView.findViewById(R.id.drawer_section_header_title)).setText(drawerItem.getTitle());
                    break;
                case 1:
                    convertView = inflater.inflate(R.layout.drawer_list_item, parent, false);
                    Log.d(TAG, "Old text: " + ((TextView)convertView.findViewById(R.id.drawer_list_item_title)).getText() + "; New text: "+drawerItem.getTitle());
                    ((TextView)convertView.findViewById(R.id.drawer_list_item_title)).setText(drawerItem.getTitle());
                    break;
                case 2:
                    convertView = inflater.inflate(R.layout.drawer_list_item, parent, false);
                    ((TextView)convertView.findViewById(R.id.drawer_list_item_title)).setText(drawerItem.getTitle());
                    break;
                default:
                    Log.d(TAG, "----DEFAULT CASE---");

            }
        }else{
            if(drawerItem.getTypeId() == 1){
                ((TextView)convertView.findViewById(R.id.drawer_list_item_title)).setText(drawerItem.getTitle());
            }

        }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        DrawerItem drawerItem = mDrawerItems.get(position);
        if(drawerItem.getTypeId() == 0){
            return 0;
        }else if(drawerItem.getTypeId() == 1){
            return 1;
        }else{
            return 2;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }


}
