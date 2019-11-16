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

        adapter = new MusicListViewAdapter(list);
        listview = (ListView) findViewById(R.id.listview_music);

        listview.setDivider(new ColorDrawable(0xffffffff));
        listview.setDividerHeight(2);
        listview.setAdapter(adapter);


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();

            }
        });

        final Intent music_info = new Intent(this, MusicInfoActivity.class);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v,int position, long id){
                PlayingMusicInfo pl = (PlayingMusicInfo) parent.getItemAtPosition(position);
                if(!pl.getTitle().equals(musicService.getCurrMusicInfo().getTitle())) {
                    musicService.setCurrMusic(pl);
                    musicService.play();
                }
                music_info.putExtra("title", musicService.getCurrMusicInfo().getTitle());
                music_info.putExtra("artist", musicService.getCurrMusicInfo().getArtist());
                music_info.putExtra("isPlaying", musicService.isPlaying());
                music_info.putExtra("currentTime", musicService.getCurrentPosition());
                startActivity(music_info);
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
            musicService.setMusicList(list);
            // 서비스쪽 객체를 전달받을수 있슴
            isMusicService = true;
        }
        public void onServiceDisconnected(ComponentName name) {
            // 서비스와 연결이 끊겼을 때 호출되는 메서드
            musicService = null;
            isMusicService = false;
        }
    };

    @Override
    protected void onStart(){
        super.onStart();
        if (musicServiceIntent == null) {
            musicServiceIntent = new Intent(this, MusicPlayerService.class);
            bindService(musicServiceIntent, conn, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.i("println", "MusicLibrary : into onDestory");

        if(isMusicService)
            unbindService(conn);
    }

    public ArrayList<PlayingMusicInfo> getExternalMusicList(){
        ArrayList<PlayingMusicInfo> music_list = new ArrayList();
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if (PermissionHandler.isPermissionGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, "External Storage", 1000)) {

                String rootSD = Environment.getExternalStorageDirectory().toString();
                File file = new File(rootSD + "/Music");

                try {
                    // File file = new File(path);
                    File list[] = file.listFiles();
                    for (int i = 0; i < list.length; i++) {
                        Uri uri = Uri.parse(list[i].getPath());
                        MediaMetadataRetriever mmdata = new MediaMetadataRetriever();
                        mmdata.setDataSource(getApplication(), uri);

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

    /*
    public ArrayAdapter<String> getArrayAdapter(ArrayList list){
        final ArrayAdapter<String> adapter=new ArrayAdapter<String> (
                this, android.R.layout.simple_list_item_1, list){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view =super.getView(position, convertView, parent);

                TextView textView=(TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE);
                textView.setTypeface(textView.getTypeface(), Typeface.BOLD);

                return view;
            }
        };
        return adapter;
    }
     */
}
