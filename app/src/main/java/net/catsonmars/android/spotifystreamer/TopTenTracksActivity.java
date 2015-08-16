package net.catsonmars.android.spotifystreamer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import kaaes.spotify.webapi.android.models.Track;


/**
 * The code for retaining the fragment state is adapted from http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html
 */
public class TopTenTracksActivity extends ActionBarActivity
        implements NowPlayingFragment.TopTenTracksCallback {

    private final String TAG_LOG = "SPOTIFY_STREAMER";
    private static final String TAG_TRACKSFRAGMENT = "TFTAG";
    private static final String KEY_SUBTITLE = "KEY_SUBTITLE";

    //private TopTenTracksActivityFragment mTracksFragment;
    private String mSubTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG_LOG, "TopTenTracksActivity.onCreate");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_top_ten_tracks);

        if (null == savedInstanceState) {
            Intent intent = getIntent();

            Bundle args = new Bundle();
            args.putString("artist_id", intent.getStringExtra(Intent.EXTRA_TEXT));
            args.putBoolean("layout_twopane", intent.getBooleanExtra("layout_twopane", true));

            mSubTitle = intent.getStringExtra(Intent.EXTRA_TITLE);

            TopTenTracksActivityFragment f = new TopTenTracksActivityFragment();
            f.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_tracks_container, f, TAG_TRACKSFRAGMENT)
                    .commit();
        } else {
            if (savedInstanceState.containsKey(KEY_SUBTITLE)) {
                mSubTitle = savedInstanceState.getString(KEY_SUBTITLE);
            } else {
                mSubTitle = "";
            }
        }

        setSubTitle(mSubTitle);
    }


    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        Log.d(TAG_LOG, "TopTenTracksActivity.onSaveInstanceState");

        savedInstanceState.putString(KEY_SUBTITLE, mSubTitle);

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_ten_tracks, menu);

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

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                /*
                this is to make sure the screen the Up button returns us to is populated with the search results
                see http://stackoverflow.com/a/20306670 for details
                 */
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);

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
