package com.android.musicplayertemplate;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private MediaPlayer mPlayer = null;

    private ArrayList<Songs> mSongs;

    private int mSongPosition;

    private IBinder musicBind = new MusicBinder();

    private MediaSession mediaSession;
    private MediaSessionManager mediaSessionManager;
    private MediaController mController;

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";

    private int duration;
    private MediaPlayer mediaPlayer;

    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSongPosition = 0;
        mPlayer = new MediaPlayer();
//        mPlayer=
        initMusicPlayer();

    }


    public void initMusicPlayer() {
        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnErrorListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return musicBind;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mPlayer.start();
        duration = mediaPlayer.getDuration();
        this.mediaPlayer=mediaPlayer;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onUnbind(Intent intent) {
        mPlayer.stop();
        mPlayer.release();
        mediaSession.release();
        return false;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();

        if (action.equalsIgnoreCase(ACTION_PLAY)) {
            mController.getTransportControls().play();
        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            mController.getTransportControls().pause();
        } else if (action.equalsIgnoreCase(ACTION_NEXT)) {
            mController.getTransportControls().skipToNext();
        } else {
            mController.getTransportControls().skipToPrevious();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    private Notification.Action genereateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mediaSession.setSessionActivity(pendingIntent);
        return new Notification.Action.Builder(icon, title, pendingIntent).build();

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void buildNotification(Notification.Action action) {
        Notification.MediaStyle style = new Notification.MediaStyle();
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        intent.setAction(ACTION_PAUSE);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("Lock Screen Media Example")
                .setContentText("Artist Namse")
                .setDeleteIntent(pendingIntent)
                .setStyle(style);

        builder.addAction(genereateAction(R.drawable.ic_action_skipprevious, "Previous", ACTION_PREVIOUS));
        builder.addAction(genereateAction(R.drawable.ic_action_skipnext, "Next", ACTION_NEXT));
        builder.addAction(action);
        builder.addAction(genereateAction(R.drawable.ic_action_play, "Play", ACTION_PLAY));
        builder.addAction(genereateAction(R.drawable.ic_action_pause, "Pause", ACTION_PAUSE));
        style.setShowActionsInCompactView(0, 1, 2, 4);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1, builder.build());


    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mediaSessionManager == null) {
            initMediaSession();
        }

        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initMediaSession() {

        mediaSession = new MediaSession(getApplicationContext(), "Exampl player session");
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mController = new MediaController(getApplicationContext(), mediaSession.getSessionToken());

        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                buildNotification(genereateAction(R.drawable.ic_action_play, "Play", ACTION_PLAY));

            }

            @Override
            public void onPause() {
                super.onPause();
                buildNotification(genereateAction(R.drawable.ic_action_pause, "Pause", ACTION_PAUSE));

            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();

                buildNotification(genereateAction(R.drawable.ic_action_skipnext, "Next", ACTION_NEXT));

            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();

                buildNotification(genereateAction(R.drawable.ic_action_skipprevious, "Previous", ACTION_PREVIOUS));

            }

            @Override
            public void onStop() {
                super.onStop();
            }
        });

    }


    public void playSong() {
        mPlayer.reset();

        Songs playSong = mSongs.get(mSongPosition);

        long curreSog = playSong.getmSongID();

        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, curreSog);

        try {
            mPlayer.setDataSource(getApplicationContext(), trackUri);

        } catch (IOException e) {
            e.printStackTrace();
        }
        mPlayer.prepareAsync();
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }


    public void setmSongs(int songIndex) {
        mSongPosition = songIndex;
    }

    public void setList(ArrayList<Songs> theSongs) {
        mSongs = theSongs;
    }


    public MediaPlayer getMediaPlayer() {
        return mPlayer;
    }




    public int getDuration() {
        return duration;
    }

    public int getCurrentDuration() {
        return mediaPlayer.getCurrentPosition();
    }


    @Override
    public void onDestroy() {

        NotificationManager notificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        stopService(intent);
        super.onDestroy();

    }


}
