package net.catsonmars.android.spotifystreamer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
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
    public static final String ACTION_TRACK_BROADCAST_DURATION = "ACTION_TRACK_SET_DURATION";
    public static final String ACTION_TRACK_BROADCAST_PROGRESS = "ACTION_TRACK_BROADCAST_PROGRESS";

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
    private BroadcastProgressTask mBroadcastProgressTask;

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

    @Override
    public void onPrepared(MediaPlayer mp) {
        mIsPreparing = false;
        resume();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        broadcastMaxProgress();
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
        //Play/pause track
        if (intent.getAction().equals(ACTION_PLAYPAUSE)) {
            String trackUrl = intent.getStringExtra(TRACK_URL);
            playPause(trackUrl);
        }

        //Stop playback
        if (intent.getAction().equals(ACTION_STOP)) {
            stop();
        }
    }

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

    private void playPause(String trackUrl) {
        // return if the media service is already preparing a playback
        // TODO: here's a problem. if the user quickly clicks Next or Previous two or more times
        // the fragment and then service will appear out of sync, because one or more tracks
        // will catch the service while it's preparing the next track. These tracks will not be played
        // Instead of simply returning there must be some sort of blocking for the playPause(String...) method
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

        //Start Media Player
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
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

        if (mBroadcastProgressTask != null)
            mBroadcastProgressTask.cancel(true);

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

        if (mBroadcastProgressTask != null)
            mBroadcastProgressTask.cancel(true);
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
        intent.putExtra(CALLBACK_ACTION, ACTION_TRACK_BROADCAST_DURATION);
        intent.putExtra(TRACK_DURATION, mMediaPlayer.getDuration());
        sendBroadcast(intent);

        mBroadcastProgressTask = new BroadcastProgressTask();
        mBroadcastProgressTask.execute();

//        showNotification();
    }

    class BroadcastProgressTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    Thread.sleep(350);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!mMediaPlayer.isPlaying())
                    return null;

                broadcastProgress();
            }

            return null;
        }
    }

    private void broadcastProgress() {
        Intent intent = new Intent(CALLBACK_MEDIASERVICE);
        intent.putExtra(CALLBACK_ACTION, ACTION_TRACK_BROADCAST_PROGRESS);
        intent.putExtra(TRACK_PROGRESS, mMediaPlayer.getCurrentPosition());
        sendBroadcast(intent);
    }

    private void broadcastMaxProgress() {
        Intent intent = new Intent(CALLBACK_MEDIASERVICE);
        intent.putExtra(CALLBACK_ACTION, ACTION_TRACK_BROADCAST_PROGRESS);
        intent.putExtra(TRACK_PROGRESS, mMediaPlayer.getDuration());
        sendBroadcast(intent);
    }
}