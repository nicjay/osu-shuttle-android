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

/*
  Created by jordan_n on 8/22/2014.
 */
public class JSONGetter {

    public final String TAG = "JSONGetter";
    private StringBuilder builder = new StringBuilder();
    private JSONArray mJSON;

    public JSONGetter(){
    }

    public JSONArray getJSONFromUrl(String url){
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);

        try{
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();

            if(statusCode == 200){
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;

                while((line = reader.readLine()) != null){
                    builder.append(line);
                }

            } else {
                Log.d(TAG, "statusCode: " + statusCode);
            }
        }catch (ClientProtocolException e){
            e.printStackTrace();
            Log.e(TAG, "ClientProtocolException");
        }catch (IOException e){
            e.printStackTrace();
            Log.e(TAG, "IOException");
        } finally{
            //TODO: cleanup http
        }

        try{
            mJSON = new JSONArray(builder.toString());
        }catch (JSONException e){
            Log.e(TAG, "JSONException");
        }

        return mJSON;
    }
}
