package com.nao.im.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nao.im.net.parser.IListParser;

import java.util.List;

public class MusicEntity implements Parcelable {

    private int musicId;
    private String album;
    private String url;
    private String title;
    private String cover;
    /**
     * 时长（毫秒）
     */
    private long duration;
    private String artist;

    private int like;
    private int dislike;
    private int request;

    public int getMusicId() {
        return musicId;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public void setMusicId(int musicId) {
        this.musicId = musicId;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getArtist() {
        return artist;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public int getLike() {
        return like;
    }

    public void setDislike(int dislike) {
        this.dislike = dislike;
    }

    public int getDislike() {
        return dislike;
    }

    public void setRequest(int request) {
        this.request = request;
    }

    public int getRequest() {
        return request;
    }

    @Override
    public String toString() {
        long duration = this.duration / 1000;
        StringBuffer sb = new StringBuffer();
        sb      .append("title:").append(title)
                .append("\nartist:").append(artist)
                .append("\nalbum:").append(album)
                .append("\ncover:").append(cover)
                .append("\nduration:").append(duration/60).append("m").append(duration % 60).append("s")
                .append("\nurl:").append(url);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if(null != o && o instanceof MusicEntity) {
            MusicEntity m = ((MusicEntity) o);
            if(this.musicId != m.musicId) {
                return false;
            }
            if(!this.album.equals(m.album)) {
                return false;
            }
            if(!this.url.equals(m.url)) {
                return false;
            }
            if(!this.title.equals(m.title)) {
                return false;
            }
            if(!this.artist.equals(m.artist)) {
                return false;
            }
            if(this.duration != m.duration) {
                return false;
            }
            if(!this.cover.equals(m.cover)) {
                return false;
            }
            return true;
        }
        return false;
    }

    public MusicEntity() {
        //todo
    }
    public MusicEntity(String url, String title, String album, String cover, String artist) {
        this.url = url;
        this.title = title;
        this.album = album;
        this.cover = cover;
        this.artist = artist;
    }

    public MusicEntity(Parcel source) {
        musicId = source.readInt();
        album = source.readString();
        url = source.readString();
        title = source.readString();
        cover = source.readString();
        duration = source.readLong();
        artist = source.readString();
        like = source.readInt();
        dislike = source.readInt();
        request = source.readInt();
    }

    public static final IListParser<MusicEntity> LIST_PARSER = new IListParser<MusicEntity>() {
        @Override
        public List<MusicEntity> parseList(String json) {
            Gson gson = new Gson();
            return gson.fromJson(json,
                    new TypeToken<List<MusicEntity>>(){}.getType());
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MusicEntity> CREATOR = new Creator<MusicEntity>() {
        @Override
        public MusicEntity createFromParcel(Parcel source) {
            return new MusicEntity(source);
        }

        @Override
        public MusicEntity[] newArray(int size) {
            return new MusicEntity[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(musicId);
        dest.writeString(album);
        dest.writeString(url);
        dest.writeString(title);
        dest.writeString(cover);
        dest.writeLong(duration);
        dest.writeString(artist);
        dest.writeInt(like);
        dest.writeInt(dislike);
        dest.writeInt(request);
    }

}
