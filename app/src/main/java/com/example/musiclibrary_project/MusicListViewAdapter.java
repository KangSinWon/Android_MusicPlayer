package com.example.musiclibrary_project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

// ListView에 들어가는 item을 custom하기 위해 별도로 만든 Adapter 클래스
public class MusicListViewAdapter extends BaseAdapter {
    private ArrayList<PlayingMusicInfo> musicList;

    public MusicListViewAdapter(ArrayList list) {
        this.musicList = list;
    }

    @Override
    public int getCount() {
        return musicList.size();
    }

    @Override
    public Object getItem(int i) {
        return musicList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        // 노래 리스트 중 현재 만들 item의 index
        final int pos = position;
        final Context context = viewGroup.getContext();

        // 정의해둔 listview의 item view를 가져
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_music_item, viewGroup, false);
        }

        // item view에 들어있는 image와 text를 가져옴
        ImageView listview_item_image = (ImageView) view.findViewById(R.id.listview_item_image) ;
        TextView listview_item_text = (TextView) view.findViewById(R.id.listview_item_text) ;

        // 노래 리스트의 정보들을 가져옴
        PlayingMusicInfo pl = musicList.get(position);

        // item의 이미지와 text 설정
        listview_item_image.setImageResource(R.drawable.cover);
        listview_item_text.setText(pl.getTitle());

        return view;
    }
}
