package net.catsonmars.android.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by pmatushkin on 6/30/2015.
 * Custom adapter code is adapted from http://www.softwarepassion.com/android-series-custom-listview-items-and-adapters/
 * Spotify icon is extracted from the Spotify Design Resources package at https://developer.spotify.com/design/
 */
public class ArtistsAdapter extends ArrayAdapter<Artist> {

    private ArrayList<Artist> items;
    private Context context;

    public ArtistsAdapter(Context context, int resource, int textViewResourceId, ArrayList<Artist> objects) {
        super(context, resource, textViewResourceId, objects);

        this.context = context;
        this.items = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_item_artist, null);
        }

        Artist o = items.get(position);
        if (o != null) {
            TextView tt = (TextView) v.findViewById(R.id.txtArtistName);
            if (tt == null) {
            } else {
                tt.setText(o.name);
            }

            ImageView img = (ImageView) v.findViewById(R.id.imgArtistIcon);
            if (img == null) {
            } else {
                if (o.images == null || o.images.isEmpty()) {
                    Picasso.with(context)
                            .load(R.drawable.img_spotify_default)
                            .resize(200, 200)
                            //.centerCrop()
                            .into(img);
                } else {
                    Picasso.with(context)
                            .load(o.images.get(0).url)
                            .placeholder(R.drawable.img_spotify_default)
                            .error(R.drawable.img_spotify_default)
                            .resize(200, 200)
                            //.centerCrop()
                            .into(img);
                }
            }
        }
        return v;
    }
}
