package io.github.appmakingbois.nodeboy.net;


import android.os.AsyncTask;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerAsyncTask extends AsyncTask<Void,Void,Void> {
    private ServerSocket serverSocket;
    private boolean listening = false;
    @Override
    protected Void doInBackground(Void... voids) {
        setupServer();
        waitForConnections();

        return null;
    }

    public void stop(){
        listening = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void waitForConnections(){
        if(serverSocket!=null){
            try {
                listening = true;
                while(listening){
                    Socket newConn = serverSocket.accept();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface ConnectionListener{
        public void onConnect(Socket socket);
    }

    private void setupServer(){
        try {
            serverSocket = new ServerSocket(4200);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}