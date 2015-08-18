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
    public static final String ACTION_TRACK_BROADCAST_PROGRESS = "ACTION_TRACK_BROADCAST_PROGRESS";

    // internal action strings
    static final String ACTION_PLAYPAUSE = "ACTION_PLAYPAUSE";
    static final String ACTION_STOP = "ACTION_STOP";
    static final String ACTION_SEEK = "ACTION_SEEK";

    // internal and external object keys
    private static final String TRACK_URL = "TRACK_URL";
    public static final String TRACK_DURATION = "TRACK_DURATION";
    public static final String TRACK_PROGRESS = "TRACK_PROGRESS";
    private static final String TRACK_SEEK_POSITION = "TRACK_SEEK_POSITION";

    private String mTrackUrl;
    private MediaPlayer mMediaPlayer;
    private static Boolean mIsPreparing;
    private BroadcastProgressTask mBroadcastProgressTask;

    /* Public methods */
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

    public static void seek(Context context, int position) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        intent.setAction(ACTION_SEEK);
        intent.putExtra(TRACK_SEEK_POSITION, position);

        context.startService(intent);
    }

    public static boolean isPreparing() {
        return mIsPreparing;
    }

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
        resume();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Binding is not supported");
    }

    /* Private methods */
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

        //Seek new position
        if (intent.getAction().equals(ACTION_SEEK)) {
            int position = intent.getIntExtra(TRACK_SEEK_POSITION, 0);
            seek(position);
        }
    }

    // this method selects between playing and pausing
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

    private void seek(int position) {
        if (mMediaPlayer == null)
            return;

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(position);
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
    }

    private void stop() {
        Log.d(TAG_LOG, "Stopping playback of " + mTrackUrl);
        if (mMediaPlayer == null)
            return;

        if (mMediaPlayer.isPlaying())
            mMediaPlayer.stop();

        mTrackUrl = "";
        mIsPreparing = false;

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

        mBroadcastProgressTask = new BroadcastProgressTask();
        mBroadcastProgressTask.execute();
    }

    class BroadcastProgressTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            final long SLEEP_TIME = 250;
            long sleepTime = 0;

            while (!isCancelled()) {
                try {
                    Thread.sleep(sleepTime);
                    if (0 == sleepTime) {
                        sleepTime = SLEEP_TIME;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // checking for null to synchronize the stop() and BroadcastProgress functionalities
                // after the playback position was updated followed by the immediate fragment dismissal
                if (null == mMediaPlayer
                        || !mMediaPlayer.isPlaying())
                    return null;

                broadcastProgress();
            }

            return null;
        }
    }

    private void broadcastProgress() {
        Intent intent = new Intent(CALLBACK_MEDIASERVICE);
        intent.putExtra(CALLBACK_ACTION, ACTION_TRACK_BROADCAST_PROGRESS);
        intent.putExtra(TRACK_DURATION, mMediaPlayer.getDuration());
        intent.putExtra(TRACK_PROGRESS, mMediaPlayer.getCurrentPosition());
        sendBroadcast(intent);
    }

    // This is to make sure the service always broadcasts
    // the full duration of the track upon playback completion.
    // Basically we want to make sure the seek bar is always
    // in the rightmost position when the playback is completed
    private void broadcastMaxProgress() {
        Intent intent = new Intent(CALLBACK_MEDIASERVICE);
        intent.putExtra(CALLBACK_ACTION, ACTION_TRACK_BROADCAST_PROGRESS);
        intent.putExtra(TRACK_DURATION, mMediaPlayer.getDuration());
        intent.putExtra(TRACK_PROGRESS, mMediaPlayer.getDuration());
        sendBroadcast(intent);
    }
}