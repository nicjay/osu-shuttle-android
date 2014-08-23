package com.jordann.maptest;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by jordan_n on 8/22/2014.
 */
public class DrawerAdapter extends ArrayAdapter<DrawerItem> {
    private static final String TAG = "DrawerAdapter";

    private final Context mContext;
    private ArrayList<DrawerItem> mDrawerItems;

    public DrawerAdapter(Context context, ArrayList<DrawerItem> objects) {
        super(context, 0, objects);
        mContext = context;
        mDrawerItems = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            DrawerItem drawerItem = mDrawerItems.get(position);
            switch (drawerItem.getTypeId()){
                case 0:
                    convertView = inflater.inflate(R.layout.drawer_section_header, parent, false);
                    ((TextView)convertView.findViewById(R.id.drawer_section_header_title)).setText(drawerItem.getTitle());
                    break;
                case 1:
                    convertView = inflater.inflate(R.layout.drawer_list_item, parent, false);
                    ((TextView)convertView.findViewById(R.id.drawer_list_item_title)).setText(drawerItem.getTitle());
                    break;
                case 2:


                    break;
                default:
                    Log.d(TAG, "----DEFAULT CASE---");

            }
        }
        return convertView;
    }
}
