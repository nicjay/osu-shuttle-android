package com.jordann.maptest;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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


    public MapInfoWindowAdapter(final Context context) {
        mMapState = MapState.get();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        shuttleView = inflater.inflate(R.layout.info_shuttle_new, null);


        RelativeLayout.LayoutParams newParams = (RelativeLayout.LayoutParams)shuttleView.findViewById(R.id.min1).getLayoutParams();
        newParams.addRule(RelativeLayout.ALIGN_BASELINE, R.id.info_shuttle_time1);
        shuttleView.findViewById(R.id.min1).setLayoutParams(newParams);

        newParams = (RelativeLayout.LayoutParams)shuttleView.findViewById(R.id.min2).getLayoutParams();
        newParams.addRule(RelativeLayout.ALIGN_BASELINE,R.id.info_shuttle_time2);
        shuttleView.findViewById(R.id.min2).setLayoutParams(newParams);

        newParams = (RelativeLayout.LayoutParams)shuttleView.findViewById(R.id.min3).getLayoutParams();
        newParams.addRule(RelativeLayout.ALIGN_BASELINE,R.id.info_shuttle_time3);
        shuttleView.findViewById(R.id.min3).setLayoutParams(newParams);

        TextView tv = (TextView)shuttleView.findViewById(R.id.info_shuttle_title);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Gudea-Bold.ttf");
        tv.setTypeface(typeface);

    }

    @Override
    public View getInfoWindow(final Marker marker) {
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


    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }


}
