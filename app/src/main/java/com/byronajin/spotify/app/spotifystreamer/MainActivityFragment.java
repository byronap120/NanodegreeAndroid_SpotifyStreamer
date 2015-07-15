package com.byronajin.spotify.app.spotifystreamer;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
//import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import retrofit.RetrofitError;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    //Global vairiables
    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    SpotifyApi api;
    ArtistListArrayAdapter<MyArtist> artistAdapter;
    ListView list;
    LinearLayout finddArtistMessage,noResultMessage ;
    TextView textViewArtistNoFound;
    ArrayList<MyArtist> artistList;
    String actualQuery;
    boolean networkErrorConection;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        api = new SpotifyApi();
        actualQuery="";
        networkErrorConection = false;

        if(savedInstanceState == null || !savedInstanceState.containsKey("artistList")) {
            artistList = new ArrayList<MyArtist>();
        }
        else {
            artistList = savedInstanceState.getParcelableArrayList("artistList");
            actualQuery = savedInstanceState.getString("actualQuery");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_main, container, false);

        list = (ListView) view.findViewById(R.id.listViewArtists);
        finddArtistMessage = (LinearLayout) view.findViewById(R.id.resultMessages);
        noResultMessage = (LinearLayout) view.findViewById(R.id.artistNotFound);
        textViewArtistNoFound = (TextView) view.findViewById(R.id.textViewArtistNoFound);


        //I check this to enable the visibility of the listview
        if(artistList.size()>0){
            finddArtistMessage.setVisibility(View.INVISIBLE);
            list.setVisibility(View.VISIBLE);
            noResultMessage.setVisibility(View.INVISIBLE);
        }

        artistAdapter = new ArtistListArrayAdapter<MyArtist>(
                getActivity(),
                artistList);
        list.setAdapter(artistAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String idArtist = artistList.get(position).id;
                Intent intent = new Intent(getActivity(), TopTracks.class)
                        .putExtra(Intent.EXTRA_TEXT, idArtist).putExtra("artistName", artistList.get(position).name);
                startActivity(intent);
            }
        });


        return view;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("artistList", artistList);
        outState.putString("actualQuery", actualQuery);
        super.onSaveInstanceState(outState);
    }


    public class SearchSpotifyTask extends AsyncTask<String, Void, ArrayList<MyArtist>>
    {
        @Override
        protected ArrayList<MyArtist> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            //Create a new List of "MyArtist" that implement parcelable
            ArrayList<MyArtist> myArtist = new ArrayList<MyArtist>();

            try {
                //Get the information from API
                SpotifyService service = api.getService();
                ArtistsPager results = service.searchArtists(params[0]);
                List<Artist> artists = results.artists.items;
                for (int i = 0; i < artists.size(); i++) {
                    myArtist.add(new MyArtist(artists.get(i)));
                }
            }catch (RetrofitError error){
                Log.e("RetrofitError", error.toString());
                networkErrorConection = true;
                return null;
            }

            return myArtist;
        }

        @Override
        protected void onPostExecute(ArrayList<MyArtist> artistListFromAPI) {
            if(artistListFromAPI != null){
                artistList = artistListFromAPI;
                if(artistListFromAPI.size()==0){
                    finddArtistMessage.setVisibility(View.INVISIBLE);
                    list.setVisibility(View.INVISIBLE);
                    noResultMessage.setVisibility(View.VISIBLE);
                    textViewArtistNoFound.setText("No artist found for \"" +actualQuery + "\"");
                }else{
                    finddArtistMessage.setVisibility(View.INVISIBLE);
                    list.setVisibility(View.VISIBLE);
                    noResultMessage.setVisibility(View.INVISIBLE);
                    artistAdapter.clear();
                    for(MyArtist artist : artistListFromAPI) {
                        artistAdapter.add(artist);
                    }
                    artistAdapter.notifyDataSetChanged();
                }
            }else if(networkErrorConection == true){
                showDialogError();
            }
        }
    }

    public void showDialogError(){
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Network Error");
        alertDialog.setMessage("Please check your network connection and try again ");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);


        SearchView.OnQueryTextListener searchListener = new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // do my stuff
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                actualQuery = s;
                String artistName = s.toString().toLowerCase(Locale.getDefault());
                if(artistName.length()>0){
                    new SearchSpotifyTask().execute(artistName);
                }else{
                    finddArtistMessage.setVisibility(View.VISIBLE);
                    noResultMessage.setVisibility(View.INVISIBLE);
                    list.setVisibility(View.INVISIBLE);
                }
                return false;
            }
        };

        searchView.setOnQueryTextListener(searchListener);

        if(!actualQuery.equals("")){

            //searchView.requestFocus();
            searchView.setQuery(actualQuery,false);
            searchView.clearFocus();
        }
    }
}
