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
import kaaes.spotify.webapi.android.models.Image;


/**
 * Created by Byron on 7/11/2015.
 */
public class ArtistListArrayAdapter<MyArtist> extends ArrayAdapter<MyArtist> {

    Context context;

     public ArtistListArrayAdapter(Context context, List<MyArtist> artistList) {
         super(context, 0, artistList);
         this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Gets the artist object
        com.byronajin.spotify.app.spotifystreamer.MyArtist artist = (com.byronajin.spotify.app.spotifystreamer.MyArtist) getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);
        }

        ImageView imageThumbnailArtist = (ImageView) convertView.findViewById(R.id.imageThumbnailArtist);
        String images = artist.urImage;

        if(images !=null){
            Picasso.with(this.context)
                    .load(images)
                    .into(imageThumbnailArtist);
        }else{
            Picasso.with(context).
                    load(R.drawable.artist_not_pic).
                    into(imageThumbnailArtist);
        }


        TextView textViewNameArtist = (TextView) convertView.findViewById(R.id.textViewNameArtist);
        textViewNameArtist.setText(artist.name);

        TextView textViewGenres = (TextView) convertView.findViewById(R.id.textViewGenres);
        textViewGenres.setText(artist.genres.substring(1,artist.genres.length()-1));

        return convertView;
    }

}
