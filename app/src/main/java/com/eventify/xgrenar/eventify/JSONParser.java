package com.eventify.xgrenar.eventify;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class JSONParser {
    public static List<User> loadUsers(Context context, int jsonFile){
        InputStream inputStream = context.getResources().openRawResource(jsonFile);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        Gson gson = new Gson();
        List<User> users = gson.fromJson(bufferedReader, new TypeToken<List<User>>(){}.getType());

        for (User user: users){
            user.setPosition(new LatLng(user.getLatitude(), user.getLongitude()));
        }

        return users;
    }
}
