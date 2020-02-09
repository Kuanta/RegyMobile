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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private EditText loginUsername;
    private EditText loginPassword;
    private Button   loginButton;
    private TextView loginErrors;
    private Button   goToRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.loginUsername = (EditText)findViewById(R.id.login_username);
        this.loginPassword = (EditText) findViewById(R.id.login_password);
        this.loginButton = (Button) findViewById(R.id.login_button);
        this.loginErrors = (TextView) findViewById(R.id.loginErrors);
        this.goToRegister = (Button) findViewById(R.id.go_register);
        //Click Handlers
        loginButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                login();
            }
        });

        goToRegister.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent =  new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
    protected void login(){
        String username = this.loginUsername.getText().toString();
        String password = this.loginPassword.getText().toString();

        RequestQueueManager reqQueueMan = RequestQueueManager.getInstance(this.getApplicationContext());


        String url = getString(R.string.server_url)+"/login";
        Log.d("DEBUG", "Url is "+url);
        JSONObject postParams = new JSONObject();
        try{
            postParams.put("username", username);
            postParams.put("password", password);
        }catch (JSONException e){
            Log.d("DEBUG", "Couldn't create json object");
        }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, postParams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("DEBUG", "Response has arrived");

                        //Try To go to members area
                        JSONArray errors = null;
                        try{
                            errors = response.getJSONArray("Errors");
                        }catch(JSONException e){
                            //Errors probably null
                        }

                        if(errors != null){
                            String errorsString = "";
                            try{
                                for(int i=0;i<errors.length();i++){
                                    errorsString += errors.get(i).toString()+"\n";
                                }
                            }catch(JSONException e){
                                Log.d("DEBUG", e.toString());
                            }

                            loginErrors.setText(errorsString);
                        }else{
                            //No errors, Proceed To Members area

                            //Save the Token first (A login response should contain a JWT Token)
                            String authToken;
                            try{
                                authToken = response.getString("Token");
                                Log.d("DEBUG", "Aurh token is "+authToken);
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
                        Log.d("DEBUG", "COuldn't Reach server");
                        loginErrors.setText("Couldn't Reach the server");
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
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
