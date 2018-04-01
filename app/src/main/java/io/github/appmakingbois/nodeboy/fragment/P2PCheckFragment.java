package io.github.appmakingbois.nodeboy.fragment;

import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.appmakingbois.nodeboy.R;
import io.github.appmakingbois.nodeboy.activity.MainActivity;
import io.github.appmakingbois.nodeboy.net.WifiP2PBroadcastReceiver;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link P2PCheckFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link P2PCheckFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class P2PCheckFragment extends Fragment {

    public static final int TITLE = R.string.p2p_check_title;
    public static final int MENU = R.menu.menu_p2p_check;


    private WifiP2PBroadcastReceiver receiver;
    private OnFragmentInteractionListener mListener;

    public P2PCheckFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment P2PCheckFragment.
     */
    public static P2PCheckFragment newInstance() {
        P2PCheckFragment fragment = new P2PCheckFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        checkP2P();
        return inflater.inflate(R.layout.fragment_p2p_check, container, false);
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

    public void onDestroy() {
        getActivity().unregisterReceiver(receiver);
        super.onDestroy();
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


    private void checkP2P() {
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            //first test: see if we can obtain a service. If we can't, then start fail fragment with "p2p not supported" error
            WifiP2pManager manager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
            if (manager == null) {
                activity.displayFragment(P2PFailFragment.newInstance(P2PFailFragment.P2P_REASON_UNSUPPORTED));
            }
            else {
                WifiP2pManager.Channel channel = manager.initialize(getContext(), Looper.getMainLooper(), null);
                IntentFilter intentFilter = new IntentFilter(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
                receiver = new WifiP2PBroadcastReceiver(manager, channel);
                receiver.onP2PStateChange(state -> {
                    if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                        activity.displayFragment(P2PFailFragment.newInstance(P2PFailFragment.P2P_REASON_DISABLED));
                    }
                    else {
                        activity.displayFragment(DeviceListFragment.newInstance());
                    }
                });
                getActivity().registerReceiver(receiver, intentFilter);
            }
        }
    }
}
