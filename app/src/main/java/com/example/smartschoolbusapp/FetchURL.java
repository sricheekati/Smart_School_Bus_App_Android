package com.example.smartschoolbusapp;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class FetchURL extends AsyncTask<String, Void, String> {
    private Context context;

    public FetchURL(Context ctx) {
        this.context = ctx;
    }

    @Override
    protected String doInBackground(String... strings) {
        String responseString = "";
        try {
            URL url = new URL(strings[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream stream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            responseString = builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONArray routes = jsonObject.getJSONArray("routes");

            if (routes.length() > 0) {
                String encodedPath = routes.getJSONObject(0).getJSONObject("overview_polyline").getString("points");
                List<LatLng> points = PolyUtil.decode(encodedPath);

                // ✅ Call the public method in RoutesActivity to draw the polyline
                if (context instanceof RoutesActivity) {
                    ((RoutesActivity) context).drawRoutePolyline(points);
                }
            } else {
                Toast.makeText(context, "No route found!", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}