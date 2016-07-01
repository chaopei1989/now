package com.nao.im.model.play;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nao.im.model.Channel;
import com.nao.im.model.MusicEntity;
import com.nao.im.model.login.SocialUser;
import com.nao.im.net.parser.IListParser;

import java.util.List;

/**
 * Created by chaopei on 2015/10/16.
 */
public class JoinChannel {
    public Channel channel;
    public MusicEntity curr;
    public SocialUser currUser;
    public int state;

    public static final IListParser<JoinChannel> LIST_PARSER = new IListParser<JoinChannel>() {
        @Override
        public List<JoinChannel> parseList(String json) {
            Gson gson = new Gson();
            return gson.fromJson(json,
                    new TypeToken<List<JoinChannel>>() {
                    }.getType());
        }
    };
}
