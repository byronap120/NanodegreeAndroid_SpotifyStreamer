package com.byronajin.spotify.app.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Byron on 7/13/2015.
 */
public class MyTrack implements Parcelable {

    String name;
    String album;
    String urImage;

    public MyTrack(Track track){
        this.name = track.name;
        this.album = track.album.name;
        if(track.album.images.size()>=3)
            this.urImage = track.album.images.get(2).url;
    }

    private MyTrack(Parcel in){
        name = in.readString();
        album = in.readString();
        urImage= in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(album);
        dest.writeString(urImage);
    }

    public final Parcelable.Creator<MyTrack> CREATOR = new Parcelable.Creator<MyTrack>() {
        @Override
        public MyTrack createFromParcel(Parcel parcel) {
            return new MyTrack(parcel);
        }

        @Override
        public MyTrack[] newArray(int i) {
            return new MyTrack[i];
        }

    };
}
