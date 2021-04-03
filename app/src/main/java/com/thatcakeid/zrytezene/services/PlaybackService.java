package com.thatcakeid.zrytezene.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.IOException;

public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {
    private MediaPlayer mplayer;
    private AudioManager audioManager;
    private String mediaSource;

    private final IBinder iBinder = new LocalBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            mediaSource = intent.getExtras().getString("source");
        } catch (NullPointerException e) {
            stopSelf();
        }

        if (!requestAudioFocus()) {
            stopSelf();
        }

        initPlayer();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mplayer != null) {
            stopMedia();
            mplayer.release();
        }
        removeAudioFocus();
    }

    private void initPlayer() {
        mplayer = new MediaPlayer();
        mplayer.setOnPreparedListener(this);
        mplayer.setOnBufferingUpdateListener(this);
        mplayer.setOnCompletionListener(this);
        mplayer.setOnErrorListener(this);
        mplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mplayer.setDataSource(mediaSource);
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }

        mplayer.prepareAsync();
    }

    private void playMedia() {
        if (!mplayer.isPlaying()) {
            mplayer.start();
        }
    }

    private void stopMedia() {
        if (mplayer == null) return;
        mplayer.stop();
    }

    private void pauseMedia() {
        if (mplayer.isPlaying()) {
            mplayer.pause();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopSelf();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playMedia();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mplayer == null) initPlayer();
                else if (!mplayer.isPlaying()) mplayer.start();
                mplayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                stopMedia();
                mplayer.release();
                mplayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pauseMedia();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mplayer.isPlaying()) mplayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    public class LocalBinder extends Binder {
        public PlaybackService getService() {
            return PlaybackService.this;
        }
    }
}
