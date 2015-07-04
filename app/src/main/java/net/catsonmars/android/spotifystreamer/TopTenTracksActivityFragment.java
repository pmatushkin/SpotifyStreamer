package net.catsonmars.android.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTenTracksActivityFragment extends Fragment {

    String artistID;

    public TopTenTracksActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_ten_tracks, container, false);

        String extra = Intent.EXTRA_TEXT;

        Intent intent = getActivity().getIntent();
        if ((intent != null) && intent.hasExtra(extra)) {
            artistID = intent.getStringExtra(extra);

            ((TextView)rootView.findViewById(R.id.txtArtistID)).setText(artistID);
        }

        return rootView;
    }
}
