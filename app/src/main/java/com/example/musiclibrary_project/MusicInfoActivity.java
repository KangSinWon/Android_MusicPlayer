package com.example.musiclibrary_project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musiclibrary_project.MusicPlayerService.MusicBinder;

import java.util.concurrent.TimeUnit;

public class MusicInfoActivity extends AppCompatActivity {
    private MusicPlayerService musicService;
    private PlayingMusicInfo pl;
    private boolean isMusicService = false;
    private Intent musicServiceIntent;

    TextView tv_title;
    TextView tv_artist;
    SeekBar seekBar;

    TextView tv_curr_time;
    TextView tv_total_time;

    // MusicPlayerService에서 변경된 정보를 알려줄 때 실행되는 함수
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("title");
            String artist = intent.getStringExtra("artist");
            setInfo(title, artist);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_info);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        String title = bundle.getString("title");
        String artist = bundle.getString("artist");
        boolean isPlaying = bundle.getBoolean("isPlaying");
        int currTime = bundle.getInt("currentTime");

        seekBar = (SeekBar) findViewById(R.id.musicplayer_seekbar);
        seekBar.setProgress(currTime);

        tv_title = (TextView) findViewById(R.id.title);
        tv_artist = (TextView) findViewById(R.id.artist);
        setInfo(title, artist);

        tv_curr_time = (TextView) findViewById(R.id.curr_time);
        tv_total_time = (TextView) findViewById(R.id.total_time);

        ImageButton ib = (ImageButton) findViewById(R.id.button_play);
        if(isPlaying) {
            ib.setImageResource(R.drawable.pause);
        } else {
            ib.setImageResource(R.drawable.play);
        }
    }

    // Service 정보를 가져올 때 연결을 해주는 변수
    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            // 서비스와 연결되었을 때 호출되는 메서드
            MusicBinder mb = (MusicBinder) service;
            musicService = mb.getService();
            MusicPlayerSeekBar();

            isMusicService = true;
        }
        public void onServiceDisconnected(ComponentName name) {
            isMusicService = false;
        }
    };

    // 백그라운드에서 재생되고 있는 MusicPlayerService 정보를 가져옴
    @Override
    protected void onStart(){
        super.onStart();
        if (musicServiceIntent == null) {
            musicServiceIntent = new Intent(this, MusicPlayerService.class);
            bindService(musicServiceIntent, conn, Context.BIND_AUTO_CREATE);
            // TODO : 'unbindService' method 구현
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        // MusicPlayerService에서 바뀐 정보를 알려줄 때 정보를 받을 수 있도록 하는 함수
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("setInfo"));
    }


    @Override
    protected void onPause(){
        super.onPause();
        // 연결 해제
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

        // 연결 해제
        unbindService(conn);
        isMusicService = false;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        if(isMusicService)
            unbindService(conn);
    }

    // 노래가 재생되는 동안 진행률을 보여주는 seekbar
    public void MusicPlayerSeekBar(){
        seekBar.setMax(musicService.getMusicTime());
        tv_total_time.setText(convertMillisSeconds(musicService.getMusicTime()));
        seekBar.setProgress(musicService.getCurrentPosition());
        tv_curr_time.setText(convertMillisSeconds(musicService.getCurrentPosition()));

        // 노래가 재생되면 Thread로 1초마다 진행률이 증가하도록 함
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(musicService.isPlaying()){
                    try{
                        Thread.sleep(1000);
                        seekBar.setProgress(musicService.getCurrentPosition());
                        tv_curr_time.setText(convertMillisSeconds(musicService.getCurrentPosition()));
                    }catch (Exception e){}
                }
            }
        }).start();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // seekbar를 임의로 움직였을 때 해당 시간으로 노래를 이동시켜 재생
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    musicService.seekTo(i);
                    seekBar.setProgress(musicService.getCurrentPosition());
                    tv_curr_time.setText(convertMillisSeconds(musicService.getCurrentPosition()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    // 밀리세컨드를 분, 초로 변경해주는 함수
    public String convertMillisSeconds(int millis){
        String time = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        return time;
    }

    // 현재 노래 정보를 화면에 보여주는 함수 : 파라미터 PlayingMusicInfo
    public void setInfo(PlayingMusicInfo pl){
        if (pl.getTitle() == null || pl.getTitle().equals(""))
            tv_title.setText("?");
        else
            tv_title.setText(pl.getTitle());

        if (pl.getArtist() == null || pl.getArtist().equals(""))
            tv_artist.setText("?");
        else
            tv_artist.setText(pl.getArtist());
    }

    // 현재 노래 정보를 화면에 보여주는 함수 : 파라미터 String, String
    public void setInfo(String title, String artist){
        if(title == null || title.equals(""))
            tv_title.setText("?");
        else
            tv_title.setText(title);

        if(artist == null || artist.equals(""))
            tv_artist.setText("?");
        else
            tv_artist.setText(artist);
    }

    // play, pause 버튼 함수
    public void play_button(View view) {
        ImageButton ib = (ImageButton) view;
        if(musicService.isPlaying()){
            ib.setImageResource(R.drawable.play);
            musicService.pause();
        } else {
            ib.setImageResource(R.drawable.pause);
            musicService.continue_play();
            MusicPlayerSeekBar();
        }
    }

    // 이전 곡 재생 함수
    public void previous_button(View view) {
        musicService.previous();
        setInfo(musicService.getCurrMusicInfo());
    }

    // 다음 곡 재생 함수
    public void next_button(View view) {
        musicService.next();
        setInfo(musicService.getCurrMusicInfo());
    }
}
