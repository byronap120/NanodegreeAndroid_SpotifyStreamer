package com.byronajin.spotify.app.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.List;

/**
 * Created by Byron on 7/13/2015.
 */
public class TopTrackAdapter<Track> extends ArrayAdapter<Track> {

    Context context;

    public TopTrackAdapter(Context context, List<Track> artistList) {
        super(context, 0, artistList);
        this.context = context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Gets the artist object
        com.byronajin.spotify.app.spotifystreamer.MyTrack track = (com.byronajin.spotify.app.spotifystreamer.MyTrack) getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_top_tracks, parent, false);
        }

        ImageView imageThumbnailArtist = (ImageView) convertView.findViewById(R.id.imageThumbnailTrack);
        String images = track.urImage;

        if(images !=null){
            Picasso.with(this.context)
                    .load(images)
                    .into(imageThumbnailArtist);
        }else{
            Picasso.with(context).
                    load(R.drawable.artist_not_pic).
                    into(imageThumbnailArtist);
        }


        TextView textViewNameArtist = (TextView) convertView.findViewById(R.id.textViewNameTrack);
        textViewNameArtist.setText(track.name);

        TextView textViewNameAlbum = (TextView) convertView.findViewById(R.id.textViewNameAlbum);
        textViewNameAlbum.setText(track.album);

        return convertView;
    }
}
