package com.jordann.maptest;

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
 * Created by jordan_n on 8/22/2014.
 */
public class JSONGetter {

    public final String TAG = "JSONGetter";
    private StringBuilder builder = new StringBuilder();
    private JSONArray mJSONShuttles;
    private JSONArray mJSONStops;
    private JSONArray[] jarray;

    public JSONGetter(){
        jarray = new JSONArray[2];

    }

    public JSONArray[] getJSONFromUrl(String url){
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);

        try{
            Log.d(TAG, "HTTP TRY");
            HttpResponse response = client.execute(httpGet);

            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            //Log.d(TAG, "statusCode: " + statusCode);
            if(statusCode == 200){
                Log.d(TAG, "Got the status code");
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;

                while((line = reader.readLine()) != null){
                    //Log.d(TAG, "LINE: "+ line);
                    builder.append(line);
                }
            }//TODO: else
        Log.d(TAG, "END OF HTTP TRY");
        }catch (ClientProtocolException e){
            e.printStackTrace();
            Log.e(TAG, "ClientProtocolException");
        }catch (IOException e){
            e.printStackTrace();
            Log.e(TAG, "IOException");
        } finally{
            //cleanup http
        }

        try{
            //Log.d(TAG, "builder.toString(): " + builder.toString());
            mJSONShuttles = new JSONArray(builder.toString());
        }catch (JSONException e){
            Log.e(TAG, "JSONException");
        }

        //do stops http

        jarray[0] = mJSONShuttles;
        jarray[1] = new JSONArray();


        return jarray;

    }



}
