package com.example.musiclibrary_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import com.example.musiclibrary_project.MusicPlayerService.MusicBinder;

public class MainActivity extends AppCompatActivity {
    private boolean isMusicService = false;
    private Intent musicServiceIntent;
    private MusicPlayerService musicService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Start 버튼을 누르면 music list 화면으로 넘어감
        final Intent music_library = new Intent(this, MusicLibraryActivity.class);
        Button button_start = findViewById(R.id.button_start);
        button_start.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                startActivity(music_library);
            }
        });
    }

    // Service 정보를 가져올 때 연결을 해주는 변수
    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            MusicBinder mb = (MusicBinder) service;
            musicService = mb.getService();

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
    protected void onDestroy(){
        super.onDestroy();
        unbindService(conn);
    }
}
