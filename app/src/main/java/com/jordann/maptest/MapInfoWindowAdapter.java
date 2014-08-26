package com.jordann.maptest;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by sellersk on 8/26/2014.
 */
public class MapInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final static String TAG = "MapInfoWindowAdapter";

    private final View shuttleView;
    //private final View stopView;

    private final MapState mMapState;

    public MapInfoWindowAdapter(Context context) {
        mMapState = MapState.get();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        shuttleView = inflater.inflate(R.layout.info_shuttle, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        ShuttleMarker shuttleMarker = mMapState.getShuttleMarkerOfMarker(marker);

        Log.d(TAG, "ShuttleView : " + shuttleView);
        String text = String.valueOf(shuttleMarker.getVehicleId());
        ((TextView)shuttleView.findViewById(R.id.info_shuttle_time1)).setText(text);



        return shuttleView;
    }
}
