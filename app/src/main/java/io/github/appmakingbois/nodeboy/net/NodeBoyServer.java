package io.github.appmakingbois.nodeboy.net;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;


public class NodeBoyServer extends WebSocketServer {
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Log.d("server","New connection from "+handshake.getResourceDescriptor());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.d("server","Connection closed: "+conn.getRemoteSocketAddress()+" ("+reason+")");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        //when we receive a message, rebroadcast it to all connected clients
        broadcast(message);
        Log.d("server","New message: "+message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.e("server","Error occurred!!");
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        Log.d("server","Server started");
    }

    public NodeBoyServer(InetSocketAddress address){
        super(address);
    }
}
