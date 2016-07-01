package com.nao.im.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nao.im.net.parser.IListParser;

import java.util.List;

/**
 * Created by chaopei on 2015/10/16.
 */
public class SameMusic {
    public String album_art_url;
    public String album_name;
    public String artist_name;
    public int id;
    public String name;
    public String source_url;
    public static final IListParser<SameMusic> LIST_PARSER = new IListParser<SameMusic>() {
        @Override
        public List<SameMusic> parseList(String json) {
            Gson gson = new Gson();
            return gson.fromJson(json,
                    new TypeToken<List<SameMusic>>() {
                    }.getType());
        }
    };
}
