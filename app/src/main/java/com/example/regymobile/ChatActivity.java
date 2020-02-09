package com.example.regymobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.ScrollingView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.Transport;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Manager;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.BufferedSink;
import okio.ByteString;

public class ChatActivity extends AppCompatActivity {
    private ScrollView chatPanel;
    private LinearLayout chatPanelLayout;
    private EditText chatInput;
    private Button sendMessageButton;
    private String username;
    private OkHttpClient client;
    private WebSocket webSocket;

    private final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            ChatActivity.this.webSocket = webSocket;
            webSocket.send(ChatActivity.this.createMessage("Hi"));

        }
        @Override
        public void onMessage(WebSocket webSocket, String text) {
            JSONObject jo;
            try{
                jo = new JSONObject(text);
                addMessageToUI(jo.get("sender").toString(), jo.get("message").toString());
            }catch(JSONException e){
                Log.d("DEBUG", e.toString());
            }
        }
        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            addMessageToUI("Server", "Receiving bytes : " + bytes.hex());
        }
        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            addMessageToUI("Server", "Closing : " + code + " / " + reason);
        }
        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            addMessageToUI("Server", "Couldn't connect to server");
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        this.chatPanel = findViewById(R.id.chat_messages);
        this.chatPanelLayout = findViewById(R.id.chatMessages);
        Intent incomingIntent = getIntent();
        String username = incomingIntent.getStringExtra("username");
        if (username != null){
            this.username = username;
        }

        String geoLocString = incomingIntent.getStringExtra("geoLocString");

        //Widgets
        this.chatInput = findViewById(R.id.chat_message);
        this.sendMessageButton = findViewById(R.id.send_chat_message);
        this.sendMessageButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //Get the written text
                String message = ChatActivity.this.chatInput.getText().toString();
                if(message.length() > 0){
                    ChatActivity.this.sendMessage(message);
                    ChatActivity.this.chatInput.setText(""); // Clear the text
                }
            }
        });

        // Start socket connection
        SharedPreferences prefs  = getSharedPreferences(getString(R.string.prefs_name), Context.MODE_PRIVATE);
        String token;

        token = prefs.getString(getString(R.string.token_prefs_key), "");
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody reqBod = RequestBody.create(JSON, geoLocString);
        String encodedString = android.util.Base64.encodeToString(geoLocString.getBytes(), android.util.Base64.DEFAULT);
        Request request = new Request.Builder().url(getString(R.string.server_url)+"/chat?geoloc="+encodedString)
                .addHeader("Authorization", "Bearer "+token).build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        client = new OkHttpClient();
        try{
            WebSocket ws = client.newWebSocket(request, listener);
            client.dispatcher().executorService().shutdown();
        }catch (Exception e){
            Log.d("DEBUG", e.getMessage());
        }

    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if(this.webSocket != null){
            this.webSocket.close(1000, "Closed by user");
        }
    }

    private void addMessageToUI(final String senderName, final String message){
        //Define new Text View
        //TODO: Remove older chat messages when chat panel has too many childs
        ChatActivity.this.runOnUiThread(new Runnable(){
            public void run(){
                TextView tv = new TextView(ChatActivity.this.getApplicationContext());
                tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                tv.setText(senderName+": "+message);

                ChatActivity.this.chatPanelLayout.addView(tv);
            }
        });

    }

    private String createMessage(String message){
        JSONObject obj = new JSONObject();
        try{
            obj.put("sender", this.username);
            obj.put("message", message);
            return obj.toString();
        }catch(JSONException e){
            return "Failed";
        }

    }

    private void sendMessage(String message){
        if(this.webSocket != null){
            this.webSocket.send(this.createMessage(message));
        }
    }
}
