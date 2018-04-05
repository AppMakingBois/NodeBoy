package io.github.appmakingbois.nodeboy.net;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;

import java.util.ArrayList;

public class WifiP2PBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private Channel mChannel;

    private ConnectionChangeCallback connectionChangeCallback;
    private PeerChangeCallback peerChangeCallback;
    private ThisDeviceChangeCallback thisDeviceChangeCallback;
    private P2PStateChangeCallback p2pStateChangeCallback;

    public WifiP2PBroadcastReceiver(WifiP2pManager manager, Channel channel) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
    }

    public void onConnectionChange(ConnectionChangeCallback callback){
        this.connectionChangeCallback = callback;
    }

    public void onPeerChange(PeerChangeCallback callback){
        this.peerChangeCallback = callback;
    }

    public void onP2PStateChange(P2PStateChangeCallback callback){
        this.p2pStateChangeCallback = callback;
    }

    public void onThisDeviceChange(ThisDeviceChangeCallback callback){
        this.thisDeviceChangeCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, WifiP2pManager.WIFI_P2P_STATE_DISABLED);
                if(p2pStateChangeCallback!=null){
                    p2pStateChangeCallback.onStateChange(state);
                }
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            if (mManager != null) {
                mManager.requestPeers(mChannel, wifiP2pDeviceList -> {
                    ArrayList<WifiP2pDevice> newDeviceList = new ArrayList<>(wifiP2pDeviceList.getDeviceList());
                    if(peerChangeCallback!=null){
                        peerChangeCallback.onPeerChange(newDeviceList);
                    }
                });
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                mManager.requestConnectionInfo(mChannel, wifiP2pInfo -> {
                    if(connectionChangeCallback!=null){
                        connectionChangeCallback.onConnect(wifiP2pInfo);
                    }
                });
                mManager.requestGroupInfo(mChannel, wifiP2pGroup -> {
                    if(connectionChangeCallback!=null){
                        connectionChangeCallback.onGroupInfo(wifiP2pGroup);
                    }
                });
            } else {
                if(connectionChangeCallback!=null){
                    connectionChangeCallback.onDisconnect();
                }
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            WifiP2pDevice thisDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            if(thisDeviceChangeCallback!=null){
                thisDeviceChangeCallback.onThisDeviceChanged(thisDevice);
            }
        }
    }

    public interface ConnectionChangeCallback {
        void onConnect(WifiP2pInfo wifiP2pInfo);
        void onGroupInfo(WifiP2pGroup wifiP2pGroup);
        void onDisconnect();
    }

    public interface PeerChangeCallback {
        void onPeerChange(ArrayList<WifiP2pDevice> peers);
    }

    public interface ThisDeviceChangeCallback{
        void onThisDeviceChanged(WifiP2pDevice thisDevice);
    }

    public interface P2PStateChangeCallback{
        void onStateChange(int state);
    }
}
