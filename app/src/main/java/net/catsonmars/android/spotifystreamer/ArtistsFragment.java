package net.catsonmars.android.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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

    public ArtistsFragment() {
    }

    @Override
    public void onStart(){
        super.onStart();

        updateArtists("motorhead");
    }

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

        return rootView;
    }

    private void updateArtists(String searchArgument) {
        /* TODO: handle the case when searchArgument is empty (Spotify throws a 400 Bad Request); display Toast or something */

        FetchArtistsTask task = new FetchArtistsTask();
        task.execute(searchArgument);
    }

    public class FetchArtistsTask extends AsyncTask<String, Void, ArrayList<SpotifyArtist>> {
        @Override
        protected ArrayList<SpotifyArtist> doInBackground(String... params) {
            SpotifyApi api = new SpotifyApi();

            SpotifyService spotify = api.getService();

            /* TODO: handle the case when the use enters the exact name of the artist, and we would want to place the found artist on the top of the list */

            ArtistsPager artists = spotify.searchArtists(params[0]);

            ArrayList<SpotifyArtist> spotifyArtists = new ArrayList<SpotifyArtist>();

            for(Artist artist : artists.artists.items) {

                SpotifyArtist spotifyArtist = getSpotifyArtist(artist);

                spotifyArtists.add(spotifyArtist);
            }

            Collections.sort(spotifyArtists, new SpotifyArtistComparator());

            return spotifyArtists;
        }

        @Override
        protected void onPostExecute(ArrayList<SpotifyArtist> artists) {
            mArtistsAdapter.clear();

            for(SpotifyArtist spotifyArtist : artists) {
                mArtistsAdapter.add(spotifyArtist);
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

    public class SpotifyArtistComparator implements Comparator<SpotifyArtist>
    {
        public int compare(SpotifyArtist left, SpotifyArtist right) {
            return left.Name.compareTo(right.Name);
        }
    }
}
