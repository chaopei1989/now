package com.nao.im.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nao.im.net.parser.IListParser;

import java.util.List;

/**
 * Created by chaopei on 2015/10/5.
 */
public class Channel implements Parcelable {
    private int channelId;
    private int type;
    private String channelName;
    private int playingIndex;

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public int getPlayingIndex() {
        return playingIndex;
    }

    public void setPlayingIndex(int playingIndex) {
        this.playingIndex = playingIndex;
    }

    public Channel() {
        //todo
    }
    public Channel(Parcel source) {
        channelId = source.readInt();
        type = source.readInt();
        channelName = source.readString();
        playingIndex = source.readInt();
    }

    public static final IListParser<Channel> LIST_PARSER = new IListParser<Channel>() {
        @Override
        public List<Channel> parseList(String json) {
            Gson gson = new Gson();
            return gson.fromJson(json,
                    new TypeToken<List<Channel>>(){}.getType());
        }
    };

    public static final Parcelable.Creator<Channel> CREATOR = new Parcelable.Creator<Channel>() {
        @Override
        public Channel createFromParcel(Parcel source) {
            return new Channel(source);
        }

        @Override
        public Channel[] newArray(int size) {
            return new Channel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(channelId);
        dest.writeInt(type);
        dest.writeString(channelName);
        dest.writeInt(playingIndex);
    }
}
