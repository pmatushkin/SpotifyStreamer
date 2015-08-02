package net.catsonmars.android.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * The code for retaining the fragment state is adapted from http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html
 */
public class ArtistsFragment extends Fragment {
    String LOG_TAG = "SPOTIFY_STREAMER";

    private ArtistsAdapter mArtistsAdapter;
    private String mSearchArgument;

    // The list to retain the previous search results across configuration changes
    private ArrayList<Artist> mSpotifyArtists;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mSpotifyArtists == null)
            mSpotifyArtists = new ArrayList<Artist>();

        mArtistsAdapter = new ArtistsAdapter(
                getActivity(),
                R.layout.list_item_artist);

        View rootView = inflater.inflate(R.layout.fragment_artists, container, false);

        ListView listView = (ListView)rootView.findViewById(R.id.lvArtists);
        listView.setAdapter(mArtistsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = (Artist) parent.getAdapter().getItem(position);

                Intent detailIntent = new Intent(getActivity(), TopTenTracksActivity.class);
                detailIntent.putExtra(Intent.EXTRA_TEXT, artist.id);
                detailIntent.putExtra(Intent.EXTRA_TITLE, artist.name);
                startActivity(detailIntent);
            }
        });

        /* TODO: redo this for SearchView and a higher API level (see http://pastebin.com/BRGEujnU for details)
         */
        EditText edtArtist = (EditText)rootView.findViewById(R.id.edtArtist);
        edtArtist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {  }

            @Override
            public void afterTextChanged(Editable s) { updateArtists(s.toString()); }
        });

        return rootView;
    }

    /*
    * Compare the saved search argument and the current search argument;
    * Do the search only if the arguments don't match
    *
    * The deletion of the last character in the search box doesn't trigger the search because of the second 'if'.
    * I didn't intend to implement this behaviour but I like it better than blanking the list view
    * when the user deletes the last character in the search box.
    */
    private void updateArtists(String searchArgument) {
        if (searchArgument.equals(this.mSearchArgument)) {
            mArtistsAdapter.clear();
            mArtistsAdapter.addAll(mSpotifyArtists);
        } else {
            this.mSearchArgument = searchArgument;

            if (!searchArgument.isEmpty()) {
                FetchArtistsTask task = new FetchArtistsTask();
                task.execute(searchArgument);
            }
        }
    }

    public class FetchArtistsTask extends AsyncTask<String, Void, ArrayList<Artist>> {
        @Override
        protected ArrayList<Artist> doInBackground(String... params) {
            ArrayList<Artist> spotifyArtists = new ArrayList<Artist>();

            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();

                Log.d(LOG_TAG, "searching for an artist on Spotify");
                ArtistsPager artists = spotify.searchArtists(params[0]);

                spotifyArtists.addAll(artists.artists.items);
            }
            catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                spotifyArtists = null;
            }

            return spotifyArtists;
        }

        @Override
        protected void onPostExecute(ArrayList<Artist> artists) {
            if (artists == null) {
                Toast toast = Toast.makeText(getActivity(), getString(R.string.error_search_artists), Toast.LENGTH_SHORT);
                toast.show();
            } else {
                mSpotifyArtists.clear();
                mSpotifyArtists.addAll(artists);

                mArtistsAdapter.clear();
                mArtistsAdapter.addAll(artists);

                if (mArtistsAdapter.isEmpty()) {
                    Toast toast = Toast.makeText(getActivity(), getString(R.string.warn_empty_search_artists), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }
    }
}
