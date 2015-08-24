package com.byronajin.spotify.app.spotifystreamer;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

/**
 * A placeholder fragment containing a simple view.
 */
public class TrackPlayerFragment extends Fragment {
    String urlImageArtist;
    SpotifyApi api;
    String idArtist;
    String trackName;
    String artistName;
    Integer indexTrack;
    ArrayList<MyTrack> listTracks;
    MediaPlayer mediaPlayer;
    SeekBar trackSeekBar;
    private Handler mHandler;
    boolean playing;
    ImageView albumImageView;
    TextView textViewTrack,textViewArtist,textViewAlbum;
    ImageButton imageButtonPrevious, imageButtonPause , imageButtonNext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = new SpotifyApi();
        urlImageArtist="";
        indexTrack=0;
        listTracks = new ArrayList<MyTrack>();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mHandler = new Handler();
        playing = false;
    }

    public TrackPlayerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_player, container, false);
        albumImageView = (ImageView) view.findViewById(R.id.imageViewAlbum);
        trackSeekBar = (SeekBar) view.findViewById(R.id.seekBarTrack);
        textViewTrack = (TextView) view.findViewById(R.id.textViewTrack);
        textViewArtist = (TextView) view.findViewById(R.id.textViewArtist);
        textViewAlbum = (TextView) view.findViewById(R.id.textViewAlbum);
        imageButtonPrevious = (ImageButton) view.findViewById(R.id.imageButtonPrevious);
        imageButtonPrevious.setOnClickListener(onClickPlayerButton);
        imageButtonPause = (ImageButton) view.findViewById(R.id.imageButtonPause);
        imageButtonPause.setOnClickListener(onClickPlayerButton);
        imageButtonNext = (ImageButton) view.findViewById(R.id.imageButtonNext);
        imageButtonNext.setOnClickListener(onClickPlayerButton);
        trackSeekBar.setMax(30);

        Intent intent = getActivity().getIntent();
        if(intent != null){
            idArtist = intent.getStringExtra("idArtist");
            trackName = intent.getStringExtra("tackName");
            artistName = intent.getStringExtra("artistName");
            new SearchSpotifyTask().execute(idArtist);
        }

        //Listeners for player
        trackSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
               Log.d("TERMINO" , "FINISHHHHHH");
                playing = false;
            }
        });

        return view;
    }

    View.OnClickListener onClickPlayerButton = new View.OnClickListener() {
        public void onClick(View view) {
            switch (view.getId()) {
            case R.id.imageButtonPrevious:
               if(indexTrack>0){
                   playing=false;
                   mediaPlayer.stop();
                   mediaPlayer.release();
                   mediaPlayer = new MediaPlayer();
                   mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                   indexTrack--;
                   PlayTrack(indexTrack);
               }
                break;
            case R.id.imageButtonPause:
                if(playing){
                    mediaPlayer.pause();
                    playing=false;
                    imageButtonPause.setImageResource(R.drawable.play_circle_outline);
                }else{
                    mediaPlayer.start();
                    playing=true;
                    new listenerActualTrack().execute();
                    imageButtonPause.setImageResource(R.drawable.pause_circle_outline);
                }

                break;
            case R.id.imageButtonNext:
                if(indexTrack<listTracks.size()-1){
                    playing=false;
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    indexTrack++;
                    PlayTrack(indexTrack);
                }
                break;
            }
        }
    };


    @Override
    public void onStop() {
        super.onStop();
        playing=false;
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    public class SearchSpotifyTask extends AsyncTask<String, Void, ArrayList<MyTrack>>
    {
        @Override
        protected ArrayList<MyTrack> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }
            //Create a new List of "MyTrack" that implement parcelable
            ArrayList<MyTrack> myArtistTracks = new ArrayList<MyTrack>();

            try{
                //Get the information from API
                SpotifyService service = api.getService();
                Map<String, Object> options = new HashMap<>();
                options.put(SpotifyService.COUNTRY, Locale.getDefault().getCountry());
                Tracks result1 = service.getArtistTopTrack(params[0], options);
                List<Track> tracksArtist= result1.tracks;

                for (int i = 0; i < tracksArtist.size(); i++) {
                    myArtistTracks.add(new MyTrack(tracksArtist.get(i)));
                }
            }catch (RetrofitError error){
                return  null;
            }
            return myArtistTracks;
        }

        @Override
        protected void onPostExecute(ArrayList<MyTrack> tracksListFromAPI) {
            if(tracksListFromAPI!=null){
                listTracks = tracksListFromAPI;
                PlayTrack(-1);
            }

        }
    }

    public void PlayTrack(int indexActualTrack){
        String trackUrl = "";
        String imageUrl = "";
        if(indexActualTrack >=0){
            trackUrl = listTracks.get(indexActualTrack).preview_url;
        }else {
            for(int i=0 ; i< listTracks.size() ; i++){
                if(listTracks.get(i).name.equals(trackName)){
                    indexTrack = i;
                    indexActualTrack = i;
                    trackUrl = listTracks.get(i).preview_url;
                }
            }
        }

        loadImageTrack(listTracks.get(indexActualTrack).imageHQ);
        setTextviews(listTracks.get(indexActualTrack).name,artistName ,listTracks.get(indexActualTrack).album);
        playPreviewTrack(trackUrl);
    }

    public void setTextviews(String track , String artist , String album){
        textViewTrack.setText(track);
        textViewArtist.setText(artist);
        textViewAlbum.setText(album);
    }
    public void loadImageTrack(String urlImage){
        if(urlImage !=null){
            Picasso.with(getActivity())
                    .load(urlImage)
                    .into(albumImageView);
        }else{
            Picasso.with(getActivity()).
                    load(R.drawable.artist_not_pic).
                    into(albumImageView);
        }
    }
    public void playPreviewTrack (String trackUrl){
        try {
            mediaPlayer.setDataSource(trackUrl);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    playing = true;
                    new listenerActualTrack().execute();
                }
            });
            mediaPlayer.prepareAsync(); // prepare async to not block main thread
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public class listenerActualTrack extends AsyncTask<String, Void, ArrayList<MyTrack>>
    {

        @Override
        protected ArrayList<MyTrack> doInBackground(String... strings) {
            while(playing){
                try {
                    publishProgress();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            trackSeekBar.setProgress(mediaPlayer.getCurrentPosition() / 1000);
        }
    }


}
