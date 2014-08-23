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

/**
 * Created by jordan_n on 8/21/2014.
 */
public class ShuttleUpdater {
    private static final String TAG = "ShuttleUpdater";
    private boolean isBusy = false;
    private boolean stop = false;
    private Handler mHandler = new Handler();

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


    public ShuttleUpdater(OnMapStateUpdate listener) {
        this.listener = listener;
        mapState = MapState.get();
        Log.d(TAG, "ShuttleUpdater CONSTRUCTED");

        new pollNewDataTask(urlShuttlePoints);

        startHandler();
    }


    private void startHandler(){
        r = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "--------RUN---------");
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
        mHandler.removeCallbacks(r);
        mHandler.postDelayed(r, asyncDelay);
    }

    private class pollNewDataTask extends AsyncTask<String, Void, JSONArray[]>{

        private String url;

        private JSONArray[] jarray;

        public pollNewDataTask(String url) {
            super();
            this.url = url;
        }

        @Override
        protected JSONArray[] doInBackground(String... params) {
            JSONGetter getter = new JSONGetter();
            jarray = getter.getJSONFromUrl(url);
            return jarray;
        }

        @Override
        protected void onPostExecute(JSONArray[] j) {
            super.onPostExecute(j);

            parseJSON(j);
            listener.updateMap();

            //Log.d(TAG, "RETURNED JsonArray: " + j.toString());
        }


        //TODO: reduce number of string conversions here and JSONGetter
       private void parseJSON(JSONArray[] j){
            Gson gson = new Gson();

            JSONArray JSONShuttles = j[0];


            for (int i=0; i<JSONShuttles.length();i++) {

                String json = null;

                try {
                    JSONObject test = JSONShuttles.getJSONObject(i);
                    json = test.toString();
                    //Log.d(TAG, "json in parse is: "+test);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mapState.setShuttle(i, gson.fromJson(json, Shuttle.class));

                //Log.d(TAG, ">>SHUTTLE "+ gson.fromJson(json, Shuttle.class));
              // Log.d(TAG, "NAME SHUTTLE:"+mapState.getShuttles()[i].getName());
            }

       }

    }

}
