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
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import io.github.appmakingbois.nodeboy.activity.ChatActivity;
import io.github.appmakingbois.nodeboy.R;


public class NetService extends Service {

    private static final int NOTIFICATION_ID = 42069;

    public static final int SERVER_PORT = 4200;

    private static final int STATE_NOT_RUNNING = -1;
    private static final int STATE_STARTING_UP = 0;
    private static final int STATE_SHUTTING_DOWN = 1;
    private static final int STATE_RUNNING = 2;
    private static final int STATE_P2P_DISABLED = 3;
    private int currentState = STATE_NOT_RUNNING;

    private WifiP2PBroadcastReceiver receiver;
    private WifiP2pManager.Channel channel;
    private WifiP2pManager manager;

    private boolean started = false;

    private NetServiceBinder binder;

    private String myHardwareAddress;

    private boolean isServer = false;

    private NetServiceEventListener netServiceEventListener;

    private NodeBoyServer server;
    private NodeBoyClient client;


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
                    WifiP2pInfo info = intent.getParcelableExtra(getString(R.string.extra_p2p_connection_info));
                    if (info != null) {
                        Log.d("service", "starting");
                        startup(info);
                    }
                    else {
                        //whoever started this service didn't give us wifi p2p connection info, so let's abort
                        stopSelf();
                    }
                }
            }
            else if (intent.getAction().equals(getString(R.string.action_stop))) {
                Log.d("service", "stopping");
                shutdown();
            }
        }
        return START_REDELIVER_INTENT;
    }

    private void startup(WifiP2pInfo info) {
        currentState = STATE_STARTING_UP;
        putNotification(currentState, 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        final WifiP2pManager manager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        checkP2PManager(manager);
        this.manager = manager;
        final WifiP2pManager.Channel c = manager.initialize(this, getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                //maybe retry connection in here
            }
        });
        channel = c;
        receiver = new WifiP2PBroadcastReceiver(manager, c);

        receiver.onP2PStateChange(state -> {
            if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                Log.e("net", "WiFi P2P is not enabled!!");
                this.currentState = STATE_P2P_DISABLED;
                putNotification(currentState, 0);
                if (netServiceEventListener != null) {
                    netServiceEventListener.onShutdown(NetServiceEventListener.SHUTDOWN_P2P_DISABLED);
                }
                //if P2P gets disabled, that means that everyone will be disconnected anyway. we should shut down the service.
                shutdown();
            }
        });

        receiver.onThisDeviceChange(thisDevice -> {
            myHardwareAddress = thisDevice.deviceAddress;
            Log.d("net", "My address: " + myHardwareAddress);
        });

        registerReceiver(receiver, filter);

        isServer = info.isGroupOwner;

        try {
            if (isServer) {
                Log.d("inet","IPv4? "+String.valueOf(info.groupOwnerAddress instanceof Inet4Address));
                Log.d("server","This device is the server");
                server = new NodeBoyServer(new InetSocketAddress(info.groupOwnerAddress,SERVER_PORT));
                server.start();
                URI clientURI = new URI("ws://" + info.groupOwnerAddress.getHostAddress() + ":" + SERVER_PORT);
                client = makeClient(clientURI);
                Log.d("client","Client connecting to "+clientURI.toASCIIString());
            }
            else {
                Log.d("inet","IPv4? "+String.valueOf(info.groupOwnerAddress instanceof Inet4Address));
                URI clientURI = new URI("ws://" + info.groupOwnerAddress.getHostAddress() + ":" + SERVER_PORT);
                client = makeClient(clientURI);
                Log.d("client","Client connecting to "+clientURI.toASCIIString());
            }
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
        client.connect();
        currentState = STATE_RUNNING;
        putNotification(currentState, 0);
        started = true;
        binder = new NetServiceBinder();
    }

    private void shutdown() {
        currentState = STATE_SHUTTING_DOWN;
        putNotification(currentState, 0);
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        WifiP2pManager manager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        checkP2PManager(manager);
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
        cancelNotification();
        try {
            if(server!=null) {
                server.stop();
            }
            client.close();
            started = false;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            stopSelf();
        }
    }

    private NodeBoyClient makeClient(@NonNull URI uri) {
        return new NodeBoyClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                //yeet, we are connected
                Log.d("client","Client is connected");
            }

            @Override
            public void onMessage(String message) {
                if (netServiceEventListener != null) {
                    netServiceEventListener.onMessage(message);
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {

            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }
        };
    }

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

    private void cancelNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        checkNotificationManager(manager);
        manager.cancel(NOTIFICATION_ID);
    }

    private void putNotification(int state, int clients) {
        // The id of the channel.
        String CHANNEL_ID = getString(R.string.notification_channel_id);

        Intent stopIntent = new Intent(this, ChatActivity.class);
        stopIntent.putExtra("shutdown_requested", true);
        stopIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_service_notification);
        switch (state) {
            case STATE_RUNNING:
                mBuilder.setContentTitle("Running")
                        .setContentText("Connected to 0 peers")
                        .setOngoing(true)
                        .addAction(R.drawable.ic_service_notification, "Stop", PendingIntent.getActivity(this, 0, stopIntent, PendingIntent.FLAG_ONE_SHOT));
        }
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

        // NOTIFICATION_ID is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder.setChannelId(CHANNEL_ID);
        }
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public interface NetServiceEventListener {
        void onMessage(String message);

        int SHUTDOWN_P2P_DISABLED = 1;
        int SHUTDOWN_OTHER_ERROR = 2;

        void onShutdown(int reason);
    }

    public void setNetServiceEventListener(NetServiceEventListener listener) {
        this.netServiceEventListener = listener;
    }

    public void sendMessage(String message) {
        client.send(message);
    }

    public class NetServiceBinder extends Binder {
        public NetService getNetService() {
            return NetService.this;
        }
    }


}
