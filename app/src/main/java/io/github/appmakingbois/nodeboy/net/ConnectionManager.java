package io.github.appmakingbois.nodeboy.net;


import android.net.wifi.p2p.WifiP2pDevice;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

public class ConnectionManager {
    private ArrayList<WifiP2pDevice> connections;

    public ArrayList<WifiP2pDevice> getConnections() {
        return copyList(connections);
    }

    @Nullable
    public WifiP2pDevice getDeviceByIndex(int i) {
        try {
            return copyDevice(connections.get(i));
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    @Nullable
    public WifiP2pDevice getDeviceByAddress(@NonNull String address) {
        for (WifiP2pDevice d : connections) {
            if (d.deviceAddress.equalsIgnoreCase(address)) {
                return copyDevice(d);
            }
        }
        return null;
    }

    public void addConnection(@NonNull WifiP2pDevice device) {
        connections.add(copyDevice(device));
    }

    public boolean removeConnection(int index) {
        WifiP2pDevice result = getDeviceByIndex(index);
        if (result != null) {
            connections.remove(index);
            return true;
        }
        else {
            return false;
        }
    }

    public boolean removeConnection(@NonNull String address) {
        WifiP2pDevice result = getDeviceByAddress(address);
        if(result!=null){
            connections.remove(result);
            return true;
        }
        else{
            return false;
        }
    }

    public boolean removeConnection(@NonNull WifiP2pDevice device){
        return removeConnection(device.deviceAddress);
    }

    @NonNull
    private WifiP2pDevice copyDevice(@NonNull WifiP2pDevice device) {
        return new WifiP2pDevice(device);
    }

    @NonNull
    private ArrayList<WifiP2pDevice> copyList(@NonNull ArrayList<WifiP2pDevice> input) {
        ArrayList<WifiP2pDevice> output = new ArrayList<>();
        for (WifiP2pDevice d : input) {
            output.add(new WifiP2pDevice(d));
        }
        return output;
    }

    private static ConnectionManager instance;

    private ConnectionManager() {
        connections = new ArrayList<>();
    }

    @NonNull
    public static ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }
}