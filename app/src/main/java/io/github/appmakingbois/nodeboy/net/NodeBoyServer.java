package io.github.appmakingbois.nodeboy.net;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;


public abstract class NodeBoyServer extends WebSocketServer {
    @Override
    public abstract void onOpen(WebSocket conn, ClientHandshake handshake);

    @Override
    public abstract void onClose(WebSocket conn, int code, String reason, boolean remote);

    @Override
    public abstract void onMessage(WebSocket conn, String message);

    @Override
    public abstract void onError(WebSocket conn, Exception ex);

    @Override
    public abstract void onStart();

    public NodeBoyServer(InetSocketAddress address){
        super(address);
    }
}
