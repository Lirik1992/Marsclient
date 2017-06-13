package com.diploma.likharev.android.marsclient;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Lirik on 11.06.2017.
 */

public class Route {

    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;

    public List<LatLng> points;

}
