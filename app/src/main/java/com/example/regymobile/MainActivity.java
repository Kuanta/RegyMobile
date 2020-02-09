package com.example.regymobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("DEBUG","At the start");
        checkAuth();
    }
    /*
        Checks if an auth token is available and is valid
     */
    private void checkAuth(){

        SharedPreferences prefs  = getSharedPreferences(getString(R.string.prefs_name), Context.MODE_PRIVATE);
        String token;

        token = prefs.getString(getString(R.string.token_prefs_key), "");
        Log.d("DEBUG", "Checking Token");
        if (token == null){
            //Go to login
            Log.d("DEBUG", "Going to login since there isn't a token available");
            goToLogin();
        }else{
            //Try to request Members Area
            sendToken(token);
        }
    }

    /*
        Tries to get the members page data by sending a get request to ..url../member with the
        token
     */
    private void sendToken(String token){
        final String tok = token;
        Log.d("DEBUG", "Sending the token "+token);
        final Callback cb = new Callback() {
            @Override
            public void respond(AppCompatActivity activity, JSONObject responseData) {
                Log.d("DEBUG", "Running Callback");
                //Check Errors
                if(responseData != null){
                    JSONArray errors = null;
                    try{
                        errors = responseData.getJSONArray("Errors");

                    }catch(JSONException e){
                        Log.d("JSONError", "Couldn't Parse Errors Array in Main");
                    }

                    if (errors != null && errors.length()>0){
                        //Go login
                        Log.d("DEBUG", "Errors COunt is "+errors.length());
                        try{
                            String errorMessage = errors.getString(0);
                            Log.d("DEBUG", errorMessage);
                        }catch(JSONException e){
                            goToLogin();
                        }
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }else{
                        //Go to members area
                        goToMembersArea(responseData);
                    }
                }
            }

            @Override
            public void error(AppCompatActivity activity, String error) {
                goToLogin();
            }


        };
        RequestQueueManager reqQueueMan = RequestQueueManager.getInstance(this.getApplicationContext());
        String url = getString(R.string.server_url)+"/member";
        Log.d("DEBUG",url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("DEBUG", "Response From Member");
                        cb.respond(MainActivity.this, response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        cb.error(MainActivity.this, error.toString());
                    }
                })
            {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", "Bearer " + tok);
                    return params;
                }
            };
        reqQueueMan.addToRequestQue(jsonObjectRequest);
    }
    protected void goToMembersArea(JSONObject object){
        Intent intent = new Intent(this, MemberAreaActivity.class);
        intent.putExtra("response", object.toString());
        startActivity(intent);
    }
    private void goToLogin(){
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
