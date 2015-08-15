package net.catsonmars.android.spotifystreamer;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class NowPlayingFragment extends DialogFragment
        implements View.OnClickListener {
    private static final String TAG_LOG = "SPOTIFY_STREAMER";

    private static final String STATE_SAVED = "STATE_SAVED";

//    private static MediaPlayerService mMediaPlayerService;

    private TopTenTracksCallback mTopTenTracks;

    private Track mCurrentTrack;

    View rootView;

    public interface TopTenTracksCallback {
        Activity getActivity();

        Track getCurrentTrack();
        //Track getNextTrack();
        //Track getPreviousTrack();
        Boolean moveToPreviousTrack();
        Boolean moveToNextTrack();
    }

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

//        if (null == mMediaPlayerService) {
//            Log.d(TAG_LOG, "Initializing media player in onCreateView...");
//            mMediaPlayerService = new MediaPlayerService();
//        } else {
//            Log.d(TAG_LOG, "Media player is already initialized");
//        }

        // Inflate the layout to use as dialog or embedded fragment
        rootView = inflater.inflate(R.layout.fragment_now_playing, container, false);

        mCurrentTrack = mTopTenTracks.getCurrentTrack();
        if (null == mCurrentTrack) {
            Log.d(TAG_LOG, "Current track: NULL");
        } else {
            Log.d(TAG_LOG, "Current track: " + mCurrentTrack.name);
        }

        Boolean startPlayback = null == savedInstanceState
                || !savedInstanceState.containsKey(STATE_SAVED);
        loadCurrentTrack(startPlayback);

        View viewPlayPause = rootView.findViewById(R.id.btnPlayPause);
        if (null == viewPlayPause) {
            Log.e(TAG_LOG, "Play/Pause button not found; check the Now Playing layout");
        } else {
            viewPlayPause.setOnClickListener(this);
        }
        View viewPreviousTrack = rootView.findViewById(R.id.btnPreviousTrack);
        if (null == viewPreviousTrack) {
            Log.e(TAG_LOG, "Previous button not found; check the Now Playing layout");
        } else {
            viewPreviousTrack.setOnClickListener(this);
        }
        View viewNextTrack = rootView.findViewById(R.id.btnNextTrack);
        if (null == viewNextTrack) {
            Log.e(TAG_LOG, "Next button not found; check the Now Playing layout");
        } else {
            viewNextTrack.setOnClickListener(this);
        }

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

    private void loadCurrentTrack() {
        loadCurrentTrack(true);
    }

    private void loadCurrentTrack(Boolean startPlayback) {
        Context context = mTopTenTracks.getActivity();
        View v = rootView;

        if (null == mCurrentTrack) {
            Toast toast = Toast.makeText(context, getString(R.string.warn_empty_current_track), Toast.LENGTH_SHORT);
            toast.show();
        } else {
            TextView tt;
            ImageView img;

            tt = (TextView) v.findViewById(R.id.txtTrackName);
            if (null == tt) {
            } else {
                tt.setText(mCurrentTrack.name);
            }

            tt = (TextView) v.findViewById(R.id.txtAlbumName);
            if (null == tt) {
            } else {
                tt.setText(mCurrentTrack.album.name);
            }

            tt = (TextView) v.findViewById(R.id.txtArtistName);
            if (null == tt) {
            } else {
                if ((null == mCurrentTrack.artists) || mCurrentTrack.artists.isEmpty()) {
                    tt.setText("");
                } else {
                    tt.setText(mCurrentTrack.artists.get(0).name);
                }
            }

            img = (ImageView) v.findViewById(R.id.imgTrackArt);
            if (null == img) {
            } else {
                if (null == mCurrentTrack.album.images || mCurrentTrack.album.images.isEmpty()) {
                    Picasso.with(context)
                            .load(R.drawable.img_spotify_default)
                            .resize(500, 500)
                            .into(img);
                } else {
                    // try to get the large size image for the list view
                    // it's usually the first image on the result list
                    Image image = mCurrentTrack.album.images.get(0);

                    Picasso.with(context)
                            .load(image.url)
                            .placeholder(R.drawable.img_spotify_default)
                            .error(R.drawable.img_spotify_default)
                            .resize(500, 500)
                            .into(img);
                }
            }

            if (startPlayback) {
                View viewPlayPause = v.findViewById(R.id.btnPlayPause);
                onClick(viewPlayPause);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPreviousTrack:
                onPreviousTrack();
                break;
            case R.id.btnPlayPause:
                playPause(v);
                break;
            case R.id.btnNextTrack:
                onNextTrack();
                break;
            default:
                break;
        }
    }

    private void playPause(View v) {
        Log.d(TAG_LOG, "NowPlayingFragment.playPause");
        Log.d(TAG_LOG, mCurrentTrack.preview_url);

        MediaPlayerService.playPause(mTopTenTracks.getActivity(), mCurrentTrack.preview_url);
    }

    private void onPreviousTrack() {
        if (mTopTenTracks.moveToPreviousTrack()) {
            mCurrentTrack = mTopTenTracks.getCurrentTrack();
            loadCurrentTrack();
        } else {
            Toast toast = Toast.makeText(mTopTenTracks.getActivity(), getString(R.string.warn_no_previous_track), Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void onNextTrack() {
        if (mTopTenTracks.moveToNextTrack()) {
            mCurrentTrack = mTopTenTracks.getCurrentTrack();
            loadCurrentTrack();
        } else {
            Toast toast = Toast.makeText(mTopTenTracks.getActivity(), getString(R.string.warn_no_next_track), Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @Override
    public void onStart() {
        Log.d(TAG_LOG, "NowPlayingFragment.onStart");

        super.onStart();

        IntentFilter filter = new IntentFilter(MediaPlayerService.CALLBACK_MEDIASERVICE);
        mTopTenTracks.getActivity().registerReceiver(mOnMediaPlayerServiceCallback, filter);
    }

    @Override
    public void onStop() {
        Log.d(TAG_LOG, "NowPlayingFragment.onStop");

        super.onStop();
        mTopTenTracks.getActivity().unregisterReceiver(mOnMediaPlayerServiceCallback);
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

        outState.putBoolean(STATE_SAVED, true);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG_LOG, "NowPlayingFragment.onDestroy");

        super.onDestroy();

        Log.d(TAG_LOG, "Stopping media player in onDestroy...");
        MediaPlayerService.stop(mTopTenTracks.getActivity());
//        Log.d(TAG_LOG, "Stopping media player service in onDestroy...");
//        MediaPlayerService.stopService(mTopTenTracks.getActivity());
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

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG_LOG, "NowPlayingFragment.onAttach");
        if (null == activity) {
            Log.d(TAG_LOG, "activity is NULL");
        } else {
            Log.d(TAG_LOG, "activity is NOT NULL... can attach");
        }

        mTopTenTracks = (TopTenTracksCallback)activity;

        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        Log.d(TAG_LOG, "NowPlayingFragment.onDetach");

        mTopTenTracks = null;

        super.onDetach();
    }

    public BroadcastReceiver mOnMediaPlayerServiceCallback = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleServiceCallback(intent);
        }
    };

    private void handleServiceCallback(Intent intent) {
        if (intent.getAction().equals(MediaPlayerService.CALLBACK_MEDIASERVICE)) {
            String callbackAction = intent.getStringExtra(MediaPlayerService.CALLBACK_ACTION);

            switch (callbackAction) {
                case MediaPlayerService.ACTION_TRACK_PLAYED : {
                    onTrackPrepared(intent);

                    break;
                }
                case MediaPlayerService.ACTION_TRACK_PAUSED : {
                    onTrackPaused(intent);

                    break;
                }
                case MediaPlayerService.ACTION_TRACK_STOPPED : {
                    onTrackPaused(intent);

                    break;
                }
                default : {
                    Log.d(TAG_LOG, "Unknown callback action: " + intent.getAction());

                    break;
                }
            }
        } else {
            Log.d(TAG_LOG, "Unknown callback object: " + intent.getAction());
        }
        Toast.makeText(mTopTenTracks.getActivity(), "OnPrepared received", Toast.LENGTH_SHORT).show();
    }

    private void onTrackPrepared(Intent intent) {
        View viewPlayPause = rootView.findViewById(R.id.btnPlayPause);

        if (null == viewPlayPause) {
            Log.e(TAG_LOG, "Play/Pause button not found; check the Now Playing layout");
        } else {
            ((Button)viewPlayPause).setCompoundDrawablesRelativeWithIntrinsicBounds(android.R.drawable.ic_media_pause, 0, 0, 0);
        }
    }

    private void onTrackPaused(Intent intent) {
        View viewPlayPause = rootView.findViewById(R.id.btnPlayPause);

        if (null == viewPlayPause) {
            Log.e(TAG_LOG, "Play/Pause button not found; check the Now Playing layout");
        } else {
            ((Button)viewPlayPause).setCompoundDrawablesRelativeWithIntrinsicBounds(android.R.drawable.ic_media_play, 0, 0, 0);
        }
    }
}