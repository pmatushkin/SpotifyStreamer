package net.catsonmars.android.spotifystreamer;

import android.content.Intent;
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

    String LOG_TAG = "";
    TopTenTracksAdapter mTracksAdapter;
    private String mSearchArgument;
    ArrayList<Track> mSpotifyTracks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LOG_TAG = getString(R.string.app_log_tag);

        if (mSpotifyTracks == null)
            mSpotifyTracks = new ArrayList<Track>();

        mTracksAdapter = new TopTenTracksAdapter(
                getActivity(),
                R.layout.list_item_track,
                R.id.txtTrackName,
                mSpotifyTracks);

        View rootView = inflater.inflate(R.layout.fragment_top_ten_tracks, container, false);

        ListView listView = (ListView)rootView.findViewById(R.id.lvTracks);
        listView.setAdapter(mTracksAdapter);

        String extra = Intent.EXTRA_TEXT;

        String artistID = "";
        Intent intent = getActivity().getIntent();
        if ((intent != null) && intent.hasExtra(extra)) {
            artistID = intent.getStringExtra(extra);
        }

        fetchTopTenTracks(artistID);

        return rootView;
    }

    private void fetchTopTenTracks(String searchArgument) {
        if (!searchArgument.equals(this.mSearchArgument)) {
            this.mSearchArgument = searchArgument;

            FetchTopTenTracksTask task = new FetchTopTenTracksTask();
            task.execute(searchArgument);
        }
    }

    public class FetchTopTenTracksTask extends AsyncTask<String, Void, ArrayList<Track>> {
        @Override
        protected ArrayList<Track> doInBackground(String... params) {
            mSpotifyTracks = new ArrayList<Track>();

            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("country", "US");

            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();
                Tracks tracks = spotify.getArtistTopTrack(params[0], queryMap);

                for (Track track : tracks.tracks) {
                    mSpotifyTracks.add(track);
                }
            }
            catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                mSpotifyTracks = null;
            }

            return mSpotifyTracks;
        }

        @Override
        protected void onPostExecute(ArrayList<Track> tracks) {
            if (tracks == null) {
                Toast toast = Toast.makeText(getActivity(), getString(R.string.error_search_top_tracks), Toast.LENGTH_SHORT);
                toast.show();
            } else {
                mTracksAdapter.clear();

                for (Track track : tracks) {
                    mTracksAdapter.add(track);
                }

                if (mTracksAdapter.isEmpty()) {
                    Toast toast = Toast.makeText(getActivity(), getString(R.string.warn_empty_search_top_tracks), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }
    }
}
