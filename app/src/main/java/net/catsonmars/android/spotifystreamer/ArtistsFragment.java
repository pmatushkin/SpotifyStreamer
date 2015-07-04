package net.catsonmars.android.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
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
 * A placeholder fragment containing a simple view.
 */
public class ArtistsFragment extends Fragment {

    private ArtistsAdapter mArtistsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mArtistsAdapter = new ArtistsAdapter(
                getActivity(),
                R.layout.list_item_artist,
                R.id.txtArtistName,
                new ArrayList<Artist>());

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

        EditText edtArtist = (EditText)rootView.findViewById(R.id.edtArtist);
        edtArtist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {  }

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

    public class FetchArtistsTask extends AsyncTask<String, Void, ArrayList<Artist>> {
        @Override
        protected ArrayList<Artist> doInBackground(String... params) {
            ArrayList<Artist> spotifyArtists = new ArrayList<Artist>();

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager artists = spotify.searchArtists(params[0]);

            for(Artist artist : artists.artists.items) {
                spotifyArtists.add(artist);
            }

            return spotifyArtists;
        }

        @Override
        protected void onPostExecute(ArrayList<Artist> artists) {
            mArtistsAdapter.clear();

            for(Artist spotifyArtist : artists) {
                mArtistsAdapter.add(spotifyArtist);
            }

            if (mArtistsAdapter.isEmpty()) {
                Toast toast = Toast.makeText(getActivity(), getString(R.string.warn_empty_search_artists), Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}
