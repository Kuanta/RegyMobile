package com.example.regymobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RequestManager {

    public static void sendPostRequest(final AppCompatActivity activity, String url, JSONObject params, final Callback cb,final boolean useToken){
        RequestQueueManager reqQueueMan = RequestQueueManager.getInstance(activity.getApplicationContext());
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        cb.respond(activity, response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        cb.error(activity, error.toString());
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() {
                String token = "";
                if (useToken) {
                    SharedPreferences prefs  = activity.getSharedPreferences(activity.getString(R.string.prefs_name), Context.MODE_PRIVATE);
                    token = prefs.getString(activity.getString(R.string.token_prefs_key), "");

                }
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };

        reqQueueMan.addToRequestQue(req);
    }

    public static void sendGetRequest(final AppCompatActivity activity, String url, final Callback cb,final boolean useToken){
        RequestQueueManager reqQueueMan = RequestQueueManager.getInstance(activity.getApplicationContext());
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null
                ,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        cb.respond(activity, response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        cb.error(activity, error.toString());
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() {
                String token = "";
                if (useToken) {
                    SharedPreferences prefs  = activity.getSharedPreferences(activity.getString(R.string.prefs_name), Context.MODE_PRIVATE);
                    token = prefs.getString(activity.getString(R.string.token_prefs_key), "");

                }
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };

        reqQueueMan.addToRequestQue(req);
    }
}
