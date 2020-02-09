package com.example.regymobile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ScrollingView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberAreaActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextView messageDisplay;
    private LinearLayout gpsLocation;
    private Button logoutButton;
    private Button startChatting;
    private Spinner nearbyLocations;
    private String username = "Anon"; //TODO: This should be an object that contains user info
    private FusedLocationProviderClient fusedLocationClient;
    private List<Geolocation> locations = new ArrayList<Geolocation>();
    public Geolocation selectedLoc = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_area);
        Log.d("DEBUG","Welcome To Members Area");

        //Widgets
        this.messageDisplay = findViewById(R.id.MemberMessage);
        this.logoutButton = findViewById(R.id.logout);
        this.startChatting = findViewById(R.id.go_chat);
        this.gpsLocation = findViewById(R.id.exploredPlaces);
        this.nearbyLocations = findViewById(R.id.nearbyLocations);

        Intent incomingIntent = getIntent();
        String jsonString = incomingIntent.getStringExtra("response");

        //Try to parse jsonString
        try{
            JSONObject jsonObj = new JSONObject(jsonString);
            JSONObject data = jsonObj.getJSONObject("Data");
            String Message = data.getString("Message");
            this.username = data.getString("Username");
            this.messageDisplay.setText(Message);
        }catch(JSONException e){
            Log.d("DEBUG", e.toString());
        }

        //Set Click Handler
        this.logoutButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //Logout
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.prefs_name), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.remove(getString(R.string.token_prefs_key));
                editor.apply(); //Save async

                //Go to Login
                Intent intent = new Intent(MemberAreaActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        this.startChatting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MemberAreaActivity.this.selectedLoc != null){
                    Intent intent = new Intent(MemberAreaActivity.this, ChatActivity.class);
                    intent.putExtra("username", MemberAreaActivity.this.username);
                    intent.putExtra("geoLocString", MemberAreaActivity.this.selectedLoc.getJSONString());
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), "Select a location first", Toast.LENGTH_LONG).show();
                }

            }
        });

        //Ask for permission if needed
        MemberAreaActivity.this.requestLocationPermission();

        //Get Location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        try{
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        TextView tv = new TextView(MemberAreaActivity.this.getApplicationContext());
                        tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        tv.setText("Lattitude:"+location.getLatitude()
                                +" Longitude:"+location.getLongitude());
                        MemberAreaActivity.this.gpsLocation.addView(tv);

                        MemberAreaActivity.this.explore(String.valueOf(location.getLatitude()),
                                String.valueOf(location.getLongitude()));

                    }else{
                        Log.d("DEBUG", "Location is null");
                    }
                }

            }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("DEBUG", e.getMessage());

                }
            });
        }catch(SecurityException e){
            //Probably a permission error
            Log.d("DEBUG", "CANT GET GPS");
        }
        Log.d("DEBUG", "End of Members Area");
    }

    public void requestLocationPermission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
    public void explore(String lat, String lng){
        RequestQueueManager reqQueueMan = RequestQueueManager.getInstance(this.getApplicationContext());
        String url = getString(R.string.server_url)+"/explore?lat="+lat+"&lng="+lng;
        Log.d("DEBUG", url);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){

            @Override
            public void onResponse(JSONObject response) {
                try{
                    JSONObject result = response.getJSONObject("Data");
                    JSONArray locations = (JSONArray) result.get("Locations");
                    String resultingText = "";
                    resultingText+="\n";
                    Geolocation nearbyLocations[] = new Geolocation[locations.length()];
                    for(int i=0;i<locations.length();i++){
                        JSONObject loc = (JSONObject) locations.get(i);
                        resultingText += ">"+loc.get("name")+"\n";

                        //Create geoloc
                        String name = (String) loc.get("name");
                        String lat = ((JSONArray)((JSONObject)loc.get("center")).get("coordinates")).get(1).toString();
                        String lng = ((JSONArray)((JSONObject)loc.get("center")).get("coordinates")).get(0).toString();
                        String id = (String) loc.get("_id");
                        JSONArray json_types = (JSONArray) loc.get("types");
                        String types[] = new String[json_types.length()];
                        for(int j=0; j<json_types.length();j++){
                            types[j] = (String) json_types.get(j);
                        }
                        Geolocation geoLoc = new Geolocation(id, name, lat, lng, types);
                        nearbyLocations[i] = geoLoc;
                                MemberAreaActivity.this.locations.add(geoLoc);
                    }
                    TextView tv = new TextView(MemberAreaActivity.this.getApplicationContext());
                    tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    tv.setText(resultingText);
                    MemberAreaActivity.this.gpsLocation.addView(tv);

                    //Populate the spinner
                    ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(MemberAreaActivity.this,
                            android.R.layout.simple_spinner_item, nearbyLocations);
                    MemberAreaActivity.this.nearbyLocations.setAdapter(spinnerArrayAdapter);
                    MemberAreaActivity.this.nearbyLocations.setOnItemSelectedListener(MemberAreaActivity.this);

                }catch(Exception e){
                    Log.d("DEBUG", e.toString());
                }
            }
        }, null)
        {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences prefs  = getSharedPreferences(getString(R.string.prefs_name), Context.MODE_PRIVATE);
                String token;

                token = prefs.getString(getString(R.string.token_prefs_key), "");
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };
        reqQueueMan.addToRequestQue(req);
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
        Geolocation geoLoc = (Geolocation)this.nearbyLocations.getSelectedItem();
        this.selectedLoc = geoLoc;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent){
        this.selectedLoc = null;
    }
}
