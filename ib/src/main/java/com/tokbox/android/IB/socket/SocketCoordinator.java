package com.tokbox.android.IB.socket;

import android.util.Log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.tokbox.android.IB.config.IBConfig;

import org.json.JSONObject;

import java.net.URISyntaxException;


public class SocketCoordinator {

    private static final String LOG_TAG = SocketCoordinator.class.getSimpleName();

    private Socket mSocket;

    {
        try {
            mSocket = IO.socket(IBConfig.SIGNALING_URL);
        } catch (URISyntaxException e) {
            Log.i(LOG_TAG, e.getMessage());
        }
    }

    public void connect() {
        mSocket.connect();

        Log.i(LOG_TAG, "connected");
    }

    public void emitJoinRoom(String sessionIdProducer) {
        if(mSocket.connected()) {
            mSocket.emit("joinRoom", sessionIdProducer);
            Log.i(LOG_TAG, "joinRoom emitted");
        } else {
            Log.i(LOG_TAG, "joinRoom not emitted");
        }
    }

    public Socket getSocket() {
        return mSocket;
    }

    /*public void on(String sessionIdProducer) {
        if(mSocket.connected()) {
            mSocket.emit("joinRoom", sessionIdProducer);
            Log.i(LOG_TAG, "joinRoom emitted");
        } else {
            Log.i(LOG_TAG, "joinRoom not emitted");
        }
    }*/



    public void SendSnapShot(JSONObject data) {
        if(mSocket.connected()) {
            mSocket.emit("mySnapshot", data);
            Log.i(LOG_TAG, "mySnapshot emitted");
        } else {
            Log.i(LOG_TAG, "mySnapshot not emitted");
        }

    }

    public void disconnect() {
        if(mSocket.connected()) {
            Log.i(LOG_TAG, "socket disconnected");
            mSocket.disconnect();
            //mSocket.off("new message", onNewMessage);
        }
    }

}
