package com.nao.im.net;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.nao.im.App;
import com.nao.im.GMEnv;
import com.nao.im.model.SameMusic;
import com.nao.im.net.data.RequestManager;
import com.nao.im.net.parser.IReplyListener;
import com.nao.im.net.parser.ReplyParser;
import com.nao.im.net.parser.ReplyWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by chaopei on 2015/10/9.
 * same音乐api
 */
public class SameApi {

    private final static boolean DEBUG = GMEnv.DEBUG;

    private final static String TAG = DEBUG ? "SameApi" : SameApi.class.getSimpleName();

    public interface IRequestResponse {
        void onResponse(List<SameMusic> results);

        void onError(int code, String msg);
    }

    private static void doSearch(String BASE_URL, String query, int limit, int offset, final IRequestResponse requestResponse, Response.ErrorListener volleyErrorListener, Object tag) throws UnsupportedEncodingException {
        final int method = Request.Method.GET;
        query = URLEncoder.encode(query, "utf-8");
        String url = String.format(BASE_URL, query, limit, offset);
        if (DEBUG) {
            Log.d(TAG, "[doSearch] : url=" + url);
        }
        RequestManager.addRequest(new StringRequest(method, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    response = new String(response.getBytes("iso-8859-1"), "utf-8");
                    if (DEBUG) {
                        Log.d(TAG, "[onResponse] : response=" + response);
                    }
                    JSONObject jo = null;
                    jo = new JSONObject(response);
                    int code = jo.getInt("code");
                    if (0 != code) {
                        requestResponse.onError(code, "");
                    } else {
                        JSONObject data = jo.getJSONObject("data");
                        JSONArray results = data.getJSONArray("results");
                        List<SameMusic> list = SameMusic.LIST_PARSER.parseList(results.toString());
                        requestResponse.onResponse(list);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    requestResponse.onError(ReplyParser.Constants.STATUS_JSON_ERROR, e.getMessage());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    requestResponse.onError(ReplyParser.Constants.STATUS_JSON_ERROR, e.getMessage());
                }
            }
        }, volleyErrorListener), tag);
    }

    public static void requestMusicSearch(final String query, final int limit, final int offset, final IRequestResponse requestResponse, final Response.ErrorListener volleyErrorListener, final Object tag) {
        if (DEBUG) {
            Log.d(TAG, "[requestMusicSearch]");
        }
        GMDataCenter.getApi(App.getContext(), new IReplyListener<String>() {
            @Override
            public void onReplyParserResponse(List<String> response) {
                if (0 < response.size()) {
                    String BASE_URL = response.get(0);
                    try {
                        doSearch(BASE_URL, query, limit, offset, requestResponse, volleyErrorListener, tag);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        requestResponse.onError(ReplyParser.Constants.STATUS_URL_ERROR, e.getMessage());
                    }
                } else {
                    requestResponse.onError(ReplyParser.Constants.STATUS_URL_ERROR, "base_url is empty.");
                }
            }

            @Override
            public void onReplyParserError(ReplyWrapper<String> wrapper) {
                requestResponse.onError(wrapper.getStatus(), wrapper.getMsg());
            }
        }, volleyErrorListener);
    }

}
