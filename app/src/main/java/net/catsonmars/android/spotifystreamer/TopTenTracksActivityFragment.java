package net.catsonmars.android.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * The code for retaining the fragment state is adapted from http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html
 */
public class TopTenTracksActivityFragment extends Fragment {
    private static final String TAG_LOG = "SPOTIFY_STREAMER";
    private static final String TAG_NOWPLAYING = "NOW PLAYING";

    TopTenTracksAdapter mTracksAdapter;
    private String mSearchArgument;
    private String mArtistID;
    private Boolean mTwoPane;

    // The list to retain the previous search results across configuration changes
    ArrayList<Track> mSpotifyTracks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG_LOG, "TopTenTracksActivityFragment.onCreate");

        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG_LOG, "TopTenTracksActivityFragment.onCreateView");

        if (mSpotifyTracks == null)
            mSpotifyTracks = new ArrayList<>();

        mTracksAdapter = new TopTenTracksAdapter(
                getActivity(),
                R.layout.list_item_track);

        View rootView = inflater.inflate(R.layout.fragment_top_ten_tracks, container, false);

        ListView listView = (ListView)rootView.findViewById(R.id.lvTracks);
        listView.setAdapter(mTracksAdapter);

        mArtistID = "";
        Bundle args = getArguments();
        if (null == args) {
            Log.d(TAG_LOG, "TopTenTracksActivityFragment.onCreateView... NOT found arguments");
        } else {
            Log.d(TAG_LOG, "TopTenTracksActivityFragment.onCreateView... found arguments");

            mArtistID = args.getString("artist_id");
            mTwoPane = args.getBoolean("layout_twopane");
        }

        fetchTopTenTracks(mArtistID);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mTwoPane) {
                    // Create the fragment and show it as a dialog.
                    DialogFragment newFragment = new NowPlayingFragment();
                    newFragment.show(getFragmentManager(), TAG_NOWPLAYING);
                } else {
                    DialogFragment newFragment = new NowPlayingFragment();

                    // The device is smaller, so show the fragment fullscreen
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    // For a little polish, specify a transition animation
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    // To make it fullscreen, use the 'content' root view as the container
                    // for the fragment, which is always the root view for the activity
                    transaction.add(android.R.id.content, newFragment, TAG_NOWPLAYING)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        return rootView;
    }

    private void fetchTopTenTracks(String searchArgument) {
        if (!searchArgument.isEmpty()) {
            if (searchArgument.equals(this.mSearchArgument)) {
                mTracksAdapter.clear();
                mTracksAdapter.addAll(mSpotifyTracks);
            } else {
                this.mSearchArgument = searchArgument;

                FetchTopTenTracksTask task = new FetchTopTenTracksTask();
                task.execute(searchArgument);
            }
        }
    }

    public class FetchTopTenTracksTask extends AsyncTask<String, Void, ArrayList<Track>> {
        @Override
        protected ArrayList<Track> doInBackground(String... params) {
            ArrayList<Track> spotifyTracks = new ArrayList<Track>();

            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("country", "US");

            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();

                Log.d(TAG_LOG, "searching for the top 10 tracks on Spotify");
                Tracks tracks = spotify.getArtistTopTrack(params[0], queryMap);

                spotifyTracks.addAll(tracks.tracks);
            }
            catch (Exception e) {
                Log.e(TAG_LOG, e.getMessage());
                spotifyTracks = null;
            }

            return spotifyTracks;
        }

        @Override
        protected void onPostExecute(ArrayList<Track> tracks) {
            if (null == tracks) {
                Toast toast = Toast.makeText(getActivity(), getString(R.string.error_search_top_tracks), Toast.LENGTH_SHORT);
                toast.show();
            } else {
                mSpotifyTracks.clear();
                mSpotifyTracks.addAll(tracks);

                mTracksAdapter.clear();
                mTracksAdapter.addAll(tracks);

                if (mTracksAdapter.isEmpty()) {
                    Toast toast = Toast.makeText(getActivity(), getString(R.string.warn_empty_search_top_tracks), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }
    }
}
