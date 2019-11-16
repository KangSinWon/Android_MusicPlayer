package com.example.musiclibrary_project;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;

public class MusicPlayerService extends Service implements MediaPlayer.OnCompletionListener {
    private MediaPlayer mp;
    private ArrayList<PlayingMusicInfo> list;
    private PlayingMusicInfo currMusicInfo = new PlayingMusicInfo();

    IBinder mBinder = new MusicBinder();

    @Override
    public void onCreate(){
        super.onCreate();

        mp = new MediaPlayer();
        mp.setOnCompletionListener(this);
    }

    public void setMusicList(ArrayList<PlayingMusicInfo> list){
        this.list = list;
    }

    public ArrayList<PlayingMusicInfo> getMusicList(){
        return list;
    }

    public PlayingMusicInfo getCurrMusicInfo(){
        return currMusicInfo;
    }

    public void setCurrMusic(PlayingMusicInfo pl){
        currMusicInfo = pl;
    }

    public void play(){
        mp.reset();

        String p = currMusicInfo.getPath();
        Uri uri = Uri.parse(p);

        try{
            mp.setDataSource(getApplicationContext(), uri);
            mp.prepare();
        } catch (Exception e){}

        mp.start();
    }

    public void continue_play(){
        mp.start();
    }

    public void pause(){
        mp.pause();
    }

    public boolean isPlaying() {
        return mp.isPlaying();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return super.onStartCommand(intent, flags, startId);
    }

    public void previous() {
        int pre = currMusicInfo.getNum() - 1;
        if (pre < 0)
            pre = list.size() - 1;

        currMusicInfo = list.get(pre);
        play();
    }

    public void next() {
        int next = currMusicInfo.getNum() + 1;
        if (next > list.size() - 1)
            next = 0;

        currMusicInfo = list.get(next);
        sendSetInfo();
        play();
    }

    private void sendSetInfo(){
        Intent intent = new Intent("setInfo");
        intent.putExtra("title", currMusicInfo.getTitle());
        intent.putExtra("artist", currMusicInfo.getArtist());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public int getCurrentPosition(){
        return mp.getCurrentPosition();
    }

    public int getMusicTime(){
        return mp.getDuration();
    }

    public void seekTo(int time){
        mp.seekTo(time);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.reset();
        next();
    }

    public class MusicBinder extends Binder {
        MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        mp.stop();
        mp.release();
        return false;
    }
}
