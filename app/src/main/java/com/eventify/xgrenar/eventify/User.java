package com.eventify.xgrenar.eventify;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.geometry.LatLng;

public class User {
    private String email;
    private double latitude;
    private double longitude;
    private String image;
    private int cctv;
    private String action;
    private MeetingPoint meetingPoint;

    private transient LatLng position;
    private transient Marker marker;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public int getCctv() {
        return cctv;
    }

    public void setCctv(int cctv) {
        this.cctv = cctv;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public MeetingPoint getMeetingPoint() {
        return meetingPoint;
    }

    public void setMeetingPoint(MeetingPoint meetingPoint) {
        this.meetingPoint = meetingPoint;
    }
}
