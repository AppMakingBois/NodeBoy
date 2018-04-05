package io.github.appmakingbois.nodeboy.net;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;


public abstract class NodeBoyClient extends WebSocketClient {
    public NodeBoyClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public abstract void onOpen(ServerHandshake handshakedata);

    @Override
    public abstract void onMessage(String message);

    @Override
    public abstract void onClose(int code, String reason, boolean remote);

    @Override
    public abstract void onError(Exception ex);
}
