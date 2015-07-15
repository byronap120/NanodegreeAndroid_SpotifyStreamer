package com.byronajin.spotify.app.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

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
public class TopTracksFragment extends Fragment {

    private static final String LOG_TAG = TopTracksFragment.class.getSimpleName();
    SpotifyApi api;
    TopTrackAdapter<MyTrack> tracksAdapter;
    ArrayList<MyTrack> trackList;
    ListView list;
    String actualQuery;
    String urlImageArtist;
    ImageView artistImageView;
    CollapsingToolbarLayout collapsingToolbar;
    String artistName;

    public TopTracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = new SpotifyApi();
        actualQuery="";
        urlImageArtist="";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        list = (ListView) view.findViewById(R.id.listViewTopTracks);
        artistImageView = (ImageView) view.findViewById(R.id.ArtistImageView);
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        collapsingToolbar = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        //Replace the action bar with the toolbar
        ((TopTracks)getActivity()).setSupportActionBar(toolbar);
        ((TopTracks)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Restore the values of the list of tracks and Artist image url
        if(savedInstanceState == null || !savedInstanceState.containsKey("trackList")) {
            trackList = new ArrayList<MyTrack>();
            Intent intent = getActivity().getIntent();
            if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
                String idArtist = intent.getStringExtra(Intent.EXTRA_TEXT);
                artistName = intent.getStringExtra("artistName");
                new SearchSpotifyTask().execute(idArtist,artistName);
            }
        }
        else {
            trackList = savedInstanceState.getParcelableArrayList("trackList");
            actualQuery = savedInstanceState.getString("actualQuery");
            artistName =  savedInstanceState.getString("artistName");
            urlImageArtist =  savedInstanceState.getString("urlImageArtist");

            if(urlImageArtist != null && !urlImageArtist.equals(""))
                Picasso.with(getActivity())
                        .load(urlImageArtist)
                        .into(artistImageView);
        }

        collapsingToolbar.setTitle(artistName);
        tracksAdapter = new TopTrackAdapter<MyTrack>(
                getActivity(),
                trackList);
        list.setAdapter(tracksAdapter);


        return view;
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("trackList", trackList);
        outState.putString("actualQuery", actualQuery);
        outState.putString("artistName", artistName);
        outState.putString("urlImageArtist", urlImageArtist);
        super.onSaveInstanceState(outState);
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

                //read the artist to get the image
                ArtistsPager results = service.searchArtists(params[1]);
                List<Artist> artists = results.artists.items;
                List<Image> imagesArtist =  artists.get(0).images;
                if(imagesArtist.size()>0){
                    urlImageArtist=imagesArtist.get(0).url;
                }

            }catch (RetrofitError error){
                return  null;
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

                if(urlImageArtist != null && !urlImageArtist.equals(""))
                Picasso.with(getActivity())
                        .load(urlImageArtist)
                        .into(artistImageView);

            }
        }
    }
}
