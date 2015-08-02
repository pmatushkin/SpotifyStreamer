package net.catsonmars.android.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


/**
 * The code for retaining the fragment state is adapted from http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html
 */
public class MainActivity extends ActionBarActivity {

    private final String LOG_TAG = "SPOTIFY_STREAMER";
    private static final String TAG_FRAGMENT = "artists_fragment";
    private final String TRACKSFRAGMENT_TAG = "TFTAG";

    private ArtistsFragment mArtistsFragment;
    private boolean mTwoPanes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (null == findViewById(R.id.fragment_tracks_container)) {
            Log.d(LOG_TAG, "initializing phone UI");

            mTwoPanes = false;
        } else {
            Log.d(LOG_TAG, "initializing tablet UI");

            mTwoPanes = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_tracks_container, new TopTenTracksActivityFragment(), TRACKSFRAGMENT_TAG)
                        .commit();
            }
        }

        FragmentManager fm = getSupportFragmentManager();
        mArtistsFragment = (ArtistsFragment)fm.findFragmentByTag(TAG_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mArtistsFragment == null) {
            mArtistsFragment = new ArtistsFragment();
            fm.beginTransaction().add(mArtistsFragment, TAG_FRAGMENT).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
