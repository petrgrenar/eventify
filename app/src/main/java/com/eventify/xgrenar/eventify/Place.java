package com.eventify.xgrenar.eventify;

import com.mapbox.mapboxsdk.geometry.LatLng;

public class Place {
    private String type;
    private double latitude;
    private double longitude;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
