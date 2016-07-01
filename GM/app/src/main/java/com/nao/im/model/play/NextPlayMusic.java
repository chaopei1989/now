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
public class NextPlayMusic {

    public MusicEntity curr;
    public SocialUser currUser;

    public static final IListParser<NextPlayMusic> LIST_PARSER = new IListParser<NextPlayMusic>() {
        @Override
        public List<NextPlayMusic> parseList(String json) {
            Gson gson = new Gson();
            return gson.fromJson(json,
                    new TypeToken<List<NextPlayMusic>>() {
                    }.getType());
        }
    };
}
