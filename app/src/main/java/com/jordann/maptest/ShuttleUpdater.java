package com.jordann.maptest;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by jordan_n on 8/21/2014.
 */
public class ShuttleUpdater {
    private static final String TAG = "ShuttleUpdater";

    private static ShuttleUpdater sShuttleUpdater;

    private boolean isBusy = false;
    private boolean stop = true;
    private Handler mHandler;

    private Runnable r;
    private long asyncDelay = 5000;

    private String urlShuttlePoints = "http://portal.campusops.oregonstate.edu/files/shuttle/GetMapVehiclePoints.txt";
    private String urlJsonTest = "http://ip.jsontest.com/";
    private MapState mapState;

    private OnMapStateUpdate listener;

    //new pollNewDataTask(urlShuttlePoints)


    public interface OnMapStateUpdate{
        void updateMap();
    }


    private ShuttleUpdater(OnMapStateUpdate listener) {
        this.listener = listener;
        mapState = MapState.get();
        mHandler = new Handler();
       // Log.d(TAG, "ShuttleUpdater CONSTRUCTED");
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
               // Log.d(TAG, "--------RUN---------");
                if(!isBusy){
                    //Log.d(TAG, "callAsyncTask");
                    new pollNewDataTask(urlShuttlePoints).execute();
                }

                if(!stop) startHandler();
            }
        };
        mHandler.postDelayed(r, asyncDelay);

    }

    public void stopShuttleUpdater(){
        stop = true;
        mHandler.removeCallbacks(r);
    }

    public void startShuttleUpdater(){
        stop = false;
        new pollNewDataTask(urlShuttlePoints).execute();
        startHandler();
        //mHandler.postDelayed(r, asyncDelay);
    }

    private class pollNewDataTask extends AsyncTask<String, Void, Void>{

        private String url;

        private JSONArray[] jShuttlesArray;
        private JSONArray[] jStopsArray;


        public pollNewDataTask(String url) {
            super();
            this.url = url;
        }

        @Override
        protected Void doInBackground(String... params) {
            JSONGetter getter = new JSONGetter();

            jShuttlesArray = getter.getJSONFromUrl(url);
           // jStopsArray = getter.getJSONFromUrl(url2);

            //return jShuttlesArray;
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);

            parseJSON();
            listener.updateMap();

            //Log.d(TAG, "RETURNED JsonArray: " + j.toString());
        }


        //TODO: reduce number of string conversions here and JSONGetter
       private void parseJSON(){
            Gson gson = new Gson();

            JSONArray JSONShuttles = jShuttlesArray[0];


            boolean foundFirst = false;
            ArrayList<Shuttle> shuttles = new ArrayList<Shuttle>();
            for (int i=0; i<JSONShuttles.length();i++) {
                String json = null;
                try {
                    JSONObject test = JSONShuttles.getJSONObject(i);
                    json = test.toString();
                    //Log.d(TAG, "json in parse is: "+test);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
             //  mapState.setShuttle(i, gson.fromJson(json, Shuttle.class));

                Shuttle shuttle = gson.fromJson(json, Shuttle.class);
                switch (shuttle.getRouteID()){
                    case 1:
                        shuttle.setName("East");
                        break;
                    case 3:
                        shuttle.setName("North");
                        break;
                    case 2:  //Double route
                        if(!foundFirst){
                            shuttle.setName("West A");
                            foundFirst = true;
                        }else{
                            shuttle.setName("West B");
                        }
                        break;
                    default:
                        shuttle.setName("DEFAULT");
                }
                shuttles.add(shuttle);
            }

            mapState.setShuttles(shuttles);


            //TEST STOPS
            ArrayList<Stop> stops = mapState.getStops();
           //do some code things to get times
           Random random = new Random();
          for (int i = 0; i < stops.size(); i++){
               stops.get(i).setShuttleETAs(new int[]{random.nextInt(600),random.nextInt(600),random.nextInt(600),random.nextInt(600)});
           }

           mapState.setStops(stops);


       }


    }

}
