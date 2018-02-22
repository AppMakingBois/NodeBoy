package io.github.appmakingbois.nodeboy;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        EditText chatBox = findViewById(R.id.chatBox);

        Intent serviceIntent = new Intent(this, NetService.class);
        serviceIntent.setAction(NetService.START_ACTION);
        startService(serviceIntent);

        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }



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
}
