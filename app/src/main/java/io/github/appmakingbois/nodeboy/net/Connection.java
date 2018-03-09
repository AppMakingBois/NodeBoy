package io.github.appmakingbois.nodeboy.net;

import android.net.wifi.p2p.WifiP2pDevice;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.net.Socket;

public class Connection {
    private WifiP2pDevice device;
    private Socket socket;
    private long lastKeepAlive;

    public Connection(@NonNull WifiP2pDevice device){
        this.device = device;
    }

    @Nullable
    public Socket getSocket(){
        return this.socket;
    }

    public boolean isSocketOpen() {
        return this.socket != null && !this.socket.isClosed();
    }

    @NonNull
    public WifiP2pDevice getDevice(){
        return this.device;
    }

    public void setSocket(@NonNull Socket socket){
        this.socket = socket;
    }
}
