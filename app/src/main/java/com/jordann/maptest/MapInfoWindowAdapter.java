package com.jordann.maptest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/*
  Created by sellersk on 8/26/2014.
 */
public class MapInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final static String TAG = "MapInfoWindowAdapter";

    private static MapState mMapState;
    private final View shuttleView;
    //private final View stopView;


    public MapInfoWindowAdapter(Context context) {
        mMapState = MapState.get();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        shuttleView = inflater.inflate(R.layout.info_shuttle, null);
        shuttleView.setOnClickListener(null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        for (Shuttle shuttle : mMapState.getShuttles()){
            if (shuttle.getMarker().equals(marker)){
                ((TextView)shuttleView.findViewById(R.id.info_shuttle_title)).setText(shuttle.getName());
                return shuttleView;
            }
        }

        for (Stop stop : mMapState.getStops()){
            if (stop.getMarker().equals(marker)){
                //TODO: reference new layout for stop
                ((TextView)shuttleView.findViewById(R.id.info_shuttle_title)).setText(stop.getName());
                return shuttleView;
            }
        }

        return null;
    }
}
