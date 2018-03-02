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
import android.graphics.drawable.Drawable;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import io.github.appmakingbois.nodeboy.ChatActivity;
import io.github.appmakingbois.nodeboy.R;


public class NetService extends Service {

    private int mNotificationId = 42069;

    private WifiP2PBroadcastReceiver receiver;
    private WifiP2pManager.Channel channel;

    private boolean started = false;

    private ConnectionManager connectionManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(getString(R.string.action_start))) {
                if (!started) {
                    Log.d("service", "starting");
                    startup();
                    putNotification();
                    started = true;
                }
            }
            else if (intent.getAction().equals(getString(R.string.action_stop))) {
                Log.d("service", "stopping");
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
                }
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
        }
        return START_REDELIVER_INTENT;
    }

    private void startup() {
        connectionManager = ConnectionManager.getInstance();

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        WifiP2pManager manager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        checkP2PManager(manager);
        WifiP2pManager.Channel c = manager.initialize(this, getMainLooper(), null);
        channel = c;
        receiver = new WifiP2PBroadcastReceiver(manager, c);

        registerReceiver(receiver, filter);

        WifiP2pManager.ActionListener listener = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("discovery", "Discovery Initiated");
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(getApplicationContext(), "Discovery Failed : " + reasonCode,
                        Toast.LENGTH_SHORT).show();
            }
        };

        manager.discoverPeers(c, listener);
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
        String id = "nodeboy_notification_channel";
        // The user-visible name of the channel.
        CharSequence name = "NodeBoy";
        // The user-visible description of the channel.
        String description = "Persistent ongoing notification";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
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
        manager.cancel(mNotificationId);
    }

    private void putNotification() {
        // The id of the channel.
        String CHANNEL_ID = "nodeboy_notification_channel";

        Intent stopIntent = new Intent(this, ChatActivity.class);
        stopIntent.setAction(getString(R.string.action_request_stop));
        stopIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Drawable d = getResources().getDrawable(R.drawable.ic_launcher_foreground);
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
}
