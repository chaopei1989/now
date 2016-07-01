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
public class LikeDislike {

    public MusicEntity music;
    public SocialUser user;
    public int type;
    public static final IListParser<LikeDislike> LIST_PARSER = new IListParser<LikeDislike>() {
        @Override
        public List<LikeDislike> parseList(String json) {
            Gson gson = new Gson();
            return gson.fromJson(json,
                    new TypeToken<List<LikeDislike>>() {
                    }.getType());
        }
    };
}
