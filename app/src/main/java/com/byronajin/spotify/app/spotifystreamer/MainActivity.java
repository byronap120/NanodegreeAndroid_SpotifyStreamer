package com.byronajin.spotify.app.spotifystreamer;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity implements MainActivityFragment.OnArtistSelectedListener {

    private boolean mTwoPane;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (findViewById(R.id.topTrackContainer) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.topTrackContainer, new TopTracksFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
         //   getSupportActionBar().setElevation(0f);
        }


    }

    @Override
    public void artistSelected(String trackID , String artistName) {

        if(mTwoPane){
            TopTracksFragment articleFrag = (TopTracksFragment)
                    getSupportFragmentManager().findFragmentById(R.id.topTrackContainer);
                articleFrag.updateListTracks(trackID,artistName);
        }else{
               Intent intent = new Intent(this, TopTracks.class)
                        .putExtra(Intent.EXTRA_TEXT, trackID)
                        .putExtra("artistName", artistName);
                startActivity(intent);
        }

    }

}
