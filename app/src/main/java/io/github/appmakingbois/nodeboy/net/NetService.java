package io.github.appmakingbois.nodeboy.net;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.github.appmakingbois.nodeboy.ChatActivity;
import io.github.appmakingbois.nodeboy.R;
import io.github.appmakingbois.nodeboy.protocol.Packet;


public class NetService extends Service {

    private int mNotificationId = 42069;

    public static int SERVER_PORT = 4200;
    public static int CLIENT_PORT = 4201;

    private WifiP2PBroadcastReceiver receiver;
    private WifiP2pManager.Channel channel;

    private boolean started = false;

    private ConnectionManager connectionManager;

    private ConcurrentLinkedQueue<Packet> outgoingPackets;

    private NetServiceBinder binder;

    private String myAddress;

    private ServerSocket serverSocket;

    private Socket clientSocket;

    private boolean isServer = false;

    private WifiP2pManager p2pManager;

    private Handler discoveryHandler = new Handler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(getString(R.string.action_start))) {
                if (!started) {
                    Log.d("service", "starting");
                    startup();
                }
            }
            else if (intent.getAction().equals(getString(R.string.action_stop))) {
                Log.d("service", "stopping");
                shutdown();
            }
        }
        return START_REDELIVER_INTENT;
    }

    private void startup() {
        connectionManager = ConnectionManager.getInstance();

        outgoingPackets = new ConcurrentLinkedQueue<>();

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        final WifiP2pManager manager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        checkP2PManager(manager);
        p2pManager = manager;
        final WifiP2pManager.Channel c = manager.initialize(this, getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                //maybe retry connection in here
            }
        });
        channel = c;
        receiver = new WifiP2PBroadcastReceiver(manager, c);
        receiver.onConnectionChange(new WifiP2PBroadcastReceiver.ConnectionChangeCallback() {
            @Override
            public void onConnect(WifiP2pInfo wifiP2pInfo) {
                Log.d("connection", wifiP2pInfo.toString());
            }

            @Override
            public void onGroupInfo(WifiP2pGroup wifiP2pGroup) {
                Log.d("connection", wifiP2pGroup.toString());
                if(wifiP2pGroup.isGroupOwner()){
                    isServer = true;
                    try {
                        serverSocket = new ServerSocket(4200);
                        Socket client = serverSocket.accept();
                        client.getOutputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("connection","Something went wrong opening a socket!");
                    }
                }
                else{
                    isServer = false;
                }
            }

            @Override
            public void onDisconnect() {
                Log.d("connection","Disconnected from a device");
            }
        });
        receiver.onPeerChange(new WifiP2PBroadcastReceiver.PeerChangeCallback() {
            @Override
            public void onPeerChange(ArrayList<WifiP2pDevice> deviceList) {
                for(final WifiP2pDevice device : deviceList){
                    Log.d("discovery",device.deviceName+" @ "+device.deviceAddress);
                    WifiP2pConfig cfg = new WifiP2pConfig();
                    cfg.deviceAddress = device.deviceAddress;
                    if(ConnectionManager.getInstance().getDeviceByAddress(device.deviceAddress)==null) {
                        manager.connect(c, cfg, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                Log.d("connection", "successfully connected to "+device.deviceAddress);
                                connectionManager.addConnection(device);
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
        receiver.onP2PStateChange(new WifiP2PBroadcastReceiver.P2PStateChangeCallback() {
            @Override
            public void onStateChange(int state) {
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    // Wifi P2P is enabled
                } else {
                    Log.e("net", "WiFi P2P is not enabled!!");
                }
                //check if P2P is enabled and notify appropriate activity
            }
        });
        receiver.onThisDeviceChange(new WifiP2PBroadcastReceiver.ThisDeviceChangeCallback() {
            @Override
            public void onThisDeviceChanged(WifiP2pDevice thisDevice) {
                myAddress = thisDevice.deviceAddress;
                Log.d("net","My address: "+myAddress);
            }
        });

        registerReceiver(receiver, filter);

        startDiscovery();

        putNotification();
        started = true;
        binder = new NetServiceBinder();
    }

    private void shutdown() {
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        WifiP2pManager manager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        checkP2PManager(manager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            manager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d("service", "discovery successfully stopped");
                }

                @Override
                public void onFailure(int reasonCode) {
                    Log.w("service", "discovery could not be stopped! " + reasonCode);
                }
            });
            stopDiscovery();
        }
        manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("shutdown", "Current connection successfully canceled");
            }

            @Override
            public void onFailure(int i) {
                Log.e("shutdown", "Current connection couldn't be canceled: " + i);
            }
        });
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("shutdown", "Group removal successful");
            }

            @Override
            public void onFailure(int i) {
                Log.e("shutdown", "Group removal not successful! " + i);
            }
        });
        connectionManager.removeAllConnections();
        cancelNotification();
        started = false;
        stopSelf();
    }

    private void discover(){
        p2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("discovery", "Discovery Initiated");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.e("discovery","Discovery Failed : " + reasonCode);
            }
        });
    }

    private void startDiscovery(){
        discoveryTask.run();
    }

    private void stopDiscovery(){
        discoveryHandler.removeCallbacks(discoveryTask);
    }

    private Runnable discoveryTask = new Runnable() {
        @Override
        public void run() {
            try {
                discover();
            }
            finally {
                discoveryHandler.postDelayed(discoveryTask,500);
            }
        }
    };

    private void checkNotificationManager(NotificationManager manager) {
        if (manager == null) {
            throw new RuntimeException("Could not find a valid notification service!!");
        }
    }

    private void checkP2PManager(WifiP2pManager manager) {
        if (manager == null) {
            throw new RuntimeException("Could not find a valid WiFi P2P Service! Your device likely does not support P2P");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupNotificationChannel() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        checkNotificationManager(mNotificationManager);
        // The id of the channel.
        String id = getString(R.string.notification_channel_id);
        // The user-visible name of the channel.
        CharSequence name = getString(R.string.notification_channel_name);
        int importance = NotificationManager.IMPORTANCE_MIN;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        // Configure the notification channel.
        mChannel.enableLights(true);
        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        mNotificationManager.createNotificationChannel(mChannel);
    }

    public void queuePacket(Packet packet) {
        outgoingPackets.add(packet);
    }

    private void cancelNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        checkNotificationManager(manager);
        manager.cancel(mNotificationId);
    }

    private void putNotification() {
        // The id of the channel.
        String CHANNEL_ID = getString(R.string.notification_channel_id);

        Intent stopIntent = new Intent(this, ChatActivity.class);
        stopIntent.setAction(getString(R.string.action_request_stop));
        stopIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("Running")
                        .setContentText("Connected to 0 peers")
                        .setOngoing(true)
                        .addAction(R.drawable.ic_launcher_foreground, "Stop", PendingIntent.getActivity(this, 0, stopIntent, PendingIntent.FLAG_ONE_SHOT));
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, ChatActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your app to the Home screen.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(ChatActivity.class);

            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotificationChannel();
        }
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        checkNotificationManager(mNotificationManager);

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder.setChannelId(CHANNEL_ID);
        }
        mNotificationManager.notify(mNotificationId, mBuilder.build());
    }

    public class NetServiceBinder extends Binder {
        public NetService getNetService(){
            return NetService.this;
        }
    }

    public class ServerAsyncTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {



            return null;
        }
    }

}
