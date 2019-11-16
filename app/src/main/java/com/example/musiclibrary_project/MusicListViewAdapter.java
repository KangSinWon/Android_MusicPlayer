package com.example.musiclibrary_project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

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
        final int pos = position;
        final Context context = viewGroup.getContext();

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_music_item, viewGroup, false);
        }

        ImageView listview_item_image = (ImageView) view.findViewById(R.id.listview_item_image) ;
        TextView listview_item_text = (TextView) view.findViewById(R.id.listview_item_text) ;

        PlayingMusicInfo pl = musicList.get(position);

        listview_item_image.setImageResource(R.drawable.icon);
        listview_item_text.setText(pl.getTitle());

        return view;
    }
}
