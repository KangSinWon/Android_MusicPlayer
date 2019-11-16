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
        Log.i("println", "MusicInfo : into onCreate");

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


    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            // 서비스와 연결되었을 때 호출되는 메서드
            // 서비스 객체를 전역변수로 저장
            MusicBinder mb = (MusicBinder) service;
            musicService = mb.getService(); // 서비스가 제공하는 메소드 호출하여
            MusicPlayerSeekBar();

            // 서비스쪽 객체를 전달받을수 있슴
            isMusicService = true;
        }
        public void onServiceDisconnected(ComponentName name) {
            // 서비스와 연결이 끊겼을 때 호출되는 메서드
            isMusicService = false;
        }
    };

    @Override
    protected void onStart(){
        Log.i("println", "MusicInfo : into onStart");
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
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("setInfo"));
    }


    @Override
    protected void onPause(){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

        unbindService(conn);
        isMusicService = false;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        if(isMusicService)
            unbindService(conn);
    }

    public void MusicPlayerSeekBar(){
        seekBar.setMax(musicService.getMusicTime());
        tv_total_time.setText(convertMillisSeconds(musicService.getMusicTime()));
        seekBar.setProgress(musicService.getCurrentPosition());
        tv_curr_time.setText(convertMillisSeconds(musicService.getCurrentPosition()));

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

    public String convertMillisSeconds(int millis){
        String time = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
        return time;
    }

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

    public void play_button(View view) {
        ImageButton ib = (ImageButton) view;
        if(musicService.isPlaying()){
            ib.setImageResource(R.drawable.play);
            musicService.pause();
        } else {
            ib.setImageResource(R.drawable.pause);
            musicService.continue_play();
        }
    }

    public void previous_button(View view) {
        musicService.previous();
        setInfo(musicService.getCurrMusicInfo());
    }

    public void next_button(View view) {
        musicService.next();
        setInfo(musicService.getCurrMusicInfo());
    }
}
