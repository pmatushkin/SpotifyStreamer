package net.catsonmars.android.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * A placeholder fragment containing a simple view.
 * Comparator code is adapted from http://stackoverflow.com/questions/10853205/android-sort-arraylist-by-properties
 */
public class ArtistsFragment extends Fragment {

    ArtistsAdapter mArtistsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mArtistsAdapter = new ArtistsAdapter(
                getActivity(),
                R.layout.list_item_artist,
                R.id.txtArtist,
                new ArrayList<SpotifyArtist>());

        View rootView = inflater.inflate(R.layout.fragment_artists, container, false);

        ListView listView = (ListView)rootView.findViewById(R.id.lvArtists);
        listView.setAdapter(mArtistsAdapter);

        EditText edtArtist = (EditText)rootView.findViewById(R.id.edtArtist);
        edtArtist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateArtists(s.toString());
            }
        });

        return rootView;
    }

    private void updateArtists(String searchArgument) {
        if (searchArgument.isEmpty()) {
            /* Removed this toast, because it pops up too often, cluttering the screen */
            /*
            Toast toast = Toast.makeText(getActivity(), getString(R.string.warn_empty_search_term), Toast.LENGTH_SHORT);
            toast.show();
            */
        } else {
            FetchArtistsTask task = new FetchArtistsTask();
            task.execute(searchArgument);
        }
    }

    public class FetchArtistsTask extends AsyncTask<String, Void, ArrayList<SpotifyArtist>> {
        @Override
        protected ArrayList<SpotifyArtist> doInBackground(String... params) {
            ArrayList<SpotifyArtist> spotifyArtists = new ArrayList<SpotifyArtist>();

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager artists = spotify.searchArtists(params[0]);

            for(Artist artist : artists.artists.items) {
                SpotifyArtist spotifyArtist = getSpotifyArtist(artist);

                spotifyArtists.add(spotifyArtist);
            }

            /* Removed the custom sorting; looks like Spotify sorts the results internally */
            /* Collections.sort(spotifyArtists, new SpotifyArtistComparator()); */

            return spotifyArtists;
        }

        @Override
        protected void onPostExecute(ArrayList<SpotifyArtist> artists) {
            mArtistsAdapter.clear();

            for(SpotifyArtist spotifyArtist : artists) {
                mArtistsAdapter.add(spotifyArtist);
            }

            if (mArtistsAdapter.isEmpty()) {
                Toast toast = Toast.makeText(getActivity(), getString(R.string.warn_empty_search_results), Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        private SpotifyArtist getSpotifyArtist(Artist artist) {
            SpotifyArtist spotifyArtist = new SpotifyArtist();
            spotifyArtist.ID = artist.id;
            spotifyArtist.Name = artist.name;
            if (!artist.images.isEmpty()) {
                spotifyArtist.Image = artist.images.get(0);
            }

            return spotifyArtist;
        }
    }

    /* Removed the custom sorting; looks like Spotify sorts the results internally */
    /*
    public class SpotifyArtistComparator implements Comparator<SpotifyArtist>
    {
        public int compare(SpotifyArtist left, SpotifyArtist right) {
            return left.Name.compareTo(right.Name);
        }
    }
    */
}
