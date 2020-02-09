package com.example.regymobile;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class RequestQueueManager {
    public static RequestQueueManager instance;
    private RequestQueue reqQueue;
    private static Context cont;
    public RequestQueueManager(Context ctx){
        this.cont = ctx;
    }
    public RequestQueue getRequestQueue(){
        if(this.reqQueue == null){
            this.reqQueue = Volley.newRequestQueue(cont.getApplicationContext());
        }
        return this.reqQueue;
    }
    public static synchronized RequestQueueManager getInstance(Context context) {
        if (instance == null) {
            instance = new RequestQueueManager(context);
        }
        return instance;
    }
    public<T> void addToRequestQue(Request<T> request){
        RequestQueue queue = this.getRequestQueue();
        queue.add(request);

    }
}
