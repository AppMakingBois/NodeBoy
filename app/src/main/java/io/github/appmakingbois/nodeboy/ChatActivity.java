package io.github.appmakingbois.nodeboy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import io.github.appmakingbois.nodeboy.net.NetService;
import io.github.appmakingbois.nodeboy.net.WifiP2PBroadcastReceiver;

public class ChatActivity extends AppCompatActivity {

    //private ShutdownBroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if(getIntent()!=null){
            if(getIntent().getAction()!=null && getIntent().getAction().equalsIgnoreCase(getString(R.string.action_request_stop))){
                shutdownRequest();
            }
        }
        EditText chatBox = findViewById(R.id.chatBox);

        //register();

        WifiP2pManager manager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        if(manager == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("WiFi P2P Not Supported");
            builder.setMessage("Unfortunately, we could not find a valid WiFi P2P service on your device. This usually means that WiFi P2P is not supported on your device. This application requires WiFi P2P to work, so you will be unable to use this app. We apologize for the inconvenience.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            builder.show();
        }
        else {

            Intent serviceIntent = new Intent(this, NetService.class);
            serviceIntent.setAction(getString(R.string.action_start));
            startService(serviceIntent);

            findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendMessage();
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

    private void shutdownRequest(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Shut Down NodeBoy?");
        builder.setMessage("This will disconnect you from the network, and will disconnect peers from each other if they were connected indirectly through your device.");
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
        finish();
    }

    /*public void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void onResume(){
        super.onResume();
        register();
    }

    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void register(){
        if(receiver==null) {
            receiver = new ShutdownBroadcastReceiver(this);
        }
        IntentFilter intentFilter = new IntentFilter(NetService.STOP_ACTION);
        registerReceiver(receiver,intentFilter);
    }*/

    private void sendMessage(){
        String message = ((EditText)findViewById(R.id.chatBox)).getText().toString().trim();
        if(!message.isEmpty()){
            insertOutgoingMessage("TODO implement some real name system",message);
            ((EditText)findViewById(R.id.chatBox)).setText("");
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
