package com.example.regymobile;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

public class RegySocket {
    private Socket socket = null;
    private BufferedReader incoming = null;
    private BufferedOutputStream outgoing = null;
    private Properties props;

    public RegySocket(){
        props.setProperty("Version", "1.0.0");
    }
    public void setProperty(String key, String value){
        props.setProperty(key, value);
    }
    public void connect(String ip, int port){
        try{
            socket = new Socket(ip,port);
            incoming = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outgoing = new BufferedOutputStream(socket.getOutputStream());
        }catch(UnknownHostException e){

        }catch (IOException e){

        }

    }
    public void sendMessage(String message){
        String propsString = props.toString();
        byte[] bytes = propsString.getBytes();
        try{
            outgoing.write(bytes,0, bytes.length);
        }catch (IOException e){
            this.print(e.toString());
        }

    }
    public void receive(){
        
    }
    //Utils
    private void print(String s){
        Log.d("DEBUG", s);
    }
}
