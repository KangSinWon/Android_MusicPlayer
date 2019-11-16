package com.example.musiclibrary_project;

public class PlayingMusicInfo {
    private int num;
    private String path;
    private String title;
    private String artist;
    private String album;
    private String genre;

    public PlayingMusicInfo(){
        this.path = "";
        this.title = "";
        this.artist = "";
        this.album = "";
        this.genre = "";
    }

    public PlayingMusicInfo(int num, String path, String title, String artist, String album, String genre){
        this.num = num;
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
    }

    public int getNum(){
        return this.num;
    }

    public String getPath(){
        return this.path;
    }

    public String getTitle(){
        return this.title;
    }

    public String getArtist(){ return this.artist; }

    public String getAlbum(){
        return this.album;
    }

    public String getGenre(){
        return this.genre;
    }

    public String toString(){
        String str = "";
        str += "path : " + path + "\n";
        str += "title : " + title + "\n";
        str += "artist : " + artist + "\n";
        str += "album : " + album + "\n";
        str += "genre : " + genre;
        return str;
    }
}
