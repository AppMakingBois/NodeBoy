package io.github.appmakingbois.nodeboy;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.TaskStackBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link P2PFailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link P2PFailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class P2PFailFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String P2P_FAILURE_REASON = "p2p_fail_reason";
    public static final String P2P_REASON_UNSUPPORTED = "p2p_unsupported";
    public static final String P2P_REASON_DISABLED = "p2p_disabled";

    // TODO: Rename and change types of parameters
    private String p2pFailureReason;

    private OnFragmentInteractionListener mListener;

    public P2PFailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param p2pFailureReason The reason why initialization of P2P failed.
     * @return A new instance of fragment P2PFailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static P2PFailFragment newInstance(String p2pFailureReason) {
        P2PFailFragment fragment = new P2PFailFragment();
        Bundle args = new Bundle();
        args.putString(P2P_FAILURE_REASON, p2pFailureReason);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            p2pFailureReason = getArguments().getString(P2P_FAILURE_REASON);
        }
    }

    private String validateReason(String reason){
        if(reason==null || !(reason.equalsIgnoreCase(P2P_REASON_DISABLED)||reason.equalsIgnoreCase(P2P_REASON_UNSUPPORTED))){
            reason = P2P_REASON_DISABLED;
        }
        return reason;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_p2p_fail, container, false);

        Button openWifiSettingsButton = v.findViewById(R.id.open_wifi_settings_button);
        openWifiSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent wifiSettingsIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                TaskStackBuilder.create(getActivity())
                        .addNextIntent(new Intent(getContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK))
                        .addNextIntent(wifiSettingsIntent)
                        .startActivities();
            }
        });

        String reason = validateReason(p2pFailureReason);
        if(reason.equalsIgnoreCase(P2P_REASON_DISABLED)){
            v.findViewById(R.id.open_wifi_settings_button).setVisibility(View.VISIBLE);
            v.findViewById(R.id.p2p_not_enabled_textview).setVisibility(View.VISIBLE);
            v.findViewById(R.id.p2p_not_supported_textview).setVisibility(View.INVISIBLE);

            v.findViewById(R.id.open_wifi_settings_button).setEnabled(true);
            v.findViewById(R.id.p2p_not_enabled_textview).setEnabled(true);
            v.findViewById(R.id.p2p_not_supported_textview).setEnabled(false);
        }else if (reason.equalsIgnoreCase(P2P_REASON_UNSUPPORTED)){
            v.findViewById(R.id.open_wifi_settings_button).setVisibility(View.INVISIBLE);
            v.findViewById(R.id.p2p_not_enabled_textview).setVisibility(View.INVISIBLE);
            v.findViewById(R.id.p2p_not_supported_textview).setVisibility(View.VISIBLE);

            v.findViewById(R.id.open_wifi_settings_button).setEnabled(false);
            v.findViewById(R.id.p2p_not_enabled_textview).setEnabled(false);
            v.findViewById(R.id.p2p_not_supported_textview).setEnabled(true);
        }
        return v;
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
}
