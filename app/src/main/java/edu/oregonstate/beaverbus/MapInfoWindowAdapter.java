package edu.oregonstate.beaverbus;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.internal.ge;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;

/*
  Created by sellersk on 8/26/2014.
 */
public class MapInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final static String TAG = "MapInfoWindowAdapter";

    private static MapState sMapState;
    private final Context mContext;
    private final LayoutInflater inflater;
    
    private ArrayList<Shuttle> shuttles;

    public MapInfoWindowAdapter(final Context context) {
        sMapState = MapState.get();
        mContext = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View getInfoWindow(final Marker marker) {
        shuttles = sMapState.getShuttles();
        for (Stop stop : sMapState.getStops()){
            if (stop.getMarker().equals(marker)){
                Context c = sMapState.getCurrentContext();

                boolean shuttleShown = false;

                View stopView = inflater.inflate(R.layout.info_stop_new, null);
                LinearLayout container = (LinearLayout)stopView.findViewById(R.id.eta_container);

                int[] shuttleEtas = stop.getShuttleETAs();
                for(int i = 0; i < shuttleEtas.length; i++){
                    int eta = shuttleEtas[i];
                    if(eta != -1){
                        shuttleShown = true;
                        eta = eta / 60;
                        if(eta == 0) eta = 1;
                        View stopSection = inflater.inflate(R.layout.info_stop_section, null);
                        View square = stopSection.findViewById(R.id.info_stop_square);
                        TextView etaText = (TextView)stopSection.findViewById(R.id.info_stop_eta);

                        switch (i){
                            case 0:
                                square.setBackgroundColor(c.getResources().getColor(R.color.shuttle_green));
                                break;
                            case 1:
                                square.setBackgroundColor(c.getResources().getColor(R.color.shuttle_orange));
                                break;
                            case 2:
                                square.setBackgroundColor(c.getResources().getColor(R.color.shuttle_orange));
                                break;
                            case 3:
                                square.setBackgroundColor(c.getResources().getColor(R.color.shuttle_purple));
                                break;
                        }
                        etaText.setText(""+eta);
                        container.addView(stopSection);
                        Log.d(TAG, "~! This is the width: " + container.getWidth());
                    }
                }
                if(!shuttleShown){
                    TextView textView = new TextView(c);
                    textView.setText("Offline");
                    container.addView(textView);
                }

                ImageView imageView = new ImageView(mContext);
                imageView.setImageResource(R.drawable.infowindow_bottom);



                return stopView;

            }
        }

        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
