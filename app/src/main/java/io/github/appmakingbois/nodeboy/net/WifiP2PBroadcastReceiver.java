package io.github.appmakingbois.nodeboy.net;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

public class WifiP2PBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private Channel mChannel;

    public WifiP2PBroadcastReceiver(WifiP2pManager manager, Channel channel) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    // Wifi P2P is enabled
                } else {
                    Log.e("net","WiFi P2P is not enabled!!");
                }
                //check if P2P is enabled and notify appropriate activity
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            if (mManager != null) {
                mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                        Collection<WifiP2pDevice> deviceList = wifiP2pDeviceList.getDeviceList();
                        for(final WifiP2pDevice device : deviceList){
                            Log.d("discovery",device.deviceName+" @ "+device.deviceAddress);
                            WifiP2pConfig cfg = new WifiP2pConfig();
                            cfg.deviceAddress = device.deviceAddress;
                            if(ConnectionManager.getInstance().getDeviceByAddress(device.deviceAddress)==null) {
                                mManager.connect(mChannel, cfg, new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d("connection", "successfully connected to "+device.deviceAddress);
                                        ConnectionManager.getInstance().addConnection(device);
                                    }

                                    @Override
                                    public void onFailure(int reasonCode) {
                                        Log.e("connection", "connection failed! " + reasonCode);
                                    }
                                });
                            }
                            else{
                                Log.d("connection","we are already connected to "+device.deviceAddress);
                            }
                        }
                    }
                });
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                @Override
                public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
                    Log.d("connection",wifiP2pInfo.toString());
                }
            });

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }
}
