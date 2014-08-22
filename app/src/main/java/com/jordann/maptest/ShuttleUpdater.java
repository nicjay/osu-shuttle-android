package com.jordann.maptest;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

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

    private String urlShuttlePoints = "http://apps-webdev.campusops.oregonstate.edu/robechar/portal/files/shuttle/GetMapVehiclePoints.txt";
    private String urlJsonTest = "http://ip.jsontest.com/";


    public ShuttleUpdater() {
        Log.d(TAG, "ShuttleUpdater CONSTRUCTED");
        startHandler();
    }

    private void startHandler(){
        r = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "--------RUN---------");
                if(!isBusy) callAsyncTask();

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


    private void callAsyncTask(){
        Log.d(TAG, "callAsyncTask");

        new pollNewDataTask(urlJsonTest).execute();


    }

    private class pollNewDataTask extends AsyncTask<String, Void, JSONArray>{

        private String url;
        private StringBuilder builder = new StringBuilder();
        private JSONArray jarray;

        public pollNewDataTask(String url) {
            super();
            this.url = url;
        }

        @Override
        protected JSONArray doInBackground(String... params) {

            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);

            try{
                HttpResponse response = client.execute(httpGet);

                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                Log.d(TAG, "statusCode: " + statusCode);
                if(statusCode == 200){
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;

                    while((line = reader.readLine()) != null){
                        Log.d(TAG, "LINE: "+ line);
                        builder.append(line);
                    }
                }//TODO: else

            }catch (ClientProtocolException e){
                e.printStackTrace();
                Log.e(TAG, "ClientProtocolException");
            }catch (IOException e){
                e.printStackTrace();
                Log.e(TAG, "IOException");
            }

            try{
                Log.d(TAG, "builder.toString(): " + builder.toString());

                jarray = new JSONArray("["+builder.toString()+"]");

            }catch (JSONException e){
                Log.e(TAG, "JSONException");
            }

            return jarray;
        }

        @Override
        protected void onPostExecute(JSONArray j) {
            super.onPostExecute(j);
            Log.d(TAG, "RETURNED JsonArray: " + j.toString());
        }
    }

}
