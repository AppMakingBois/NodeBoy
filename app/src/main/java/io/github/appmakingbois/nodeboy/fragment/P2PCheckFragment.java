package io.github.appmakingbois.nodeboy.fragment;

import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.appmakingbois.nodeboy.R;
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
    // TODO: Rename and change types and number of parameters
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

    // TODO: Rename method, update argument and hook method into UI event
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    private void checkP2P(){
        //first test: see if we can obtain a service. If we can't, then start fail fragment with "p2p not supported" error
        WifiP2pManager manager = (WifiP2pManager) this.getActivity().getSystemService(Context.WIFI_P2P_SERVICE);
        if(manager == null){
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.fragment_container,P2PFailFragment.newInstance(P2PFailFragment.P2P_REASON_UNSUPPORTED)).commit();
        }
        else{
            WifiP2pManager.Channel channel = manager.initialize(getContext(), Looper.getMainLooper(), null);
            IntentFilter intentFilter = new IntentFilter(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
            receiver = new WifiP2PBroadcastReceiver(manager, channel);
            receiver.onP2PStateChange(state -> {
                if(state == WifiP2pManager.WIFI_P2P_STATE_DISABLED){
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    fm.beginTransaction().replace(R.id.fragment_container,P2PFailFragment.newInstance(P2PFailFragment.P2P_REASON_DISABLED)).commit();
                }
                else{
                    //success! p2p exists and is enabled.
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    //todo add some place to go when this test succeeds
                }
            });
            getActivity().registerReceiver(receiver,intentFilter);
        }
    }
}
