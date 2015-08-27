package com.byronajin.spotify.app.spotifystreamer;


import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;

import android.os.Bundle;
import android.os.IBinder;
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
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

import com.byronajin.spotify.app.spotifystreamer.MusicService.MusicBinder;

/**
 * A placeholder fragment containing a simple view.
 */
public class TrackPlayerFragment  extends DialogFragment {
    String urlImageArtist;
    SpotifyApi api;
    String idArtist;
    String trackName;
    String artistName;
    Integer indexTrack;
    ArrayList<MyTrack> listTracks;
   // MediaPlayer mediaPlayer;
    SeekBar trackSeekBar;
    boolean playing,rotate;
    ImageView albumImageView;
    TextView textViewTrack,textViewArtist,textViewAlbum,textViewTime;
    ImageButton imageButtonPrevious, imageButtonPause , imageButtonNext;
   //Music Service
    private AppSingleton appSingleton;
    private MusicService musicSrv;



    static TrackPlayerFragment newInstance(String idArtist, String trackName, String artistName) {

        TrackPlayerFragment f = new TrackPlayerFragment();

        Bundle args = new Bundle();
        args.putString("idArtist", idArtist);
        args.putString("trackName", trackName);
        args.putString("artistName", artistName);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = new SpotifyApi();
        urlImageArtist="";
        indexTrack=0;
        listTracks = new ArrayList<MyTrack>();
        playing = false;
        rotate=false;
        appSingleton = ((AppSingleton)getActivity().getApplicationContext());
        musicSrv = appSingleton.getMusicSrv();
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
        textViewTime = (TextView) view.findViewById(R.id.textViewTime);
        imageButtonPrevious = (ImageButton) view.findViewById(R.id.imageButtonPrevious);
        imageButtonPrevious.setOnClickListener(onClickPlayerButton);
        imageButtonPause = (ImageButton) view.findViewById(R.id.imageButtonPause);
        imageButtonPause.setOnClickListener(onClickPlayerButton);
        imageButtonNext = (ImageButton) view.findViewById(R.id.imageButtonNext);
        imageButtonNext.setOnClickListener(onClickPlayerButton);
        trackSeekBar.setMax(30);


        Intent intent = getActivity().getIntent();
        if(savedInstanceState!=null) {
            listTracks =  savedInstanceState.getParcelableArrayList("trackList");
            artistName =  savedInstanceState.getString("artistName");
            trackName =  savedInstanceState.getString("trackName");
            idArtist = savedInstanceState.getString("idArtist");
            indexTrack = savedInstanceState.getInt("indexTrack");
            rotate=true;
            PlayTrack(indexTrack);
        }else{
            if(getArguments()== null){
                idArtist = intent.getStringExtra("idArtist");
                trackName = intent.getStringExtra("tackName");
                artistName = intent.getStringExtra("artistName");
            }else {
                idArtist = getArguments().getString("idArtist");
                trackName = getArguments().getString("trackName");
                artistName = getArguments().getString("artistName");
            }
            new SearchSpotifyTask().execute(idArtist);
        }


        //Listeners for player
        trackSeekBar.setOnSeekBarChangeListener(seekBarListener);



        return view;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("trackList", listTracks);
        outState.putString("artistName", artistName);
        outState.putString("trackName", trackName);
        outState.putString("idArtist", idArtist);
        outState.putInt("indexTrack", indexTrack);
        super.onSaveInstanceState(outState);
    }

    SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                musicSrv.seekToMediaPlayer(progress);
                setTextSeekBar(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    View.OnClickListener onClickPlayerButton = new View.OnClickListener() {
        public void onClick(View view) {
            switch (view.getId()) {
            case R.id.imageButtonPrevious:
               if(indexTrack>0){
                   playing = false;
                   indexTrack--;
                   PlayTrack(indexTrack);
               }
                break;
            case R.id.imageButtonPause:
                if(musicSrv.isPlaying()){
                    musicSrv.pausePlayer();
                    musicSrv.setPlaying(false);
                    playing = false;
                    imageButtonPause.setImageResource(R.drawable.play_circle_outline);
                }else{
                    musicSrv.resumePlayer();
                    musicSrv.setPlaying(true);
                    new listenerActualTrack().execute();
                    imageButtonPause.setImageResource(R.drawable.pause_circle_outline);
                }

                break;
            case R.id.imageButtonNext:
                if(indexTrack<listTracks.size()-1){
                    playing = false;
                    indexTrack++;
                    PlayTrack(indexTrack);
                }
                break;
            }
        }
    };



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
        if(indexActualTrack >=0){
            trackUrl = listTracks.get(indexActualTrack).preview_url;
        }else {
            for(int i=0 ; i < listTracks.size(); i++) {
                if(listTracks.get(i).name.equals(trackName)) {
                    indexTrack = i;
                    indexActualTrack = i;
                    trackUrl = listTracks.get(i).preview_url;
                }
            }
        }
        trackName = listTracks.get(indexActualTrack).name;
        loadImageTrack(listTracks.get(indexActualTrack).imageHQ);
        setTextviews(trackName, artistName, listTracks.get(indexActualTrack).album);
        if(!rotate){
            musicSrv.playSong(trackUrl);
            musicSrv.setPlaying(true);
        }
        new listenerActualTrack().execute();
        rotate=false;
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

/*    public void playPreviewTrack (String trackUrl){
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

    }*/

    @Override
    public void onStop() {
        super.onStop();
        playing = false;
        trackSeekBar.setOnSeekBarChangeListener(null);

    }

    public class listenerActualTrack extends AsyncTask<String, Void, ArrayList<MyTrack>>
    {

        @Override
        protected ArrayList<MyTrack> doInBackground(String... strings) {

            Log.d("value2", musicSrv.isPlaying() + "");
            playing = musicSrv.isPlaying();
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
            int time = musicSrv.currentPositionPlayer() / 1000;
            trackSeekBar.setProgress(time);
            setTextSeekBar(time);

        }
    }

    public void setTextSeekBar(int time){
        String timeString = time + "";
        if(time<10){
            timeString = "0" + timeString;
        }
        textViewTime.setText("0:" + timeString);
    }



}
