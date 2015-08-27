package com.byronajin.spotify.app.spotifystreamer;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.app.Service;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Byron on 8/25/2015.
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {


    private MediaPlayer player;
    private final IBinder musicBind = new MusicBinder();
    private boolean playing;

    public void onCreate(){
        super.onCreate();
        playing =false;
        player = new MediaPlayer();
        //initialize
        initMusicPlayer();
    }

    public void initMusicPlayer(){
        //set player properties
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //set listeners
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }


    public void playSong(String url){

        player.reset();
        playing=false;

        try{
            player.setDataSource(url);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public void pausePlayer(){
        player.pause();
    }

    public void resumePlayer(){
        player.start();
    }

    public void seekToMediaPlayer(int value){
        player.seekTo(value * 1000);
    }

    public int currentPositionPlayer(){
        return player.getCurrentPosition();
    }

    public boolean isPlaying(){
        return playing;
    }

    public void setPlaying(boolean value){
        playing = value;
    }

    //release resources when unbind
    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        playing = false;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }
}
