package io.github.appmakingbois.nodeboy.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;

import io.github.appmakingbois.nodeboy.R;
import io.github.appmakingbois.nodeboy.fragment.DeviceListFragment;
import io.github.appmakingbois.nodeboy.fragment.P2PCheckFragment;
import io.github.appmakingbois.nodeboy.fragment.P2PFailFragment;

public class MainActivity extends AppCompatActivity implements DeviceListFragment.OnFragmentInteractionListener, P2PFailFragment.OnFragmentInteractionListener, P2PCheckFragment.OnFragmentInteractionListener {

    private int menuRes;
    private int titleRes;

    private int fragmentDisplayed;
    private static final int P2P_CHECK_FRAGMENT = 0;
    private static final int P2P_FAIL_FRAGMENT = 1;
    private static final int DEVICE_LIST_FRAGMENT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            displayFragment(new P2PCheckFragment());
        }
        setContentView(R.layout.activity_main);
    }

    public void displayFragment(Fragment fragment, boolean addToBackStack) {
        if (fragment instanceof P2PCheckFragment) {
            fragmentDisplayed = P2P_CHECK_FRAGMENT;
            titleRes = P2PCheckFragment.TITLE;
            menuRes = P2PCheckFragment.MENU;
        }
        else if (fragment instanceof P2PFailFragment) {
            fragmentDisplayed = P2P_FAIL_FRAGMENT;
            titleRes = P2PFailFragment.TITLE;
            menuRes = P2PFailFragment.MENU;
        }
        else if (fragment instanceof DeviceListFragment) {
            fragmentDisplayed = DEVICE_LIST_FRAGMENT;
            titleRes = DeviceListFragment.TITLE;
            menuRes = DeviceListFragment.MENU;
        }
        else{
            return;
        }

        setTitle(titleRes);
        FragmentManager fm = getSupportFragmentManager();
        if(addToBackStack) {
            fm.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
        else{
            fm.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    public void displayFragment(Fragment fragment){
        displayFragment(fragment,false);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(menuRes,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}