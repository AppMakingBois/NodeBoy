package io.github.appmakingbois.nodeboy.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.appmakingbois.nodeboy.R;
import io.github.appmakingbois.nodeboy.activity.MainActivity;
import io.github.appmakingbois.nodeboy.net.WifiP2PBroadcastReceiver;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DeviceListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DeviceListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@SuppressWarnings("Convert2MethodRef")
public class DeviceListFragment extends Fragment {

    public static final int TITLE = R.string.device_list_title;
    public static final int MENU = R.menu.menu_device_list;

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2PBroadcastReceiver receiver;

    private MainActivity activity;

    private WifiP2pDevice thisDevice;

    private OnFragmentInteractionListener mListener;

    public DeviceListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DeviceListFragment.
     */

    public static DeviceListFragment newInstance() {
        DeviceListFragment fragment = new DeviceListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public boolean onOptionsItemSelected(MenuItem item){

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            activity = (MainActivity) getActivity();
            manager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
            if(manager == null){
                activity.displayFragment(P2PFailFragment.newInstance(P2PFailFragment.P2P_REASON_UNSUPPORTED));
                return;
            }
            channel = manager.initialize(activity, activity.getMainLooper(), () -> {
                activity.displayFragment(P2PCheckFragment.newInstance());
            });

            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
            filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
            filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
            filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

            receiver = new WifiP2PBroadcastReceiver(manager,channel);
            receiver.onP2PStateChange(state -> {
                if(state == WifiP2pManager.WIFI_P2P_STATE_DISABLED){
                    activity.displayFragment(P2PFailFragment.newInstance(P2PFailFragment.P2P_REASON_DISABLED));
                }
            });
            receiver.onPeerChange(peers -> {
                renderPeers(peers);
            });
            receiver.onConnectionChange(new WifiP2PBroadcastReceiver.ConnectionChangeCallback() {
                @Override
                public void onConnect(WifiP2pInfo wifiP2pInfo) {

                }

                @Override
                public void onGroupInfo(WifiP2pGroup wifiP2pGroup) {

                }

                @Override
                public void onDisconnect() {

                }
            });
            receiver.onThisDeviceChange(thisDevice -> {
                this.thisDevice = thisDevice;
                updateThisDevice(activity.findViewById(R.id.fragment_container));
            });

            activity.registerReceiver(receiver,filter);
        }
    }

    public void onDestroy(){
        if(receiver!=null) {
            activity.unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    private void updateThisDevice(View v){
        Activity activity = getActivity();
        if(activity!=null) {
            if (thisDevice == null) {
                v.findViewById(R.id.label_waiting_for_device_info).setEnabled(true);
                v.findViewById(R.id.progress_waiting_for_device_info).setEnabled(true);
                v.findViewById(R.id.label_this_device_name).setEnabled(false);
                v.findViewById(R.id.label_this_device_address).setEnabled(false);

                v.findViewById(R.id.label_waiting_for_device_info).setVisibility(View.VISIBLE);
                v.findViewById(R.id.progress_waiting_for_device_info).setVisibility(View.VISIBLE);
                v.findViewById(R.id.label_this_device_name).setVisibility(View.INVISIBLE);
                v.findViewById(R.id.label_this_device_address).setVisibility(View.INVISIBLE);
            }
            else{
                v.findViewById(R.id.label_waiting_for_device_info).setEnabled(false);
                v.findViewById(R.id.progress_waiting_for_device_info).setEnabled(false);
                v.findViewById(R.id.label_this_device_name).setEnabled(true);
                v.findViewById(R.id.label_this_device_address).setEnabled(true);

                v.findViewById(R.id.label_waiting_for_device_info).setVisibility(View.INVISIBLE);
                v.findViewById(R.id.progress_waiting_for_device_info).setVisibility(View.INVISIBLE);
                v.findViewById(R.id.label_this_device_name).setVisibility(View.VISIBLE);
                v.findViewById(R.id.label_this_device_address).setVisibility(View.VISIBLE);

                TextView thisDeviceName = v.findViewById(R.id.label_this_device_name);
                thisDeviceName.setText(thisDevice.deviceName);
                TextView thisDeviceAddress = v.findViewById(R.id.label_this_device_address);
                thisDeviceAddress.setText(thisDevice.deviceAddress);
            }
        }
    }

    private void renderPeers(ArrayList<WifiP2pDevice> peers){

    }

    private void searchForPeers(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            manager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    discoverPeers();
                }

                @Override
                public void onFailure(int reason) {
                    if(reason == WifiP2pManager.P2P_UNSUPPORTED){
                        activity.displayFragment(P2PFailFragment.newInstance(P2PFailFragment.P2P_REASON_UNSUPPORTED));
                        return;
                    }
                    Log.e("discovery","Stopping ongoing search for peers failed! Reason: "+reason);
                }
            });
        }
        else {
           discoverPeers();
        }
    }

    private void discoverPeers(){
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //nothing to do here, we'll be notified by the broadcast receiver
            }

            @Override
            public void onFailure(int reason) {
                if(reason == WifiP2pManager.P2P_UNSUPPORTED){
                    activity.displayFragment(P2PFailFragment.newInstance(P2PFailFragment.P2P_REASON_UNSUPPORTED));
                    return;
                }
                Log.e("discovery","Searching for peers failed! Reason: "+reason);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_device_list, container, false);
        updateThisDevice(v);
        return v;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
