package com.android.musicplayertemplate;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

public class MusicPlayerActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener, MediaPlayer.OnCompletionListener {
    private ArrayList<Songs> songList = new ArrayList<>();
    private RecyclerView recyclerView;
    private DataAdapter mAdapter;
    Uri songUri;

    private static final int PERMISSION_READ_EXTERNAL_STORAGE = 12;

    private ImageButton imgBttnPrevious;
    private ImageButton imgBttnPlayPause;
    private ImageButton imgBttnNext;
    private TextView startDurationTxtView;
    private TextView totalDurationTxtView;
    private SeekBar mSeekBar;
    private ImageButton shuffleBttn;
    private ImageButton loopBttn;

    private TextView songTitle;
    private TextView songArtist;
    private ImageButton songPlayPauseBttn;

    private double timeElapsed = 0, finalTime = 0;
    private Handler durationHandler = new Handler();
    private int mediaPos, mediaMax;
    private MediaUtils mediaUtils;
    private int seekForwardTime = 5000;
    private int seekBackWardTime = 5000;
    private SlidingUpPanelLayout mLayout;
    private MediaPlayer mPlayer = null;

    private MusicService musicService;
    private Intent playInent;
    private boolean musicBound = false;

    MusicService.MusicBinder binder;

    private static int oneTimeOnly = 0;

    MediaUtils utils;

    private boolean playPauseFlag = false;
    private HashMap<String, String> lastInsertId;
    private HashMap<String, String> mPauseProgress;

    private int currentSongIndex = 0;

    private boolean isShuffle = false;
    private boolean isRepeat = false;


    private ImageView albumArtImageView;
    private ImageView albumImg;


    private RelativeLayout relativeLayout;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();

        ImageView ic_play = (ImageView) findViewById(R.id.imageViewPlay);
        int color1 = Color.parseColor("#ffffff");
        ic_play.setColorFilter(color1);

        ImageView ic_next = (ImageView) findViewById(R.id.imageViewNext);
        int color2 = Color.parseColor("#ffffff");
        ic_next.setColorFilter(color2);

        ImageView ic_previous = (ImageView) findViewById(R.id.imageViewPrevious);
        int color3 = Color.parseColor("#ffffff");
        ic_previous.setColorFilter(color3);
        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
            }
        });
        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "under construction", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new DataAdapter(songList, this, new DataAdapter.OnItemClickListener() {
            @Override
            public void onTemClick(Songs item, int position) {
                Toast.makeText(MusicPlayerActivity.this, "" + item.getmSongTitle(), Toast.LENGTH_SHORT).show();
                imgBttnPlayPause.setOnClickListener(MusicPlayerActivity.this);

                songPlayPauseBttn.setOnClickListener(MusicPlayerActivity.this);

                songPlayPauseBttn.setActivated(true);
                imgBttnPlayPause.setActivated(true);
                if (item.getAlbumArt() != null) {
                    albumArtImageView.setImageBitmap(item.getAlbumArt());
                    albumImg.setImageBitmap(item.getAlbumArt());
                } else {
                    albumArtImageView.setImageResource(R.drawable.oldeis);
                    albumImg.setImageResource(R.drawable.oldeis);
                }
                imgBttnPlayPause.setImageResource(R.drawable.ic_action_pause);
                songPlayPauseBttn.setImageResource(R.drawable.ic_action_pause);
                songArtist.setText(item.getmSongArtist());
                songTitle.setText(item.getmSongTitle());
                playPauseFlag = true;
                musicService.setmSongs(position);
                musicService.playSong();
                musicService.getMediaPlayer().setOnCompletionListener(MusicPlayerActivity.this);
                mSeekBar.setProgress(0);
                mSeekBar.setMax(100);
                durationHandler.postDelayed(updateSongTime, 100);
                lastInsertId.put("id", String.valueOf(position));
                currentSongIndex = position;


//                Intent intent = new Intent(MusicPlayerActivity.this,MusicService.class);
//                intent.setAction(MusicService.ACTION_PLAY);
//                startService(intent);

            }

        });

        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        Collections.sort(songList, new Comparator<Songs>() {
            @Override
            public int compare(Songs songs, Songs t1) {
                return songs.getmSongArtist().compareTo(t1.getmSongArtist());
            }
        });
        recyclerView.setAdapter(mAdapter);
        loadSongsFromInternalStorage();
//        fetchSongs();
    }


    private void init() {
        imgBttnPrevious = (ImageButton) findViewById(R.id.imageViewPrevious);
        imgBttnPlayPause = (ImageButton) findViewById(R.id.imageViewPlay);
        imgBttnNext = (ImageButton) findViewById(R.id.imageViewNext);
        startDurationTxtView = (TextView) findViewById(R.id.startDurationTxtView);
        totalDurationTxtView = (TextView) findViewById(R.id.totalDurationTxtView);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        shuffleBttn = (ImageButton) findViewById(R.id.shuffleBttn);
        loopBttn = (ImageButton) findViewById(R.id.loopBttn);
        albumArtImageView = (ImageView) findViewById(R.id.albumArtImageView);
        albumImg = (ImageView) findViewById(R.id.albumImg);


        imgBttnPlayPause.setActivated(false);
        songTitle = (TextView) findViewById(R.id.tv_songsName);
        songArtist = (TextView) findViewById(R.id.artistsong);
        songPlayPauseBttn = (ImageButton) findViewById(R.id.playPauseBttn);
        int color = Color.parseColor("#000000");
        songPlayPauseBttn.setColorFilter(color);
        songPlayPauseBttn.setActivated(false);


        mSeekBar.setOnSeekBarChangeListener(this);
        utils = new MediaUtils();
        imgBttnNext.setOnClickListener(this);
        imgBttnPrevious.setOnClickListener(this);

        shuffleBttn.setOnClickListener(this);
        loopBttn.setOnClickListener(this);

        lastInsertId = new HashMap<>();
        mPauseProgress = new HashMap<>();


    }


    @Override
    protected void onStart() {
        super.onStart();

        if (playInent == null) {
            playInent = new Intent(MusicPlayerActivity.this, MusicService.class);
            bindService(playInent, musicConnection, BIND_AUTO_CREATE);
            startService(playInent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_music_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mLayout != null &&
                (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
//            stopService(playInent);
//            musicService = null;
//            doUnBindService();
            System.out.println("inside if");

        } else {
            super.onBackPressed();
//            stopService(playInent);
//            musicService = null;
//            doUnBindService();
            System.out.println("inside else" + musicService.getMediaPlayer());
        }
    }


    private void loadSongsFromInternalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ_EXTERNAL_STORAGE);
        } else {
            ContentResolver contentResolver = getContentResolver();
            songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor songCursor = contentResolver.query(songUri, null, null, null, null);
            if (songCursor != null && songCursor.moveToFirst()) {
                int songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

                do {
                    long currentId = songCursor.getLong(songId);
                    String currentTitle = songCursor.getString(songTitle);
                    String currentPath = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String currentArtist = songCursor.getString(songArtist);
                    long currentAlbumId = getAlbumId(String.valueOf(currentId));
//                    String currentAlbumArt = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));

                    Bitmap bmp = getAlbumArt(currentAlbumId);
                    songList.add(new Songs(currentId, currentTitle, currentPath, currentArtist, currentAlbumId, bmp));
                    mAdapter.notifyDataSetChanged();
                } while (songCursor.moveToNext());
            }
        }


    }

    public Long getAlbumId(String id) {
        Cursor musicCursor;
        String[] mProjection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID};
        String[] mArgs = {id};
        musicCursor = getContentResolver().query(songUri, mProjection, MediaStore.Audio.Media._ID + " = ?", mArgs, null);
        musicCursor.moveToFirst();
        Long albumId = musicCursor.getLong(musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
        return albumId;
    }

    public Bitmap getAlbumArt(Long albumId) {
        Bitmap albumArt = null;
        try {
            final Uri AlbumArtUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(AlbumArtUri, albumId);
            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r");

            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                albumArt = BitmapFactory.decodeFileDescriptor(fd);
            }
        } catch (Exception e) {
        }
        return albumArt;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadSongsFromInternalStorage();
//                fetchSongs();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (MusicService.MusicBinder) iBinder;

            musicService = binder.getService();
            musicService.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicBound = false;
            binder = null;
        }
    };


    @Override
    protected void onDestroy() {
        stopService(playInent);
        doUnBindService();
        musicService = null;
        super.onDestroy();

    }

    public void doUnBindService() {
        if (musicBound) {
            unbindService(musicConnection);
            musicBound = false;
        }
    }


    private Runnable updateSongTime = new Runnable() {
        @Override
        public void run() {
//            if (musicService.getMediaPlayer() != null) {
//                MediaPlayer mediaPlayer = musicService.getMediaPlayer();

            long totalDuration = musicService.getDuration();
            long currentDuration = musicService.getCurrentDuration();

            totalDurationTxtView.setText(utils.milliSecontsToTImer(totalDuration));
            startDurationTxtView.setText(utils.milliSecontsToTImer(currentDuration));
            int progress = utils.getProgressPercentage(currentDuration, totalDuration);
            mSeekBar.setProgress(progress);
            durationHandler.postDelayed(this, 100);
//            } else {
//                doUnBindService();
//            }

        }
    };


    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (b) {
            MediaPlayer player = musicService.getMediaPlayer();
            player.seekTo(i);
            seekBar.setProgress(i);

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        durationHandler.removeCallbacks(updateSongTime);

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        durationHandler.removeCallbacks(updateSongTime);
        int totalDuration = musicService.getMediaPlayer().getDuration();
        int currentPosition = utils.progressToTimer(mSeekBar.getProgress(), totalDuration);

        musicService.getMediaPlayer().seekTo(currentPosition);
        durationHandler.postDelayed(updateSongTime, 100);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageViewPlay:
            case R.id.playPauseBttn:
                if (playPauseFlag) {
                    imgBttnPlayPause.setImageResource(R.drawable.ic_action_play);
                    songPlayPauseBttn.setImageResource(R.drawable.ic_action_play);
                    playPauseFlag = false;
                    musicService.getMediaPlayer().pause();
                    mPauseProgress.put("progress", String.valueOf(musicService.getMediaPlayer().getCurrentPosition()));
                } else {
                    musicService.setmSongs(Integer.parseInt(lastInsertId.get("id")));
                    musicService.getMediaPlayer().seekTo(Integer.parseInt(mPauseProgress.get("progress")));
                    //durationHandler.postDelayed(updateSongTime, 100);
                    playPauseFlag = true;
                    musicService.getMediaPlayer().start();
                    imgBttnPlayPause.setImageResource(R.drawable.ic_action_pause);
                    songPlayPauseBttn.setImageResource(R.drawable.ic_action_pause);

                }


                break;


            case R.id.imageViewNext:
                if (currentSongIndex < (songList.size() - 1)) {
                    imgBttnPlayPause.setImageResource(R.drawable.ic_action_pause);
                    songPlayPauseBttn.setImageResource(R.drawable.ic_action_pause);
                    songArtist.setText(songList.get(currentSongIndex + 1).getmSongArtist());
                    songTitle.setText(songList.get(currentSongIndex + 1).getmSongTitle());
                    if (songList.get(currentSongIndex + 1).getAlbumArt() != null) {
                        albumArtImageView.setImageBitmap(songList.get(currentSongIndex + 1).getAlbumArt());
                        albumImg.setImageBitmap(songList.get(currentSongIndex + 1).getAlbumArt());
                    } else {
                        albumArtImageView.setImageResource(R.drawable.oldeis);
                        albumImg.setImageResource(R.drawable.oldeis);
                    }
                    playPauseFlag = true;
                    musicService.setmSongs(currentSongIndex + 1);
                    musicService.playSong();
                    mSeekBar.setProgress(0);
                    mSeekBar.setMax(100);
                    durationHandler.postDelayed(updateSongTime, 100);
                    lastInsertId.put("id", String.valueOf(songList.get(currentSongIndex + 1).getmSongID()));
                    currentSongIndex = currentSongIndex + 1;
                } else {

                    imgBttnPlayPause.setImageResource(R.drawable.ic_action_pause);
                    songPlayPauseBttn.setImageResource(R.drawable.ic_action_pause);
                    songArtist.setText(songList.get(0).getmSongArtist());
                    songTitle.setText(songList.get(0).getmSongTitle());
                    playPauseFlag = true;
                    musicService.setmSongs((int) songList.get(0).getmSongID());
                    if (songList.get(0).getAlbumArt() != null) {
                        albumArtImageView.setImageBitmap(songList.get(0).getAlbumArt());
                        albumImg.setImageBitmap(songList.get(0).getAlbumArt());
                    } else {
                        albumArtImageView.setImageResource(R.drawable.oldeis);
                        albumImg.setImageResource(R.drawable.oldeis);
                    }
                    musicService.playSong();
                    mSeekBar.setProgress(0);
                    mSeekBar.setMax(100);
                    durationHandler.postDelayed(updateSongTime, 100);
                    lastInsertId.put("id", String.valueOf(songList.get(0).getmSongID()));
                    currentSongIndex = 0;
                }

                break;


            case R.id.imageViewPrevious:
                if (currentSongIndex > 0) {
                    imgBttnPlayPause.setImageResource(R.drawable.ic_action_pause);
                    songPlayPauseBttn.setImageResource(R.drawable.ic_action_pause);
                    songArtist.setText(songList.get(currentSongIndex - 1).getmSongArtist());
                    songTitle.setText(songList.get(currentSongIndex - 1).getmSongTitle());
                    if (songList.get(currentSongIndex - 1).getAlbumArt() != null) {
                        albumArtImageView.setImageBitmap(songList.get(currentSongIndex - 1).getAlbumArt());
                        albumImg.setImageBitmap(songList.get(currentSongIndex - 1).getAlbumArt());
                    } else {
                        albumArtImageView.setImageResource(R.drawable.oldeis);
                        albumImg.setImageResource(R.drawable.oldeis);
                    }
                    playPauseFlag = true;
                    musicService.setmSongs(currentSongIndex - 1);
                    musicService.playSong();
                    mSeekBar.setProgress(0);
                    mSeekBar.setMax(100);
                    durationHandler.postDelayed(updateSongTime, 100);
                    lastInsertId.put("id", String.valueOf(songList.get(currentSongIndex - 1).getmSongID()));
                    currentSongIndex = currentSongIndex - 1;
                } else {

                    imgBttnPlayPause.setImageResource(R.drawable.ic_action_pause);
                    songPlayPauseBttn.setImageResource(R.drawable.ic_action_pause);
                    songArtist.setText(songList.get(songList.size() - 1).getmSongArtist());
                    songTitle.setText(songList.get(songList.size() - 1).getmSongTitle());
                    if (songList.get(currentSongIndex - 1).getAlbumArt() != null) {
                        albumArtImageView.setImageBitmap(songList.get(currentSongIndex).getAlbumArt());
                        albumImg.setImageBitmap(songList.get(currentSongIndex).getAlbumArt());
                    } else {
                        albumArtImageView.setImageResource(R.drawable.oldeis);
                        albumImg.setImageResource(R.drawable.oldeis);
                    }
                    playPauseFlag = true;
                    musicService.setmSongs((int) songList.get(songList.size() - 1).getmSongID());
                    musicService.playSong();
                    mSeekBar.setProgress(0);
                    mSeekBar.setMax(100);
                    durationHandler.postDelayed(updateSongTime, 100);
                    lastInsertId.put("id", String.valueOf(songList.get(songList.size() - 1).getmSongID()));
                    currentSongIndex = songList.size() - 1;
                }

                break;


            case R.id.shuffleBttn:
                if (isShuffle) {
                    isShuffle = false;
                    shuffleBttn.setImageResource(R.drawable.ic_action_shuffle);
                } else {
                    isShuffle = true;
                    isRepeat = false;
                    playPauseFlag = true;
                    shuffleBttn.setImageResource(R.drawable.ic_action_shuffle_focused);
                    loopBttn.setImageResource(R.drawable.ic_action_loop);
                    Random random = new Random();
                    currentSongIndex = random.nextInt((songList.size() - 1) - 0 + 1) + 0;


                    imgBttnPlayPause.setImageResource(R.drawable.ic_action_pause);
                    songPlayPauseBttn.setImageResource(R.drawable.ic_action_pause);
                    songArtist.setText(songList.get(currentSongIndex).getmSongArtist());
                    songTitle.setText(songList.get(currentSongIndex).getmSongTitle());
                    if (songList.get(currentSongIndex).getAlbumArt() != null) {
                        albumArtImageView.setImageBitmap(songList.get(currentSongIndex).getAlbumArt());
                        albumImg.setImageBitmap(songList.get(currentSongIndex).getAlbumArt());
                    } else {
                        albumArtImageView.setImageResource(R.drawable.oldeis);
                        albumImg.setImageResource(R.drawable.oldeis);
                    }
                    playPauseFlag = true;
                    musicService.setmSongs(currentSongIndex);
                    musicService.playSong();
                    mSeekBar.setProgress(0);
                    mSeekBar.setMax(100);
                    durationHandler.postDelayed(updateSongTime, 100);
                    lastInsertId.put("id", String.valueOf(currentSongIndex));
                }
                break;


            case R.id.loopBttn:
                if (isRepeat) {
                    isRepeat = false;
                    loopBttn.setImageResource(R.drawable.ic_action_loop);
                    musicService.getMediaPlayer().setLooping(false);
                } else {
                    isRepeat = true;
                    isShuffle = false;
                    loopBttn.setImageResource(R.drawable.ic_action_loop_focused);
                    shuffleBttn.setImageResource(R.drawable.ic_action_shuffle);
                    musicService.getMediaPlayer().setLooping(true);
                }

                break;


        }

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        if (isRepeat) {
            isRepeat = true;
            isShuffle = false;
            loopBttn.setImageResource(R.drawable.ic_action_loop_focused);
            shuffleBttn.setImageResource(R.drawable.ic_action_shuffle);
            musicService.getMediaPlayer().setLooping(true);
        } else if (isShuffle) {

            isShuffle = true;
            isRepeat = false;
            shuffleBttn.setImageResource(R.drawable.ic_action_shuffle_focused);
            loopBttn.setImageResource(R.drawable.ic_action_loop);
            Random random = new Random();
            currentSongIndex = random.nextInt((songList.size() - 1) - 0 + 1) + 0;


            imgBttnPlayPause.setImageResource(R.drawable.ic_action_pause);
            songPlayPauseBttn.setImageResource(R.drawable.ic_action_pause);
            songArtist.setText(songList.get(currentSongIndex).getmSongArtist());
            songTitle.setText(songList.get(currentSongIndex).getmSongTitle());
            if (songList.get(currentSongIndex).getAlbumArt() != null) {
                albumArtImageView.setImageBitmap(songList.get(currentSongIndex).getAlbumArt());
                albumImg.setImageBitmap(songList.get(currentSongIndex).getAlbumArt());
            } else {
                albumArtImageView.setImageResource(R.drawable.oldeis);
                albumImg.setImageResource(R.drawable.oldeis);
            }
            playPauseFlag = true;
            musicService.setmSongs(currentSongIndex);
            musicService.playSong();
            mSeekBar.setProgress(0);
            mSeekBar.setMax(100);
            durationHandler.postDelayed(updateSongTime, 100);
            lastInsertId.put("id", String.valueOf(songList.get(currentSongIndex).getmSongID()));
        } else {
            if (currentSongIndex < (songList.size() - 1)) {
                imgBttnPlayPause.setImageResource(R.drawable.ic_action_pause);
                songPlayPauseBttn.setImageResource(R.drawable.ic_action_pause);
                songArtist.setText(songList.get(currentSongIndex + 1).getmSongArtist());
                songTitle.setText(songList.get(currentSongIndex + 1).getmSongTitle());
                if (songList.get(currentSongIndex + 1).getAlbumArt() != null) {
                    albumArtImageView.setImageBitmap(songList.get(currentSongIndex + 1).getAlbumArt());
                    albumImg.setImageBitmap(songList.get(currentSongIndex + 1).getAlbumArt());
                } else {
                    albumArtImageView.setImageResource(R.drawable.oldeis);
                    albumImg.setImageResource(R.drawable.oldeis);
                }
                playPauseFlag = true;
                musicService.setmSongs(currentSongIndex + 1);
                musicService.playSong();
                mSeekBar.setProgress(0);
                mSeekBar.setMax(100);
                durationHandler.postDelayed(updateSongTime, 100);
                lastInsertId.put("id", String.valueOf(songList.get(currentSongIndex + 1).getmSongID()));
                currentSongIndex = currentSongIndex + 1;
            } else {

                imgBttnPlayPause.setImageResource(R.drawable.ic_action_pause);
                songPlayPauseBttn.setImageResource(R.drawable.ic_action_pause);
                songArtist.setText(songList.get(0).getmSongArtist());
                songTitle.setText(songList.get(0).getmSongTitle());
                playPauseFlag = true;
                musicService.setmSongs((int) songList.get(0).getmSongID());
                musicService.playSong();
                mSeekBar.setProgress(0);
                mSeekBar.setMax(100);
                durationHandler.postDelayed(updateSongTime, 100);
                lastInsertId.put("id", String.valueOf(songList.get(0).getmSongID()));
                currentSongIndex = 0;
            }
        }


    }


//    private void fetchSongs() {
//        String[] projection = {
//                MediaStore.Audio.Media.TITLE,
//                MediaStore.Audio.Media.ARTIST,
//                MediaStore.Audio.Media.DATA,
//                MediaStore.Audio.Media.DISPLAY_NAME,
//                MediaStore.Audio.Media.DURATION,
//                MediaStore.Audio.Media.ALBUM,
//                MediaStore.Audio.Media.ALBUM_ID
//        };
//        String sortBy = MediaStore.Audio.AudioColumns.TITLE + "COLLATE LOCALIZED AS ASC";
//
//        Cursor cursor = null;
//        try {
//            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//
//            cursor = getContentResolver().query(uri, projection, null, null, sortBy);
//            if (cursor != null) {
//                cursor.moveToFirst();
//                int position = 1;
//                while (!cursor.isAfterLast()) {
//                    songList.add(new Songs(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getLong(4), cursor.getString(5), cursor.getLong(6)));
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//    }
}
