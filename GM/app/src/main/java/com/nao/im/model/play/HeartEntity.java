package com.nao.im.model.play;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nao.im.model.MusicEntity;
import com.nao.im.model.login.SocialUser;
import com.nao.im.net.parser.IListParser;

import java.util.List;

/**
 * Created by chaopei on 2015/10/16.
 */
public class HeartEntity {
    public final static int OVER = -1, PREPARED = 0, PLAYING = 1;

    public MusicEntity music;
    public SocialUser currUser;
    public int state;
    public int userCount;
    public int musicCount;

    public static final IListParser<HeartEntity> LIST_PARSER = new IListParser<HeartEntity>() {
        @Override
        public List<HeartEntity> parseList(String json) {
            Gson gson = new Gson();
            return gson.fromJson(json,
                    new TypeToken<List<HeartEntity>>() {
                    }.getType());
        }
    };
}
