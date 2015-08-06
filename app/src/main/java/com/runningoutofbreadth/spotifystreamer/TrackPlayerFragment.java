package com.runningoutofbreadth.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class TrackPlayerFragment extends Fragment {
    private String trackPreviewUrl;
    private String trackArtist;
    private String trackAlbum;
    private String trackName;

    public TrackPlayerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        final View rootView = inflater.inflate(R.layout.fragment_track_player, container, false);

        if (intent != null && intent.hasExtra("URL")) {
            trackPreviewUrl = intent.getStringExtra("URL");
            trackArtist = intent.getStringExtra("Artist");
            trackAlbum = intent.getStringExtra("Album");
            trackName = intent.getStringExtra("Track");
        }

        try {
            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                        trackPreviewUrl + "\n"
                        + trackArtist + "\n"
                        + trackAlbum + "\n"
                        + trackName + "\n"
                    , Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        } catch (NullPointerException e) {
            Log.v("YO DAWG", "This don't got no url");
        }

        TextView artistTextView = (TextView) rootView.findViewById(R.id.player_artist_name);
        TextView albumTextView = (TextView) rootView.findViewById(R.id.player_album_name);
        TextView trackTextView = (TextView) rootView.findViewById(R.id.player_track_name);


        artistTextView.setText(trackArtist);
        albumTextView.setText(trackAlbum);
        trackTextView.setText(trackName);

        return rootView;
    }
}