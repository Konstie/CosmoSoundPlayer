package com.cosmosound.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

public class CosmoSoundService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private final String LOG_TAG = "Cosmosound Service";

    private final IBinder tracksBind = new CosmoSoundMusicBinder();

    private static final int NOTF_ID = 1;

    private CosmoSoundPlaylistFragment fragment;

    private MediaPlayer csMediaPlayer;
    private ArrayList<MusicTrack> tracks;
    private int trackPosition;

    private String trackTitle = "";
    private String trackAuthor = "";

    @Override
    public void onCreate() {
        super.onCreate();
        trackPosition = 0;
        csMediaPlayer = new MediaPlayer();
        initMediaPlayer();
    }

    public void initMediaPlayer() {
        csMediaPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        csMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        csMediaPlayer.setOnPreparedListener(this);
        csMediaPlayer.setOnCompletionListener(this);
        csMediaPlayer.setOnErrorListener(this);
    }

    public void setTrackList(ArrayList<MusicTrack> tracks) {
        this.tracks = tracks;
    }

    public class CosmoSoundMusicBinder extends Binder {
        CosmoSoundService getService() {
            return CosmoSoundService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return tracksBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        csMediaPlayer.stop();
        csMediaPlayer.release();
        return false;
    }

    public void playTrack() {
        csMediaPlayer.reset();
        MusicTrack currentPLaying = tracks.get(trackPosition);
        trackTitle = currentPLaying.getTrackTitle();
        trackAuthor = currentPLaying.getTrackAuthor();
        long currentSongID = currentPLaying.getTrackID();
        Uri trackURI = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSongID
        );

        try {
            csMediaPlayer.setDataSource(getApplicationContext(), trackURI);
        } catch (IOException exc) {
            Log.e(LOG_TAG, "Some problems with data source occured...", exc);
        }

        csMediaPlayer.prepareAsync();
    }

    public void setTrackPosition(int trackPosition) {
        this.trackPosition = trackPosition;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (csMediaPlayer.getCurrentPosition() > 0 && tracks.size() > 0) {
            csMediaPlayer.reset();
            playNext();
        } else {
            csMediaPlayer.stop();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        csMediaPlayer.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();

        Intent backHandlerIntent = new Intent(this, CosmoSoundPlaylistActivity.class);
        backHandlerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, backHandlerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notBuilder = new Notification.Builder(this);

        notBuilder.setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(trackTitle)
                .setOngoing(true)
                .setContentTitle(trackAuthor)
                .setContentText(trackTitle);
        Notification currentPlayingNot = notBuilder.build();

        startForeground(NOTF_ID, currentPlayingNot);
    }


    public void playPause() {
        if (csMediaPlayer.isPlaying()) {
            pauseTrack();
        } else {
            trackPosition--;
            launch();
        }
    }

    public void playPrevious() {
        trackPosition--;
        if (trackPosition < 0) {
            trackPosition = tracks.size() - 1;
            playTrack();
        }
    }

    public void playNext() { // запускается в OnCompletionListener
        ++trackPosition;
        String currentTrackInfo = "";
        if (trackPosition < tracks.size()) {
             currentTrackInfo += tracks.get(trackPosition).getTrackAuthor() + " — " +
                    tracks.get(trackPosition).getTrackTitle() + " — " +
                    tracks.get(trackPosition).getTrackAlbum();

        }
        if (trackPosition >= tracks.size() && tracks.size() > 0) {
            setTrackPosition(0);
            currentTrackInfo = "" + tracks.get(trackPosition).getTrackAuthor() + " — " +
                    tracks.get(trackPosition).getTrackTitle() + " — " +
                    tracks.get(trackPosition).getTrackAlbum();
            playTrack();
            try {
                Thread.sleep(100); // подождать, пока прогрузятся данные о продолжительности трека
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            CosmoSoundPlaylistFragment.currentTrackDuration = csMediaPlayer.getDuration();
            CosmoSoundPlaylistFragment.currentTrackInfo = currentTrackInfo;
        } else {
            playTrack();
            try {
                Thread.sleep(100); // подождать, пока прогрузятся данные о продолжительности трека
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            CosmoSoundPlaylistFragment.currentTrackDuration = csMediaPlayer.getDuration();
            CosmoSoundPlaylistFragment.currentTrackInfo = currentTrackInfo;
        }
    }

    public void stopTrack() {
        if (csMediaPlayer.isPlaying()) {
            csMediaPlayer.stop();
        }
    }

    public int getTrackPosition() {
        return trackPosition;
    }

    public int getTrackCurrentPosition() {
        return csMediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return csMediaPlayer.getDuration();
    }

    public boolean isPlaying() {
        return csMediaPlayer.isPlaying();
    }

    public void pauseTrack() {
        csMediaPlayer.pause();
    }

    public void seek(int trackPosition) {
        csMediaPlayer.seekTo(trackPosition);
    }

    public void launch() {
        csMediaPlayer.start();
    }

    @Override
    public void onDestroy() {
        csMediaPlayer.release();
        stopForeground(true);
    }
}
