package com.example.regymobile;

import android.support.v7.app.AppCompatActivity;

import org.json.JSONObject;

public interface Callback {
    void respond(AppCompatActivity activity, JSONObject responseData);
    void error(AppCompatActivity activity, String error);
}
