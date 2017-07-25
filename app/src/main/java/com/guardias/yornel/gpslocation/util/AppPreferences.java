package com.guardias.yornel.gpslocation.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.guardias.yornel.gpslocation.entity.ControlPosition;
import com.guardias.yornel.gpslocation.entity.Position;
import com.guardias.yornel.gpslocation.entity.User;
import com.guardias.yornel.gpslocation.entity.Watch;

import java.util.ArrayList;

/**
 * Created by Yornel on 04-jul-16.
 */
public class AppPreferences {

    public static final String PREF_FILE_NAME = "app-radar14";

    private static final String ID = "id";
    private static final String C_POSITION = "control_position";
    private static final String USER = "user";
    private static final String WATCH = "watch";
    private static final String POSITIONS = "positions";


    Context context;
    SharedPreferences preferences;

    public AppPreferences(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_FILE_NAME, context.MODE_PRIVATE);
    }

    public User getUser() {
        String userString = preferences.getString(USER, null);
        if (userString == null) {
            return null;
        } else {
            User user = new Gson()
                    .fromJson(userString, User.class);
            return user;
        }
    }

    public Watch getWatch() {
        String watchString = preferences.getString(WATCH, null);
        if (watchString == null) {
            return null;
        } else {
            Watch watch = new Gson()
                    .fromJson(watchString, Watch.class);
            return watch;
        }
    }

    public ArrayList<Position> getPositions() {
        String watchString = preferences.getString(POSITIONS, null);
        if (watchString == null) {
            return null;
        } else {
            ArrayList<Position> positions = new Gson()
                    .fromJson(watchString, new TypeToken<ArrayList<Position>>() {}.getType());
            return positions;
        }
    }

    public void save(User user) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USER, new Gson().toJson(user));
        editor.commit();
    }

    public void save(Watch watch) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(WATCH, new Gson().toJson(watch));
        editor.commit();
    }

    public void save(Position position) {
        ArrayList<Position> positions = getPositions();
        if (positions == null)
            positions = new ArrayList<>();
        positions.add(position);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(POSITIONS, new Gson().toJson(positions));
        editor.commit();
    }

    public void clearWatchs() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(WATCH);
        editor.remove(POSITIONS);
        editor.commit();
    }

    public void clearPositions() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(POSITIONS);
        editor.commit();
    }

    public void clearUser() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(USER);
        editor.commit();
    }


    public void clearAll() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(USER);
        editor.remove(WATCH);
        editor.remove(POSITIONS);
        editor.commit();
    }
}
