package com.android.musicplayertemplate;

import android.graphics.Bitmap;

/**
 * Created by rjn on 11/14/2016.s
 */

public class Songs {
    private long mSongID;
    private String mSongTitle;
    private String mSongPath;
    private String mSongArtist;
    private long mSongAlbumId;
    private Bitmap albumArt;

    public Songs() {
    }

    public Songs(long mSongID, String mSongTitle, String mSongPath, String mSongArtist) {
        this.mSongID = mSongID;
        this.mSongTitle = mSongTitle;
        this.mSongPath = mSongPath;
        this.mSongArtist = mSongArtist;
    }

    public Songs(long mSongID, String mSongTitle, String mSongPath, String mSongArtist, long mSongAlbumId) {
        this.mSongID = mSongID;
        this.mSongTitle = mSongTitle;
        this.mSongPath = mSongPath;
        this.mSongArtist = mSongArtist;
        this.mSongAlbumId = mSongAlbumId;
    }

    public Songs(long mSongID, String mSongTitle, String mSongPath, String mSongArtist, long mSongAlbumId, Bitmap albumArt) {
        this.mSongID = mSongID;
        this.mSongTitle = mSongTitle;
        this.mSongPath = mSongPath;
        this.mSongArtist = mSongArtist;
        this.mSongAlbumId = mSongAlbumId;
        this.albumArt = albumArt;
    }

    public Bitmap getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(Bitmap albumArt) {
        this.albumArt = albumArt;
    }

    public long getmSongAlbumId() {
        return mSongAlbumId;
    }

    public void setmSongAlbumId(long mSongAlbumId) {
        this.mSongAlbumId = mSongAlbumId;
    }

    public String getmSongArtist() {
        return mSongArtist;
    }

    public void setmSongArtist(String mSongArtist) {
        this.mSongArtist = mSongArtist;
    }

    public String getmSongPath() {
        return mSongPath;
    }

    public void setmSongPath(String mSongPath) {
        this.mSongPath = mSongPath;
    }

    public long getmSongID() {
        return mSongID;
    }

    public void setmSongID(long mSongID) {
        this.mSongID = mSongID;
    }

    public String getmSongTitle() {
        return mSongTitle;
    }

    public void setmSongTitle(String mSongTitle) {
        this.mSongTitle = mSongTitle;
    }
}
