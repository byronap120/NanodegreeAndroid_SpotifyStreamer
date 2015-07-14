package com.byronajin.spotify.app.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.List;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by Byron on 7/12/2015.
 */
public class MyArtist implements Parcelable {


    public String name;
    public String id;
    public String urImage;
    public String genres;

    public MyArtist(Artist artist){
        this.name = artist.name;
        this.id = artist.id;
        if(artist.images.size()>=3)
            this.urImage = artist.images.get(2).url;
        if(artist.genres != null ) {
            this.genres = artist.genres.toString();
            if (artist.genres.size() > 5)
                this.genres = artist.genres.subList(0, 5).toString();
        }

    }

    private MyArtist(Parcel in){
        name = in.readString();
        id = in.readString();
        urImage= in.readString();
        genres = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(id);
        dest.writeString(urImage);
        dest.writeString(genres);
    }

    public final Parcelable.Creator<MyArtist> CREATOR = new Parcelable.Creator<MyArtist>() {
        @Override
        public MyArtist createFromParcel(Parcel parcel) {
            return new MyArtist(parcel);
        }

        @Override
        public MyArtist[] newArray(int i) {
            return new MyArtist[i];
        }

    };
}
