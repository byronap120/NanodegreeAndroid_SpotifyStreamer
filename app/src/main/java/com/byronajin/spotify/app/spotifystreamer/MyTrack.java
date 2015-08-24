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
    String preview_url;
    String imageHQ;

    public MyTrack(Track track){
        this.preview_url = track.preview_url;
        this.name = track.name;
        this.album = track.album.name;
        if(track.album.images.size()>=3){
            this.imageHQ = track.album.images.get(0).url;
            this.urImage = track.album.images.get(2).url;
        }

    }

    private MyTrack(Parcel in){
        name = in.readString();
        album = in.readString();
        urImage= in.readString();
        preview_url = in.readString();
        imageHQ = in.readString();
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
        dest.writeString(preview_url);
        dest.writeString(imageHQ);
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
