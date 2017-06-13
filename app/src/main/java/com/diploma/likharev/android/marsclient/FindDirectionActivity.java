package com.diploma.likharev.android.marsclient;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lirik on 27.04.2017.
 */

public class FindDirectionActivity {

    private static final String
            DIRECTION_URL = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String API_KEY = "AIzaSyDeFw7Qay9435LR7IdgMmeGf6oTRcHr9hA";
    private FindDirectionActivityListener listener;
    private String origin;
    private String destination;


    public FindDirectionActivity(FindDirectionActivityListener listener,
                                 String origin, String destination) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onFindDirectionActivityStart();
        new DownloadJsonData().execute(createRequest());
    }

    private String createRequest() throws UnsupportedEncodingException, NullPointerException {

        try {

            String urlOrigin = URLEncoder.encode(origin, "utf-8");
            String urlDestination = URLEncoder.encode(destination, "utf-8");

            return DIRECTION_URL + "origin=" + urlOrigin + "&destination=" + urlDestination
                    + "&key=" + API_KEY;
        } catch (NullPointerException ignored) {
        }

        return DIRECTION_URL + "origin=" + origin + "&destination=" + destination
                + "&key=" + API_KEY;
    }

    private void parseJSon(String info) throws JSONException {

        if (info == null) {
            return;
        }
        List<Route> routes = new ArrayList<>();
        JSONObject jsonData = new JSONObject(info);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        for (int i = 0; i < jsonRoutes.length(); i++) {

            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            Route route = new Route();

            JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLeg = jsonLegs.getJSONObject(0);

            JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
            JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

            route.endAddress = jsonLeg.getString("end_address");
            route.startAddress = jsonLeg.getString("start_address");
            route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
            route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
            route.points = decodePolyLine(overview_polylineJson.getString("points"));

            routes.add(route);
        }
        listener.onFindDirectionActivitySuccess(routes);
    }

    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }

    private class DownloadJsonData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String link = params[0];

            try {
                URL url = new URL(link);
                InputStream is = url.openConnection().getInputStream();
                StringBuilder buffer = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                return buffer.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String results) {
            try {
                parseJSon(results);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
