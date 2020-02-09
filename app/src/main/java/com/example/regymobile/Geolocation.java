package com.example.regymobile;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Geolocation {
    String id;
    String name;
    String lat;
    String lng;
    String types[];

    public Geolocation(String id, String name, String lat, String lng, String types[]){
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.types = types;
    }
    public String toString(){
        return this.name;
    }
    public JSONObject getJSONObject(){
        JSONObject obj = new JSONObject();
        try{
            obj.put("_id", this.id);
            obj.put("name", this.name);
            JSONArray json_types = new JSONArray();
            for(int i=0;i<this.types.length;i++){
                json_types.put(this.types[i]);
            }
            obj.put("types", json_types);

            JSONObject center = new JSONObject();
            JSONArray json_coords = new JSONArray();
            json_coords.put(Double.parseDouble(this.lng));  //Convert to float64 (api requirment)
            json_coords.put(Double.parseDouble(this.lat));
            center.put("type", "Point");
            center.put("coordinates", json_coords);
            obj.put("center", center);
            return obj;
        }catch (JSONException e){
            return null;
        }
    }
    public String getJSONString(){
        return this.getJSONObject().toString();
    }
}
