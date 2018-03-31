package io.github.appmakingbois.nodeboy.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import io.github.appmakingbois.nodeboy.R;
import io.github.appmakingbois.nodeboy.fragment.P2PCheckFragment;
import io.github.appmakingbois.nodeboy.fragment.P2PFailFragment;

public class MainActivity extends AppCompatActivity implements P2PFailFragment.OnFragmentInteractionListener,P2PCheckFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .add(R.id.fragment_container, P2PCheckFragment.newInstance())
                    .commit();
        }
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}