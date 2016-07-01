package com.nao.im.net;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nao.im.GMEnv;
import com.nao.im.model.login.SocialUser;
import com.nao.im.net.data.ApiParams;
import com.nao.im.net.data.RequestManager;
import com.nao.im.net.login.SocialUserManager;
import com.nao.im.net.parser.IListParser;
import com.nao.im.net.parser.IReplyListener;
import com.nao.im.net.parser.ReplyWrapper;

import java.util.List;
import java.util.Map;

/**
 * Created by chaopei on 2015/8/26.
 * 业务层
 */
public class GMDataCenter {

    private static final boolean DEBUG = GMEnv.DEBUG;

    private static final String TAG = DEBUG ? "GMDataCenter" : GMDataCenter.class.getSimpleName();

    private static StringRequest newStringRequest(int method, String url, final ApiParams params, final IListParser listParser, final IReplyListener replyListener, Response.ErrorListener volleyErrorListener) {
        return new StringRequest(method, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ReplyWrapper userWrapper = ReplyWrapper.parse(response, listParser);
                if (userWrapper.isError()) {
                    replyListener.onReplyParserError(userWrapper);
                } else {
                    replyListener.onReplyParserResponse(userWrapper.getDatas());
                }
            }
        }, volleyErrorListener) {
            protected Map<String, String> getParams() {
                return params;
            }
        };
    }

    public static final IListParser EMPTY_PARSER = new IListParser() {
        @Override
        public List parseList(String json) {
            return null;
        }
    };

    public static final IListParser<String> STRING_PARSER = new IListParser<String>() {
        @Override
        public List<String> parseList(String json) {
            Gson gson = new Gson();
            return gson.fromJson(json,
                    new TypeToken<List<String>>(){}.getType());
        }
    };

    public static void getApi(Object tag,
                                          final IReplyListener<String> replyListener,
                                          Response.ErrorListener volleyErrorListener) {
        final int method = Request.Method.GET;
        int userId = SocialUserManager.getInstance().getSocialUser().getUserId();
        String openId = SocialUserManager.getInstance().getSocialUser().getOpenId();
        RequestManager.addRequest(newStringRequest(method, String.format(GMApi.API, userId, openId), null, STRING_PARSER, replyListener, volleyErrorListener), tag);
    }

    public static void getUserIcon(Object tag,
                                          final IReplyListener<SocialUser> replyListener,
                                          Response.ErrorListener volleyErrorListener) {
        final int method = Request.Method.GET;
        RequestManager.addRequest(newStringRequest(method, GMApi.USER_ICON, null, SocialUser.LIST_PARSER, replyListener, volleyErrorListener), tag);
    }

    public static void getChannelAllUsers(Object tag,
                                          final IReplyListener<SocialUser> replyListener,
                                          Response.ErrorListener volleyErrorListener,
                                          int channelId) {
        final int method = Request.Method.GET;
        RequestManager.addRequest(newStringRequest(method, String.format(GMApi.ALL_USERS, channelId), null, SocialUser.LIST_PARSER, replyListener, volleyErrorListener), tag);
    }

    public static void getChannelCurrUser(Object tag,
                                final IReplyListener<SocialUser> replyListener,
                                Response.ErrorListener volleyErrorListener,
                                int channelId) {
        final int method = Request.Method.GET;
        RequestManager.addRequest(newStringRequest(method, String.format(GMApi.CURR_USER, channelId), null, SocialUser.LIST_PARSER, replyListener, volleyErrorListener), tag);
    }

    public static void postRegisterOrLogin(Object tag,
                             final IReplyListener<SocialUser> replyListener,
                             Response.ErrorListener volleyErrorListener,
                             String userJson) {
        if(DEBUG) {
            Log.d(TAG, "[postRegisterOrLogin] : userJson=" + userJson);
        }
        final int method = Request.Method.POST;
        final ApiParams params = new ApiParams().with("data", userJson);
        RequestManager.addRequest(newStringRequest(method, GMApi.USER_LOGIN, params, SocialUser.LIST_PARSER, replyListener, volleyErrorListener), tag);
    }

    /**
     * 点歌
     * @param tag
     * @param replyListener
     * @param volleyErrorListener
     * @param requestJson
     */
    public static void postChannelRequest(Object tag,
                                          final IReplyListener<Object> replyListener,
                                          Response.ErrorListener volleyErrorListener,
                                          String requestJson) {
        if(DEBUG) {
            Log.d(TAG, "[postChannelRequest] : requestJson=" + requestJson);
        }
        final int method = Request.Method.POST;
        final ApiParams params = new ApiParams().with("data", requestJson);
        RequestManager.addRequest(newStringRequest(method, GMApi.CHANNEL_REQUEST, params, EMPTY_PARSER, replyListener, volleyErrorListener), tag);
    }

    public static void postChannelCommend(Object tag,
                                          final IReplyListener<Object> replyListener,
                                          Response.ErrorListener volleyErrorListener,
                                          String requestJson) {
        if(DEBUG) {
            Log.d(TAG, "[postChannelCommend] : requestJson=" + requestJson);
        }
        final int method = Request.Method.POST;
        final ApiParams params = new ApiParams().with("data", requestJson);
        RequestManager.addRequest(newStringRequest(method, GMApi.CHANNEL_COMMEND, params, EMPTY_PARSER, replyListener, volleyErrorListener), tag);
    }


}
