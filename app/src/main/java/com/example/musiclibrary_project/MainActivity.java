package com.example.musiclibrary_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
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

        // final String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath();

        final Intent music_library = new Intent(this, MusicLibraryActivity.class);
        Button button_start = findViewById(R.id.button_start);
        button_start.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                startActivity(music_library);
            }
        });
    }

    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            // 서비스와 연결되었을 때 호출되는 메서드
            // 서비스 객체를 전역변수로 저장
            MusicBinder mb = (MusicBinder) service;
            musicService = mb.getService(); // 서비스가 제공하는 메소드 호출하여

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
