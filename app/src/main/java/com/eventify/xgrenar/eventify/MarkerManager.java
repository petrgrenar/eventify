package com.eventify.xgrenar.eventify;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MarkerManager {
    public static Marker addMarkerToMap(Context context, MapboxMap mapboxMap, LatLng position, String email){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.setTitle(email);
        markerOptions.setPosition(position);
        Log.i("Markermanager", "Pridavam marker" + position + " -> " + markerOptions);
        return mapboxMap.addMarker(markerOptions);
    }

    public static Marker addMarkerToMap(Context context, MapboxMap mapboxMap, LatLng position, int imageResource, String type){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.setTitle(type);
        markerOptions.setPosition(position);
        markerOptions.icon(IconFactory.getInstance(context).fromBitmap(createmarkerIcon((Activity) context, imageResource)));
        return mapboxMap.addMarker(markerOptions);
    }

    private static Bitmap createmarkerIcon(Activity context, int imageResource){
        View view = LayoutInflater.from(context).inflate(R.layout.marker_layout, null);
        CircleImageView circleImageView = view.findViewById(R.id.profile_image);
        circleImageView.setImageResource(imageResource);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);  //odsazeni layoutu
        view.buildDrawingCache();
        //Prazdna bitmapa
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
}
