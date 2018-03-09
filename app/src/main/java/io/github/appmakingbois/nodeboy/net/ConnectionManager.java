package io.github.appmakingbois.nodeboy.net;


import android.net.wifi.p2p.WifiP2pDevice;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private ConcurrentHashMap<String,Connection> connectionMap;

    public ArrayList<Connection> getConnections() {
        return mapToArray(connectionMap);
    }

    @Nullable
    public Connection getDeviceByAddress(@NonNull String address) {
        return connectionMap.get(address);
    }

    public void addConnection(@NonNull WifiP2pDevice device) {
        if(getDeviceByAddress(device.deviceAddress)==null){
            connectionMap.put(device.deviceAddress,new Connection(device));
        }
    }

    public void addConnection(@NonNull Connection connection){
        connectionMap.put(connection.getDevice().deviceAddress,connection);
    }

    public boolean removeConnection(@NonNull String address) {
        Connection result = getDeviceByAddress(address);
        return result != null && connectionMap.remove(address) != null;
    }

    public boolean removeConnection(@NonNull WifiP2pDevice device){
        return removeConnection(device.deviceAddress);
    }

    public boolean removeConnection(@NonNull Connection connection){
        return removeConnection(connection.getDevice());
    }

    public void removeAllConnections(){
        Iterator i = connectionMap.entrySet().iterator();
        while(i.hasNext()){
            removeConnection(((Connection) i.next()).getDevice());
            i.remove();
        }
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

    @NonNull
    private ArrayList<Connection> mapToArray(@NonNull ConcurrentHashMap<String,Connection> input){
        Iterator i = input.entrySet().iterator();
        ArrayList<Connection> output = new ArrayList<>();
        while(i.hasNext()){
            output.add((Connection) i.next());
            i.remove();
        }
        return output;
    }

    private static ConnectionManager instance;

    private ConnectionManager() {
        connectionMap = new ConcurrentHashMap<>();
    }

    @NonNull
    public static ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }
}
