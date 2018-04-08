package io.github.appmakingbois.nodeboy.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.UUID;

import io.github.appmakingbois.nodeboy.R;
import io.github.appmakingbois.nodeboy.net.NetService;

public class ChatActivity extends AppCompatActivity {

    private boolean boundToNetService = false;
    private NetService netService = null;
    private UUID clientID = UUID.randomUUID();
    private ServiceConnection netServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            boundToNetService = true;
            NetService.NetServiceBinder binder = (NetService.NetServiceBinder) iBinder;
            netService = binder.getNetService();
            netService.setNetServiceEventListener(new NetService.NetServiceEventListener() {
                @Override
                public void onMessage(String message) {
                    insertIncomingMessage("yeet", message);
                }

                @Override
                public void onShutdown(int reason) {
                        returnToMain();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            boundToNetService = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if(getIntent()!=null){
            if(getIntent().getBooleanExtra("shutdown_requested",false)){
                shutdownRequest();
            }
        }
        EditText chatBox = findViewById(R.id.chatBox);


        WifiP2pManager manager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        if(manager == null){
            //p2p is unsupported, so we should just start up the main activity.
            //from there, the p2p checking will kick in and properly warn the user that p2p is unsupported.
            returnToMain();
        }
        else {
            //let's get the connection info and then start the service, passing the connection info to the service.
            WifiP2pManager.Channel channel = manager.initialize(this,getMainLooper(),null);
            manager.requestConnectionInfo(channel,connectionInfo -> {
                if(connectionInfo.groupFormed){
                    Intent serviceIntent = new Intent(this, NetService.class);
                    serviceIntent.setAction(getString(R.string.action_start));
                    serviceIntent.putExtra(getString(R.string.extra_p2p_connection_info),connectionInfo);
                    startService(serviceIntent);

                    findViewById(R.id.sendButton).setOnClickListener(view -> sendMessage());
                }
                else{
                    //no connection's been made, so this activity shouldn't be running.
                    //drop back to the main activity.
                    returnToMain();
                }
            });


        }
    }

    public void onNewIntent(Intent intent){
        if(intent!=null){
            if(intent.getAction()!=null && intent.getAction().equalsIgnoreCase(getString(R.string.action_request_stop))){
                shutdownRequest();
            }
        }
    }

    private void returnToMain(){
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(mainActivityIntent);
        finish();
    }

    private void shutdownRequest(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Shut Down NodeBoy?");
        builder.setMessage("This will disconnect you from the group, and will disconnect peers from each other if they were connected indirectly through your device.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                shutdown();
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    private void shutdown(){
        //network shutdown
        Intent intent = new Intent(this,NetService.class);
        intent.setAction(getString(R.string.action_stop));
        startService(intent);
        returnToMain();
    }

    public void onDestroy(){
        super.onDestroy();
    }

    private void sendMessage(){
        String message = ((EditText)findViewById(R.id.chatBox)).getText().toString().trim();
        if(boundToNetService){
            if(!message.isEmpty()){
            netService.sendMessage(message);
                insertOutgoingMessage("TODO implement some real name system",message);
                ((EditText)findViewById(R.id.chatBox)).setText("");
            }
        }
        else{
            Toast.makeText(this,"Not bound to service!!",Toast.LENGTH_SHORT).show();
        }
    }

    private void scrollDown(final ScrollView scrollLayout){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                int scrollPos = scrollLayout.getChildAt(scrollLayout.getChildCount()-1).getBottom() - scrollLayout.getHeight();
                scrollLayout.smoothScrollTo(0,scrollPos);
            }
        };
        scrollLayout.postDelayed(task,100);
    }

    private void insertOutgoingMessage(String senderID, String messageBody){
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout container = findViewById(R.id.chatContainer);
        LinearLayout messageView = (LinearLayout) inflater.inflate(R.layout.message_outgoing,null);
        TextView senderNameDisplay = messageView.findViewById(R.id.senderName);
        senderNameDisplay.setText(senderID);
        TextView messageBodyDisplay = messageView.findViewById(R.id.messageBody);
        messageBodyDisplay.setText(messageBody);
        TextView recievedTimeDisplay = messageView.findViewById(R.id.receivedTime);
        Date time = new Date();
        recievedTimeDisplay.setText(time.getHours()+":"+time.getMinutes());
        container.addView(messageView);
        ScrollView chatScrollView = findViewById(R.id.chatScrollView);
        scrollDown(chatScrollView);
    }
    private void insertIncomingMessage(String senderID, String messageBody){
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout container = findViewById(R.id.chatContainer);
        LinearLayout messageView = (LinearLayout) inflater.inflate(R.layout.message_incoming,null);
        TextView senderNameDisplay = messageView.findViewById(R.id.senderName);
        senderNameDisplay.setText(senderID);
        TextView messageBodyDisplay = messageView.findViewById(R.id.messageBody);
        messageBodyDisplay.setText(messageBody);
        TextView recievedTimeDisplay = messageView.findViewById(R.id.receivedTime);
        Date time = new Date();
        recievedTimeDisplay.setText(time.getHours()+":"+time.getMinutes());
        container.addView(messageView);
        ScrollView chatScrollView = findViewById(R.id.chatScrollView);
        scrollDown(chatScrollView);
    }

    /*private static class ShutdownBroadcastReceiver extends BroadcastReceiver{

        private ChatActivity chatActivity;

        public ShutdownBroadcastReceiver(@NonNull ChatActivity activity){
            this.chatActivity = activity;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction()!=null && intent.getAction().equalsIgnoreCase(NetService.STOP_ACTION)){
                chatActivity.finish();
            }
        }
    }*/

}
