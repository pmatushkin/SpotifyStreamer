package net.catsonmars.android.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Custom adapter code is adapted from http://www.softwarepassion.com/android-series-custom-listview-items-and-adapters/
 * Spotify icon is cropped from the Spotify Design Resources package at https://developer.spotify.com/design/
 */
public class ArtistsAdapter extends ArrayAdapter<Artist> {

    private Context context;

    public ArtistsAdapter(Context context, int resource) {
        super(context, resource);

        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_item_artist, null);
        }

        Artist o = getItem(position);
        if (o != null) {
            TextView tt;
            ImageView img;

            tt = (TextView) v.findViewById(R.id.txtArtistName);
            if (tt == null) {
            } else {
                tt.setText(o.name);
            }

            img = (ImageView) v.findViewById(R.id.imgArtistIcon);
            if (img == null) {
            } else {
                if (o.images == null || o.images.isEmpty()) {
                    Picasso.with(context)
                            .load(R.drawable.img_spotify_default)
                            .resize(200, 200)
                            .into(img);
                } else {
                    // try to get the medium size image for the list view
                    // it's usually the second image on the result list
                    Image image;
                    if (o.images.size() >= 2)
                        image = o.images.get(1);
                    else
                        image = o.images.get(0);

                    Picasso.with(context)
                            .load(image.url)
                            .placeholder(R.drawable.img_spotify_default)
                            .error(R.drawable.img_spotify_default)
                            .resize(200, 200)
                            .into(img);
                }
            }
        }
        return v;
    }
}
