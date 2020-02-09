package com.example.regymobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    //Widgets
    private EditText registerUsername;
    private EditText registerPassword;
    private Button registerSubmit;
    private TextView loginErrors;
    private Button goToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        this.registerUsername = (EditText) findViewById(R.id.register_username);
        this.registerPassword = (EditText) findViewById(R.id.register_password);
        this.registerSubmit = (Button) findViewById(R.id.register_submit);
        this.loginErrors = (TextView) findViewById(R.id.register_errors);
        this.goToLogin = (Button) findViewById(R.id.go_login);

        //Register Handler
        this.registerSubmit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                register();
            }
        });

        goToLogin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    private void register(){
        String username = this.registerUsername.getText().toString();
        String password = this.registerPassword.getText().toString();

        RequestQueueManager reqQueueMan = RequestQueueManager.getInstance(this.getApplicationContext());

        String url = getString(R.string.server_url)+"/register";
        JSONObject postParams = new JSONObject();
        try{
            postParams.put("username", username);
            postParams.put("password", password);
        }catch(JSONException e){
            Log.d("DEBUG", "Couldn't create json object while registering");
        }
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, postParams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Try To go to members area
                        JSONArray errors = null;
                        try{
                            //Check Errors
                            errors = response.getJSONArray("Errors");

                        }catch(JSONException e){
                            Log.d("DEBUG", "Couldn't Parse Response");
                        }
                        if(errors != null){
                            String errorsString = "";
                            try{
                                for(int i=0;i<errors.length();i++){
                                    errorsString += errors.get(i).toString()+"\n";
                                }
                            }catch(JSONException e){
                                //Nothing to catch
                            }

                            loginErrors.setText(errorsString);
                        }else{
                            //No errors, Proceed To Members area

                            //Save the Token first (A login response should contain a JWT Token)
                            String authToken;
                            try{
                                authToken = response.getString("Token");

                                //Save the token to Shared Prefs

                                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.prefs_name), Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(getString(R.string.token_prefs_key), authToken);
                                editor.apply(); //Save async
                                goToMembersArea(response);
                            }catch(JSONException e){
                                Log.d("JSONError", "Couldn't Get JWT Token from Login Response");
                            }

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Can't Connect To Server", Toast.LENGTH_LONG).show();
                    }
                }
        );
        reqQueueMan.addToRequestQue(req);
    }
    protected void goToMembersArea(JSONObject object){
        Intent intent = new Intent(this, MemberAreaActivity.class);
        intent.putExtra("response", object.toString());
        startActivity(intent);
    }

}
