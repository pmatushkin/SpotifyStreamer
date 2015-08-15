package net.catsonmars.android.spotifystreamer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

/**
 * Created by pmatushkin on 8/9/2015.
 */
public class MediaPlayerService extends Service
        implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnSeekCompleteListener {

    private static final String TAG_LOG = "SPOTIFY_STREAMER";

    // public object keys
    public static final String CALLBACK_MEDIASERVICE = "CALLBACK_MEDIASERVICE";
    public static final String CALLBACK_ACTION = "CALLBACK_ACTION";

    // public action strings
    public static final String ACTION_TRACK_PLAYED = "ACTION_TRACK_PLAYED";
    public static final String ACTION_TRACK_PAUSED = "ACTION_TRACK_PAUSED";
    public static final String ACTION_TRACK_STOPPED = "ACTION_TRACK_STOPPED";
    public static final String ACTION_TRACK_SET_DURATION = "ACTION_TRACK_SET_DURATION";

    // internal action strings
    static final String ACTION_PLAYPAUSE = "ACTION_PLAYPAUSE";
    static final String ACTION_STOP = "ACTION_STOP";

    // internal and external object keys
    private static final String TRACK_URL = "TRACK_URL";
    public static final String TRACK_DURATION = "TRACK_DURATION";
    public static final String TRACK_PROGRESS = "TRACK_PROGRESS";

    private String mTrackUrl;
    private MediaPlayer mMediaPlayer;
    private Boolean mIsPreparing;

    @Override
    public void onCreate() {
        super.onCreate();

        mTrackUrl = "";
        mIsPreparing = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG_LOG, "MediaPlayerService.onStartCommand");

        handleCommand(intent);

        return Service.START_NOT_STICKY;
    }

    //    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//
////        //Set tracks
////        if (intent.getAction().equals(ACTION_SET_TRACKS)) {
////            setTracks(intent);
////        }
////
////        //Previous track
////        if (intent.getAction().equals(ACTION_PLAY_PREVIOUS_TRACK)) {
////            playPreviousTrack();
////        }
//
//        //Play track
//        if (intent.getAction().equals(ACTION_PLAY)) {
//            String trackUrl = intent.getStringExtra(TRACK_URL);
//            play(trackUrl);
//        }
//
////        //Pause track
////        if (intent.getAction().equals(ACTION_PAUSE_TRACK)) {
////            pauseTrack();
////        }
////
////        //Resume track
////        if (intent.getAction().equals(ACTION_RESUME_TRACK)) {
////            resumeTrack();
////        }
////
////        //Next track
////        if (intent.getAction().equals(ACTION_PLAY_NEXT_TRACK)) {
////            playNextTrack();
////        }
////
////        //Set track progress
////        if (intent.getAction().equals(ACTION_SET_TRACK_PROGRESS_TO)) {
////            int progress = intent.getIntExtra(TRACK_PROGRESS, 0);
////            setTrackProgressTo(progress);
////        }
////
////        //Request current track broadcast
////        if (intent.getAction().equals(ACTION_BROADCAST_CURRENT_TRACK)) {
////            if (mCurrentTrack != null)
////                broadcastTrackToBePlayed();
////        }
//
//        return START_STICKY;
//    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mIsPreparing = false;
        resume();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stop();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Binding is not supported");
    }

    private void handleCommand(Intent intent) {
//        if (null == intent)
//            return;

        //Play/pause track
        if (intent.getAction().equals(ACTION_PLAYPAUSE)) {
            String trackUrl = intent.getStringExtra(TRACK_URL);
            playPause(trackUrl);
        }

        //Stop media playback
        if (intent.getAction().equals(ACTION_STOP)) {
            stop();
        }
//
//        //Pause track
//        if (intent.getAction().equals(ACTION_PAUSE)) {
//            pause();
//        }
    }

//    public static Boolean isPlaying() {
//        if (mMediaPlayer == null) {
//            return false;
//        } else {
//            return mMediaPlayer.isPlaying();
//        }
//    }

    public static void playPause(Context context, String trackUrl) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(ACTION_PLAYPAUSE);
        intent.putExtra(TRACK_URL, trackUrl);

        context.startService(intent);
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(ACTION_STOP);

        context.startService(intent);
    }

//    public static void stopService(Context context) {
//        Intent intent = new Intent(context, MediaPlayerService.class);
//
//        context.stopService(intent);
//    }

    private void playPause(String trackUrl) {
        // return if the media service is already preparing a playback
        if (mIsPreparing) {
            Log.d(TAG_LOG, "Media service is already preparing a playback; returning...");

            return;
        }

        if (isPlaying(trackUrl)) {
            pause(trackUrl);
        } else {
            if (null == mTrackUrl || !mTrackUrl.equals(trackUrl)) {
                play(trackUrl);
            } else {
                resume();
            }
        }
    }

    private boolean isPlaying(String trackUrl) {
        return null != mMediaPlayer
                && mMediaPlayer.isPlaying()
                && null != mTrackUrl
                && !mTrackUrl.isEmpty()
                && mTrackUrl.equals(trackUrl);
    }

    private void play(String trackUrl) {
        Log.d(TAG_LOG, "Starting playback of " + trackUrl);
        //Stop playback
        stop();

//        //Get track
//        mCurrentTrack = mTracksList.get(trackId);
//        mCurrentTrackIndex = mTracksList.indexOf(mCurrentTrack);
//        String trackUrl = mCurrentTrack.previewUrl;

//        //Notify track to be played
//        broadcastTrackToBePlayed();

        //Start Media Player
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
//        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        try {
            mIsPreparing = true;

            mTrackUrl = trackUrl;
            mMediaPlayer.setDataSource(trackUrl);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            mIsPreparing = false;
            e.printStackTrace();
        }
    }

    private void pause(String trackUrl) {
        Log.d(TAG_LOG, "Pausing playback of " + trackUrl);
        if (mMediaPlayer == null)
            return;

        mMediaPlayer.pause();

        Intent intent = new Intent(CALLBACK_MEDIASERVICE);
        intent.putExtra(CALLBACK_ACTION, ACTION_TRACK_PAUSED);
        sendBroadcast(intent);

//        if (mBroadcastTrackProgressTask != null)
//            mBroadcastTrackProgressTask.cancel(true);
//
//        showNotification();
    }

    private void stop() {
        Log.d(TAG_LOG, "Stopping playback of " + mTrackUrl);
        if (mMediaPlayer == null)
            return;

        if (mMediaPlayer.isPlaying())
            mMediaPlayer.stop();

        mTrackUrl = "";
        mMediaPlayer.setOnPreparedListener(null);
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;

        Intent intent = new Intent(CALLBACK_MEDIASERVICE);
        intent.putExtra(CALLBACK_ACTION, ACTION_TRACK_STOPPED);
        sendBroadcast(intent);

        //this.stopService();

//        if (mBroadcastTrackProgressTask != null)
//            mBroadcastTrackProgressTask.cancel(true);
    }

    private void resume() {
        Log.d(TAG_LOG, "Resuming playback of " + mTrackUrl);
        if (mMediaPlayer == null)
            return;

        mMediaPlayer.start();

        Intent intent = new Intent(CALLBACK_MEDIASERVICE);
        intent.putExtra(CALLBACK_ACTION, ACTION_TRACK_PLAYED);
        sendBroadcast(intent);

        intent = new Intent(CALLBACK_MEDIASERVICE);
        intent.putExtra(CALLBACK_ACTION, ACTION_TRACK_SET_DURATION);
        intent.putExtra(TRACK_DURATION, mMediaPlayer.getDuration());
        sendBroadcast(intent);

//        mBroadcastTrackProgressTask = new BroadcastTrackProgressTask();
//        mBroadcastTrackProgressTask.execute();
//
//        showNotification();
    }
}