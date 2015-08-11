package net.catsonmars.android.spotifystreamer;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

public class NowPlayingFragment extends DialogFragment {
    private static final String TAG_LOG = "SPOTIFY_STREAMER";

    private static MediaPlayerService mediaPlayerService;

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG_LOG, "NowPlayingFragment.onCreateDialog");

        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    /**
     * The system calls this to get the DialogFragment's layout, regardless
     * of whether it's being displayed as a dialog or an embedded fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG_LOG, "NowPlayingFragment.onCreateView");

        if (null == mediaPlayerService) {
            Log.d(TAG_LOG, "Initializing media player in onCreateView...");
            mediaPlayerService = new MediaPlayerService();
        } else {
            Log.d(TAG_LOG, "Media player is already initialized");
        }

        // Inflate the layout to use as dialog or embedded fragment
        View rootView = inflater.inflate(R.layout.fragment_now_playing, container, false);


//        playButton.setOnClickListener(this);
//        nextButton.setOnClickListener(this);
//        previousButton.setOnClickListener(this);

        if (null == savedInstanceState) {
//            trackToPlayList = getArguments().getParcelableArrayList(TRACK_INFO_KEY);
//            trackIdx = getArguments().getInt(TRACK_IDX_KEY, -1);
//
//            if (trackIdx != -1) {
//                MediaPlayerService.playTrack(getActivity(), trackIdx);
//            }
        } else {
//            MediaPlayerService.broadcastCurrentTrack(getActivity());

        }

//        scrubBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (fromUser) {
//                    MediaPlayerService.setTrackProgressTo(getActivity(), 300 * progress);
//                }
//
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });

        return rootView;
    }

    @Override
    public void onStart() {
        Log.d(TAG_LOG, "NowPlayingFragment.onStart");

        super.onStart();
    }

    @Override
    public void onStop() {
        Log.d(TAG_LOG, "NowPlayingFragment.onStop");

        super.onStop();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG_LOG, "NowPlayingFragment.onCreate");

        super.onCreate(savedInstanceState);

        this.setRetainInstance(true);
    }

    @Override
    public void onPause() {
        Log.d(TAG_LOG, "NowPlayingFragment.onPause");

        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d(TAG_LOG, "NowPlayingFragment.onResume");

        super.onResume();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.d(TAG_LOG, "NowPlayingFragment.onDismiss");

        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Log.d(TAG_LOG, "NowPlayingFragment.onCancel");

        super.onCancel(dialog);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG_LOG, "NowPlayingFragment.onSaveInstanceState");

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG_LOG, "NowPlayingFragment.onDestroy");

        super.onDestroy();

        Log.d(TAG_LOG, "Stopping media player in onDestroy...");
        mediaPlayerService = null;
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG_LOG, "NowPlayingFragment.onDestroyView");

        // http://stackoverflow.com/a/15444485
        // "You may have to add this code to stop your dialog from being dismissed on rotation,
        // due to a bug with the compatibility library"
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);

        super.onDestroyView();
    }
}