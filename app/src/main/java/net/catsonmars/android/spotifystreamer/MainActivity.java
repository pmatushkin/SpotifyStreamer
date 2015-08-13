package net.catsonmars.android.spotifystreamer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;


/**
 * The code for retaining the fragment state is adapted from http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html
 */
public class MainActivity extends ActionBarActivity
        implements ArtistsFragment.Callback,
        NowPlayingFragment.TopTenTracksCallback {

    private final String LOG_TAG = "SPOTIFY_STREAMER";
    private static final String TAG_FRAGMENT = "artists_fragment";
    private static final String TAG_TRACKSFRAGMENT = "TFTAG";

    private ArtistsFragment mArtistsFragment;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (null == findViewById(R.id.fragment_tracks_container)) {
            Log.d(LOG_TAG, "initializing phone UI");

            mTwoPane = false;
        } else {
            Log.d(LOG_TAG, "initializing tablet UI");

            mTwoPane = true;

            if (null == savedInstanceState) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_tracks_container, new TopTenTracksActivityFragment(), TAG_TRACKSFRAGMENT)
                        .commit();
            }
        }

        FragmentManager fm = getSupportFragmentManager();
        mArtistsFragment = (ArtistsFragment)fm.findFragmentByTag(TAG_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (null == mArtistsFragment) {
            mArtistsFragment = new ArtistsFragment();
            fm.beginTransaction().add(mArtistsFragment, TAG_FRAGMENT).commit();
        }
    }

    public void onItemSelected(Artist artist) {
        if (true == mTwoPane) {
            // on tablet, replace DetailFragment
            Bundle args = new Bundle();
            args.putString("artist_id", artist.id);
            args.putString("artist_name", artist.name);
            args.putBoolean("layout_twopane", mTwoPane);

            TopTenTracksActivityFragment f = new TopTenTracksActivityFragment();
            f.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_tracks_container, f, TAG_TRACKSFRAGMENT)
                    .commit();

            setSubTitle(artist.name);
        } else {
            // on phone, launch DetailActivity
            Intent intent = new Intent(this, TopTenTracksActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, artist.id);
            intent.putExtra(Intent.EXTRA_TITLE, artist.name);
            intent.putExtra("layout_twopane", mTwoPane);

            startActivity(intent);
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

    private void setSubTitle(String subTitle) {
        ActionBar ab = getSupportActionBar();

        if (null != ab)
            ab.setSubtitle(subTitle);
    }

    public Activity getActivity() {
        return this;
    }

    public Track getCurrentTrack() {
        Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_TRACKSFRAGMENT);

        if (null == f) {
            return null;
        } else {
            ListView listView = (ListView)this.findViewById(R.id.lvTracks);

            if (null == listView) {
                return null;
            } else {
                int position = ((TopTenTracksActivityFragment)f).getSelectedItemPosition();

                if (-1 == position) {
                    return null;
                } else {
                    return (Track) listView.getItemAtPosition(position);
                }
            }
        }
    }

    public Boolean moveToPreviousTrack(){
        TopTenTracksActivityFragment f = (TopTenTracksActivityFragment)getSupportFragmentManager().findFragmentByTag(TAG_TRACKSFRAGMENT);

        if (null == f) {
            // Top Ten Tracks fragment is not found; return
            return false;
        } else {
            ListView listView = (ListView)this.findViewById(R.id.lvTracks);

            if (null == listView) {
                // Top Ten Tracks list is not found; return
                return false;
            } else {
                int position = f.getSelectedItemPosition();

                if (-1 == position) {
                    // Top Ten Tracks list item position is not initialized; return
                    return false;
                } else {
                    // attempt to move to the next item in the Top Ten Tracks list
                    position = position - 1;

                    if (position < 0) {
                        // there is no item to move to; return
                        return false;
                    } else {
                        // move the position of the selected item; return result
                        return f.setSelectedItemPosition(position);
                    }
                }
            }
        }
    }

    public Boolean moveToNextTrack(){
        TopTenTracksActivityFragment f = (TopTenTracksActivityFragment)getSupportFragmentManager().findFragmentByTag(TAG_TRACKSFRAGMENT);

        if (null == f) {
            // Top Ten Tracks fragment is not found; return
            return false;
        } else {
            ListView listView = (ListView)this.findViewById(R.id.lvTracks);

            if (null == listView) {
                // Top Ten Tracks list is not found; return
                return false;
            } else {
                int position = f.getSelectedItemPosition();

                if (-1 == position) {
                    // Top Ten Tracks list item position is not initialized; return
                    return false;
                } else {
                    // attempt to move to the next item in the Top Ten Tracks list
                    position = position + 1;

                    if (listView.getAdapter().getCount() <= position) {
                        // there is no item to move to; return
                        return false;
                    } else {
                        // move the position of the selected item; return result
                        return f.setSelectedItemPosition(position);
                    }
                }
            }
        }
    }
}
