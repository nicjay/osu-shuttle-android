package com.jordann.maptest;

import android.app.ProgressDialog;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Random;

/*
  Created by jordan_n on 8/21/2014.
*/
public class ShuttleUpdater {
    private static final String TAG = "ShuttleUpdater";

    private static ShuttleUpdater sShuttleUpdater;
    private static MapState sMapState;
    private OnMapStateUpdate listener;
    private pollNewDataTask currentTask;


    private Handler mHandler;
    private boolean isBusy = false;
    private boolean stop = true;
    private Runnable r;
    private static final long ASYNC_DELAY = 5000;
    private String urlShuttlePoints = "http://portal.campusops.oregonstate.edu/files/shuttle/GetMapVehiclePoints.txt";

    private static final int NORTH_ROUTE_ID = 3;
    private static final int WEST_ROUTE_ID = 2;
    private static final int EAST_ROUTE_ID = 1;


    public interface OnMapStateUpdate{
        void onPostShuttleRequest(boolean success);
    }


    private ShuttleUpdater(OnMapStateUpdate listener) {
        super();
        this.listener = listener;
        sMapState = MapState.get();
        mHandler = new Handler();
    }

    public static ShuttleUpdater get(OnMapStateUpdate listener){
        if(sShuttleUpdater == null){
            sShuttleUpdater = new ShuttleUpdater(listener);
        }
        return sShuttleUpdater;

    }

    private void startHandler(){
        r = new Runnable() {
            @Override
            public void run() {
                if(!isBusy){
                    currentTask = new pollNewDataTask(urlShuttlePoints);
                    currentTask.execute();
                }
                if(!stop) startHandler();
            }
        };
        mHandler.postDelayed(r, ASYNC_DELAY);

    }

    public void stopShuttleUpdater(){
        stop = true;
        currentTask.cancel(true);
        mHandler.removeCallbacks(r);
    }

    public void startShuttleUpdater(){

        if (stop == true) {
            stop = false;
            currentTask = new pollNewDataTask(urlShuttlePoints);
            currentTask.execute();

            startHandler();
        }
    }

    private class pollNewDataTask extends AsyncTask<String, Void, Boolean>{

        private static final String TAG = "pollNewDataTask";

        private String url;
        private JSONArray JSONShuttles;

        public pollNewDataTask(String url) {
            super();
            this.url = url;
        }


        @Override
        protected Boolean doInBackground(String... params) {
            JSONGetter getter = new JSONGetter();
            JSONShuttles = getter.getJSONFromUrl(url);
            if(JSONShuttles != null){
                return true;
            }
            return false;
        }


        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            parseJSON();
            if(!stop) listener.onPostShuttleRequest(success);
        }

        //TODO: reduce number of string conversions here and JSONGetter
       private void parseJSON(){
            Gson gson = new Gson();
            boolean[] onlineStates = {false,false,false,false};
            if(JSONShuttles != null) {
                for (int i = 0; i < JSONShuttles.length(); i++) {

                    String json = null;
                    try {
                        JSONObject rawJson = JSONShuttles.getJSONObject(i);
                        json = rawJson.toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Shuttle shuttle = gson.fromJson(json, Shuttle.class);
                    shuttle.setOnline(true);

                    switch (shuttle.getRouteID()) {
                        case NORTH_ROUTE_ID:
                            sMapState.setShuttle(0, shuttle);
                            onlineStates[0] = true;
                            break;
                        case WEST_ROUTE_ID:  //Double route
                            if (sMapState.getShuttles().get(1).getVehicleId() == shuttle.getVehicleId()) {
                                sMapState.setShuttle(1, shuttle);
                                onlineStates[1] = true;
                            } else if (sMapState.getShuttles().get(2).getVehicleId() == shuttle.getVehicleId()) {
                                sMapState.setShuttle(2, shuttle);
                                onlineStates[2] = true;
                            } else if (!sMapState.getShuttles().get(1).isOnline()) {
                                sMapState.setShuttle(1, shuttle);
                                onlineStates[1] = true;
                            } else if (!sMapState.getShuttles().get(2).isOnline()) {
                                sMapState.setShuttle(2, shuttle);
                                onlineStates[2] = true;
                            }
                            break;
                        case EAST_ROUTE_ID:
                            sMapState.setShuttle(3, shuttle);
                            onlineStates[3] = true;
                            break;
                    }
                }
            }
            for (int i = 0; i < 4; i++) {
               Shuttle shuttle = sMapState.getShuttles().get(i);
               //Log.d(TAG, "SET SHUTTLE OFFLINE " + shuttle.isOnline());
               if (!onlineStates[i]){
                   shuttle.setOnline(false);

               }
               else shuttle.setOnline(true);
            }


            //Test Times
           if (sMapState.getStops() != null) {
               ArrayList<Stop> stops = sMapState.getStops();
               Random random = new Random();
               int maxNum = 600;
               for (int i = 0, len = stops.size(); i < len; i++) {
                   stops.get(i).setShuttleETAs(new int[]{random.nextInt(maxNum), random.nextInt(maxNum), random.nextInt(maxNum), random.nextInt(maxNum)});

               }
           }
          /* stops = sMapState.getStops();
           for(int i = 0; i < stops.size(); i++) {
               //Log.d(TAG, "1stopShuttleETAS: " + stops.get(i).getShuttleETAs()[0] + " , " + stops.get(i).getShuttleETAs()[1] + " , " + stops.get(i).getShuttleETAs()[2] + " , " + stops.get(i).getShuttleETAs()[3] + " , ");
           }*/
       }
    }
}