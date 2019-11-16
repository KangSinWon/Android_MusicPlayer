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

// 뮤직 플레이어를 백그라운드로 실행하기 위한 Service
public class MusicPlayerService extends Service implements MediaPlayer.OnCompletionListener {
    private MediaPlayer mp;
    private ArrayList<PlayingMusicInfo> list;
    private PlayingMusicInfo currMusicInfo = new PlayingMusicInfo();

    IBinder mBinder = new MusicBinder();

    @Override
    public void onCreate(){
        super.onCreate();

        mp = new MediaPlayer();
        // 노래가 종료되었을때 이벤트를 받기위한 설정
        mp.setOnCompletionListener(this);
    }

    // 노래 리스트 저장
    public void setMusicList(ArrayList<PlayingMusicInfo> list){
        this.list = list;
    }

    // 노래 리스트 반환
    public ArrayList<PlayingMusicInfo> getMusicList(){
        return list;
    }

    // 현재 재생되고 있는 노래 정보 반환
    public PlayingMusicInfo getCurrMusicInfo(){
        return currMusicInfo;
    }

    // 재생되고 있는 노래 정보 저장
    public void setCurrMusic(PlayingMusicInfo pl){
        currMusicInfo = pl;
    }

    // 새로운 노래를 실행할 때 실행되는 함수
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

    // 노래가 정지되었다가 다시 실행될때 실행되는 함수
    public void continue_play(){
        mp.start();
    }

    // 노래를 정지하는 함수
    public void pause(){
        mp.pause();
    }

    // 현재 노래가 실행되고 있는 알려주는 함수
    public boolean isPlaying() {
        return mp.isPlaying();
    }

    // 전 곡을 실행하기 위한 함수
    public void previous() {
        int pre = currMusicInfo.getNum() - 1;
        if (pre < 0)
            pre = list.size() - 1;

        currMusicInfo = list.get(pre);
        play();
    }

    // 다음 곡을 실행하기 위한 함수
    public void next() {
        int next = currMusicInfo.getNum() + 1;
        if (next > list.size() - 1)
            next = 0;

        currMusicInfo = list.get(next);
        sendSetInfo();
        play();
    }

    // 노래 정보가 바뀌었을 때 MusicInfoActivity에 변경된 정보를 알려주는 함수
    private void sendSetInfo(){
        Intent intent = new Intent("setInfo");
        intent.putExtra("title", currMusicInfo.getTitle());
        intent.putExtra("artist", currMusicInfo.getArtist());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // 현재 노래가 실행되고 있는 시간을 return
    public int getCurrentPosition(){
        return mp.getCurrentPosition();
    }

    // 현재 노래의 총 시간을 return
    public int getMusicTime(){
        return mp.getDuration();
    }

    // 원하는 구간으로 옮기기 위한 함수
    public void seekTo(int time){
        mp.seekTo(time);
    }

    // 노래가 종류 되었을 때 다음 노래로 넘어가기 위한 함수
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.reset();
        next();
    }

    // Service를 바인딩하기 위한 클래스와 함수
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
