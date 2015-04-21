package edu.oregonstate.beaverbus;

import android.text.format.Time;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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

  Gets JSON from specified URL. Building and returning JSONArray
*/

public class JSONGetter {
    public final String TAG = "JSONGetter";

    private StringBuilder builder = new StringBuilder();
    private JSONArray mJSON;

    public JSONGetter() {
        //Constructor
    }

    public JSONArray getJSONFromUrl(String url) {
        //Get
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);

        //Parse
        try {
            HttpResponse response = client.execute(httpGet);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) { //If OKAY
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.e(TAG, "ERROR statusCode: " + statusCode);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            Log.e(TAG, "ClientProtocolException");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException");
        } finally {
            //TODO 12/11/2014: cleanup http
        }

        //Get JSON from built string
        try {
            mJSON = new JSONArray(builder.toString());
        } catch (JSONException e) {
            Log.e(TAG, "JSONException. URL: " + url + ", toString: " + builder.toString());

        }
        return mJSON;
    }
}
