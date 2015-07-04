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

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by pmatushkin on 7/4/2015.
 */
public class TopTenTracksAdapter extends ArrayAdapter<Track> {

    private ArrayList<Track> items;
    private Context context;

    public TopTenTracksAdapter(Context context, int resource, int textViewResourceId, ArrayList<Track> objects) {
        super(context, resource, textViewResourceId, objects);

        this.context = context;
        this.items = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_item_track, null);
        }

        Track o = items.get(position);
        if (o != null) {
            TextView tt = (TextView) v.findViewById(R.id.txtTrackName);
            if (tt == null) {
            } else {
                tt.setText(o.name);
            }

            ImageView img = (ImageView) v.findViewById(R.id.imgTrackIcon);
            if (img == null) {
            } else {
                if (o.album.images == null || o.album.images.isEmpty()) {
                    Picasso.with(context)
                            .load(R.drawable.img_spotify_default)
                            .resize(200, 200)
                                    //.centerCrop()
                            .into(img);
                } else {
                    Picasso.with(context)
                            .load(o.album.images.get(0).url)
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
