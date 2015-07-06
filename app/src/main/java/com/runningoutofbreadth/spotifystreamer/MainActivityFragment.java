package com.runningoutofbreadth.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    //TODO make mNames, mThumbUrls, mArtistIDs part of a class (maybe)
    private CustomAdapter mArtistAdapter;
    private String search;
    private List<String> mNames = new ArrayList<>();
    private List<String> mThumbUrls = new ArrayList<>();
    private List<String> mArtistIds = new ArrayList<>();

    public MainActivityFragment() {
    }

    public class CustomAdapter extends ArrayAdapter {
        //constructor.
        public CustomAdapter(Context context, int resource, List<String> items) {
            super(context, resource, items);
        }

        //TODO holder
        private class ArtistResult {
            ImageView thumb;
            TextView name;
        }

        //override getView method
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) this.getContext().
                        getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.individual_artist, parent, false);
            }

            TextView name = (TextView) rowView.findViewById(R.id.artist_name_text_view);
            ImageView thumbnail = (ImageView) rowView.findViewById(R.id.artist_thumbnail_image_view);

            name.setText(mNames.get(position));
            if (!mThumbUrls.get(position).isEmpty()) {
                Picasso.with(getContext()).load(mThumbUrls.get(position)).into(thumbnail);
            } else {
                //if no images (no url), use default picture
                thumbnail.setImageResource(R.drawable.eigth_notes);
            }
            return rowView;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final String LOG_TAG = FetchArtistData.class.getSimpleName();
        final View rootView = inflater.inflate(R.layout.fragment_search_artists, container, false);

        //search field on top of screen, made so that hitting Next runs search
        final EditText editText = (EditText) rootView.findViewById(R.id.search_edit_text);

        //create empty nested string arrays to hold each list view item (artist name and pic)
        String[] results = new String[mNames.size()];
        mNames.toArray(results);
        mArtistAdapter = new CustomAdapter(getActivity(), R.layout.individual_artist, mNames);

        //onClick, open up new activity
        final ListView list = (ListView) rootView.findViewById(R.id.artist_list_view);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String artistId = mArtistIds.get(position);
                Intent intent = new Intent(getActivity(), ArtistTracks.class)
                        .putExtra(Intent.EXTRA_TEXT, artistId);
                startActivity(intent);
            }
        });

        //once you hit enter key (done/next/etc.), Asynctask runs
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                v = editText;
                search = v.getText().toString();
                search = "muse";
                //check to make sure search actually spits out as string
//                Log.v(LOG_TAG, "THIS IS THE SEARCH: " + search);

                //starts up Spotify built-in http connection, returns goodies
                if (!search.isEmpty()) {
                    FetchArtistData fetchArtistData = new FetchArtistData();
                    fetchArtistData.execute(search);
                }
                return true;
            }
        });

        list.setAdapter(mArtistAdapter);
        return rootView;
    }

    public class FetchArtistData extends AsyncTask<String, Void, String[][]> {
        private final String LOG_TAG = FetchArtistData.class.getSimpleName();

        protected String[][] doInBackground(String... params) {

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager artistsPager = spotify.searchArtists(search);
            List<Artist> artistNameListItems = new ArrayList<Artist>(artistsPager.artists.items);

            // see how many artistResults are returned. expected 20
            int size = artistNameListItems.size();
            String[][] artistListString = new String[size][2];
//            Log.v(LOG_TAG, size + " ITEMS: " + artistNameListItems.toString());

            try {

                //get smallest picture by accessing last item in images array
                String individualArtistName;
                String thumbnailUrl;
                String artistId;

                for (Artist each : artistNameListItems) {
                    int currentIndex = artistNameListItems.indexOf(each);

                    //images. if many images, get 2nd to smallest. else, get smallest
                    int lastOne;
                    int imageListSize = each.images.size();
                    if (imageListSize == 0) {
                        thumbnailUrl = "";
                    } else {
                        if (imageListSize > 1) {
                            lastOne = imageListSize - 2;
                        } else {
                            lastOne = imageListSize - 1;
                        }
                        thumbnailUrl = each.images.get(lastOne).url;
                    }

                    //Artist mNames and mArtistIds;
                    individualArtistName = each.name;
                    artistId = each.id;


                    //convert mNames and images into a string array
                    String[] nameThumbId = {thumbnailUrl, individualArtistName, artistId};

                    //append list to results with results.add()
                    artistListString[currentIndex] = nameThumbId;
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "what happened?" + e);
                return null;
            }

            try {
                return artistListString;
            } catch (Exception e) {
                //no results? oh well
                Log.e(LOG_TAG, "No results for query", e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[][] result) {
            //takes all nested String arrays and adds them to mArtistAdapter
            if (result != null) {
                mNames.clear();
                mThumbUrls.clear();
                for (String[] each : result) {
                    String url = each[0];
                    String artistName = each[1];
                    String artistId = each[2];
                    mThumbUrls.add(url);
                    mNames.add(artistName);
                    mArtistIds.add(artistId);
//                    Log.v(LOG_TAG, "this is the names entry:  " + artistName);
//                    Log.v(LOG_TAG, "this is the url entry:  " + url);
//                    Log.v(LOG_TAG, "this is the id entry:  " + artistId);
                }
            }
        }
    }

}

