package net.catsonmars.android.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    TopTenTracksAdapter mTracksAdapter;
    private String mSearchArgument;

    // The list to retain the previous search results across configuration changes
    ArrayList<Track> mSpotifyTracks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG_LOG, "in TopTenTracksActivityFragment.onCreate");

        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG_LOG, "in TopTenTracksActivityFragment.onCreateView");

        if (mSpotifyTracks == null)
            mSpotifyTracks = new ArrayList<>();

        mTracksAdapter = new TopTenTracksAdapter(
                getActivity(),
                R.layout.list_item_track);

        View rootView = inflater.inflate(R.layout.fragment_top_ten_tracks, container, false);

        ListView listView = (ListView)rootView.findViewById(R.id.lvTracks);
        listView.setAdapter(mTracksAdapter);

        String artistID = "";
        Bundle args = getArguments();
        if (null == args) {
            Log.d(TAG_LOG, "in TopTenTracksActivityFragment.onCreateView... NOT found arguments");
        } else {
            Log.d(TAG_LOG, "in TopTenTracksActivityFragment.onCreateView... found arguments");

            artistID = args.getString("artist_id");
        }

        fetchTopTenTracks(artistID);

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
