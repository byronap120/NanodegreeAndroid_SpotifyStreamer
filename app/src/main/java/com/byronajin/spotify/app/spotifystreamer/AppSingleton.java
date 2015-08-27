package com.byronajin.spotify.app.spotifystreamer;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.byronajin.spotify.app.spotifystreamer.MusicService.MusicBinder;

/**
 * Created by Byron on 8/26/2015.
 */
public class AppSingleton extends Application {


    //service
    private MusicService musicSrv;
    private Intent playIntent;
    //binding
    private boolean musicBound=false;

    public AppSingleton(){

    }


    public void onCreate() {
        super.onCreate();

        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, AppSingleton.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }


    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    public MusicService getMusicSrv(){
        return musicSrv;
    }



}
