package com.example.musiclibrary_project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import com.example.musiclibrary_project.MusicPlayerService.MusicBinder;

public class MusicLibraryActivity extends AppCompatActivity {
    private ArrayList<PlayingMusicInfo> list;
    private Intent musicServiceIntent;

    private MusicPlayerService musicService;
    private boolean isMusicService = false;

    private ListView listview;
    private MusicListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_library);

        list = getExternalMusicList();

        // custom item - adapter 생성
        adapter = new MusicListViewAdapter(list);
        listview = (ListView) findViewById(R.id.listview_music);

        // item 중간에 선을 넣기 위한 함수
        listview.setDivider(new ColorDrawable(0xffffffff));
        listview.setDividerHeight(2);

        listview.setAdapter(adapter);

        final Intent music_info = new Intent(this, MusicInfoActivity.class);
        // listview item 클릭시 이벤트 발생
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v,int position, long id){
                PlayingMusicInfo pl = (PlayingMusicInfo) parent.getItemAtPosition(position);
                // item 클릭시 클릭한 노래 재생
                if(!pl.getTitle().equals(musicService.getCurrMusicInfo().getTitle())) {
                    musicService.setCurrMusic(pl);
                    musicService.play();
                }

                // 노래 재생화면으로 넘어감
                music_info.putExtra("title", musicService.getCurrMusicInfo().getTitle());
                music_info.putExtra("artist", musicService.getCurrMusicInfo().getAlbum());
                music_info.putExtra("isPlaying", musicService.isPlaying());
                music_info.putExtra("currentTime", musicService.getCurrentPosition());
                startActivity(music_info);
            }
        });
    }

    // Service 정보를 가져올 때 연결을 해주는 변수
    private ServiceConnection serviceConn = new ServiceConnection() {
        // Service 연결
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            MusicBinder mb = (MusicBinder) service;
            musicService = mb.getService();
            musicService.setMusicList(list);
            isMusicService = true;

        }
        // Service 해
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
            isMusicService = false;
        }
    };

    // 백그라운드에서 재생되고 있는 MusicPlayerService 정보를 가져옴
    @Override
    protected void onStart(){
        super.onStart();
        if (musicServiceIntent == null) {
            musicServiceIntent = new Intent(this, MusicPlayerService.class);
            bindService(musicServiceIntent, serviceConn, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        // Service 연결 해제
        if(isMusicService)
            unbindService(serviceConn);
    }

    // External Stroage에 있는 노래들을 가져와 list에 정보들을 저장
    public ArrayList<PlayingMusicInfo> getExternalMusicList(){
        ArrayList<PlayingMusicInfo> music_list = new ArrayList();
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if (PermissionHandler.isPermissionGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, "External Storage", 1000)) {

                // External Stroage에 있는 music 폴더에 접근
                String rootSD = Environment.getExternalStorageDirectory().toString();
                File file = new File(rootSD + "/Music");

                try {
                    // 현재 폴더에 있는 파일들을 가져옴
                    File list[] = file.listFiles();
                    for (int i = 0; i < list.length; i++) {
                        Uri uri = Uri.parse(list[i].getPath());
                        MediaMetadataRetriever mmdata = new MediaMetadataRetriever();
                        mmdata.setDataSource(getApplication(), uri);

                        // 각 노래들의 정보들을 list에 저장
                        String title = mmdata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        String album = mmdata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                        String artist = mmdata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                        String genre = mmdata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
                        PlayingMusicInfo pl = new PlayingMusicInfo(i, list[i].getPath(), title, album, artist, genre);
                        music_list.add(pl);
                    }
                } catch (Exception e) {
                }
            }
        }
        return music_list;
    }
}
