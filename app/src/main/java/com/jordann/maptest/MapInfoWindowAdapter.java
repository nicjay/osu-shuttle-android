package com.jordann.maptest;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.w3c.dom.Text;

import java.util.ResourceBundle;

/*
  Created by sellersk on 8/26/2014.
 */
public class MapInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final static String TAG = "MapInfoWindowAdapter";

    private static MapState mMapState;
    private final Context mContext;
    private final LayoutInflater inflater;

    private final View shuttleView;
    //private final GridLayout gridLayout;
    //private final View stopView;


   // private final View stopSegmentView;
   // private final View stopSegmentView2;
   // private final View stopSegmentView3;

    private LinearLayout stopLayout;

    //private final ImageView shuttleIcon;
    private final TextView shuttleTitle;
    //private final TextView stopTitle;

    //private final TextView stopRouteName;
    //private final TextView stopETA;

    //private final TextView eta;
    //private final View stopView;


    public MapInfoWindowAdapter(final Context context) {
        mMapState = MapState.get();
        mContext = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        shuttleView = inflater.inflate(R.layout.info_shuttle_new, null);
       // stopView = inflater.inflate(R.layout.info_stop, null);
        //gridLayout = (GridLayout)stopView.findViewById(R.id.eta_container);


     /*   stopSegmentView = inflater.inflate(R.layout.info_stop_section, null);
        stopSegmentView2 = inflater.inflate(R.layout.info_stop_section, null);
        stopSegmentView3 = inflater.inflate(R.layout.info_stop_section, null);

        //stopLayout = (LinearLayout)stopView.findViewById(R.id.eta_container);
        stopRouteName = (TextView)stopSegmentView.findViewById(R.id.info_stop_route_name);
        stopETA = (TextView)stopSegmentView.findViewById(R.id.info_stop_eta);
*/

        shuttleTitle = (TextView)shuttleView.findViewById(R.id.info_shuttle_title);
        //stopTitle = (TextView)stopView.findViewById(R.id.info_stop_title);
        //shuttleIcon = (ImageView)shuttleView.findViewById(R.id.info_shuttle_icon);

        RelativeLayout.LayoutParams newParams = (RelativeLayout.LayoutParams)shuttleView.findViewById(R.id.min1).getLayoutParams();
        newParams.addRule(RelativeLayout.ALIGN_BASELINE, R.id.info_shuttle_time1);
        shuttleView.findViewById(R.id.min1).setLayoutParams(newParams);

        newParams = (RelativeLayout.LayoutParams)shuttleView.findViewById(R.id.min2).getLayoutParams();
        newParams.addRule(RelativeLayout.ALIGN_BASELINE,R.id.info_shuttle_time2);
        shuttleView.findViewById(R.id.min2).setLayoutParams(newParams);

        newParams = (RelativeLayout.LayoutParams)shuttleView.findViewById(R.id.min3).getLayoutParams();
        newParams.addRule(RelativeLayout.ALIGN_BASELINE,R.id.info_shuttle_time3);
        shuttleView.findViewById(R.id.min3).setLayoutParams(newParams);

        /*TextView tv = (TextView)shuttleView.findViewById(R.id.info_shuttle_title);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Gudea-Bold.ttf");
        tv.setTypeface(typeface);*/



    }

    @Override
    public View getInfoWindow(final Marker marker) {

        for (Shuttle shuttle : mMapState.getShuttles()){
            if (shuttle.getMarker().equals(marker)){
                shuttleTitle.setText(shuttle.getName());

                return shuttleView;
            }
        }




        for (Stop stop : mMapState.getStops()){
            if (stop.getMarker().equals(marker)){
                //TODO: reference new layout for stop
                View stopView = inflater.inflate(R.layout.info_stop, null);
                //LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)stopView.getLayoutParams();

                TextView stopTitle = (TextView)stopView.findViewById(R.id.info_stop_title);
                stopTitle.setText(marker.getTitle());

                LinearLayout containerLayout = (LinearLayout)stopView.findViewById(R.id.eta_container);

                Context c = containerLayout.getContext();

                LinearLayout.LayoutParams lps = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);

                for (int i = 0; i < 3; i++){
                    LinearLayout ll = new LinearLayout(c);
                    ll.setOrientation(LinearLayout.VERTICAL);
                   // ll.setGravity(Gravity.CENTER);

                    TextView tv = new TextView(c);
                    tv.setText("East");
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                    tv.setGravity(Gravity.CENTER);
                    ll.addView(tv);

                    TextView tv2 = new TextView(c);
                    tv2.setText(String.valueOf(i));
                    tv2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
                    tv2.setTextColor(c.getResources().getColor(R.color.OSU_orange));
                    tv2.setTypeface(null, Typeface.BOLD);
                    tv2.setGravity(Gravity.CENTER);
                    ll.addView(tv2);

                    TextView tv3 = new TextView(c);
                    tv3.setText("min");
                    tv3.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                    tv3.setGravity(Gravity.CENTER);
                    ll.addView(tv3);

                    ll.setLayoutParams(lps);
                    containerLayout.addView(ll);

                }



/*
                TextView title = ((TextView)stopView.findViewById(R.id.info_stop_title));
                title.setText(marker.getTitle());

                LinearLayout linearLayout = (LinearLayout)stopView.findViewById(R.id.info_stop_linear_layout);

                Log.d(TAG, "width : " + linearLayout.getWidth());

                LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);

                LinearLayout newLinearLayout = new LinearLayout(mContext);
                newLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                newLinearLayout.setWeightSum(5f);
                newLinearLayout.setLayoutParams(LLParams);

                TextView textView = new TextView(mContext);
                textView.setLayoutParams(new TableLayout.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, 1f));
                textView.setText("TEST");
                newLinearLayout.addView(textView);

                TextView textView1 = new TextView(mContext);
                textView1.setLayoutParams(new TableLayout.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, 3f));
                textView1.setText("TEST1");
                newLinearLayout.addView(textView1);

                TextView textView2 = new TextView(mContext);
                textView2.setLayoutParams(new TableLayout.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, 1f));
                textView2.setText("TEST2");
                newLinearLayout.addView(textView2);

                linearLayout.addView(newLinearLayout);
*/
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
