package com.byronajin.spotify.app.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksFragment extends Fragment {

    private static final String LOG_TAG = TopTracksFragment.class.getSimpleName();
    SpotifyApi api;
    TopTrackAdapter<MyTrack> tracksAdapter;
    ArrayList<MyTrack> trackList;
    ListView list;
    String actualQuery;

    public TopTracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = new SpotifyApi();
        actualQuery="";

        if(savedInstanceState == null || !savedInstanceState.containsKey("trackList")) {
            trackList = new ArrayList<MyTrack>();
        }
        else {
            trackList = savedInstanceState.getParcelableArrayList("trackList");
            actualQuery = savedInstanceState.getString("actualQuery");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        list = (ListView) view.findViewById(R.id.listViewTopTracks);
        tracksAdapter = new TopTrackAdapter<MyTrack>(
                getActivity(),
                trackList);
        list.setAdapter(tracksAdapter);


        Intent intent = getActivity().getIntent();
        if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
            String idArtist = intent.getStringExtra(Intent.EXTRA_TEXT);
            new SearchSpotifyTask().execute(idArtist);
        }
        return view;
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("trackList", trackList);
        outState.putString("actualQuery", actualQuery);
        super.onSaveInstanceState(outState);
    }


    public class SearchSpotifyTask extends AsyncTask<String, Void, ArrayList<MyTrack>>
    {
        @Override
        protected ArrayList<MyTrack> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            //Get the information from API
            SpotifyService service = api.getService();
            Map<String, Object> options = new HashMap<>();
            options.put(SpotifyService.COUNTRY, Locale.getDefault().getCountry());
            Tracks result1 = service.getArtistTopTrack(params[0], options);
            List<Track> tracksArtist= result1.tracks;
            //Create a new List of "MyTrack" that implement parcelable
            ArrayList<MyTrack> myArtistTracks = new ArrayList<MyTrack>();
            for (int i = 0; i < tracksArtist.size(); i++) {
                myArtistTracks.add(new MyTrack(tracksArtist.get(i)));
            }
            return myArtistTracks;
        }

        @Override
        protected void onPostExecute(ArrayList<MyTrack> tracksListFromAPI) {
            if(tracksListFromAPI != null){
                tracksAdapter.clear();
                for(MyTrack tracks : tracksListFromAPI) {
                    tracksAdapter.add(tracks);
                }
            }
        }
    }
}
