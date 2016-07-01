package com.nao.im.net.parser;

import android.util.Log;

import com.nao.im.GMEnv;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chaopei on 2015/9/2.
 * 默认的数据协议解析器，基本数据格式为
 * <pre>
 {
     status:0,
     msg:'',
     data:{
         len:1,
         obj:[{
             cid:0,
             name:'what'
         },]
     }
 } <pre/>
 */
public class ReplyParser {

    private static final boolean DEBUG = GMEnv.DEBUG;

    private static final String TAG = DEBUG ? "ReplyParser" : ReplyParser.class.getSimpleName();

    public static class Constants {
        public static final String KEY_STATUS = "status";
        public static final String KEY_MSG = "msg";
        public static final String KEY_DATA = "data";
        public static final String KEY_LEN = "len";
        public static final String KEY_OBJ = "obj";

        public static final int STATUS_ILLEGAL_AUTH = -3;
        public static final int STATUS_ILLEGAL_ARGS = -2;
        public static final int STATUS_INTERNAL_ERROR = -1;
        public static final int STATUS_OK = 0;
        public static final int STATUS_CHANNEL_NOT_EXIST = 1;
        public static final int STATUS_JSON_ERROR = -100;
        public static final int STATUS_URL_ERROR = -101;

        public static final int ERR_NO_SUCH_USER = -1024;
        public static final int ERR_NO_SUCH_MUSIC = -1025;
        public static final int ERR_NO_SUCH_CHANNEL = -1026;
        public static final int ERR_CHANNEL_EXIST = -1027;
        public static final int ERR_DUPLICATED = -1028;
        public static final int ERR_NO_PRIVILEGE = -1029;
        public static final int ERR_NOT_READY_YET = -1030;
        public static final int ERR_MUSIC_FULL = -1031;

        public static String errName(int status) {
            switch (status) {
                case STATUS_ILLEGAL_AUTH:
                    return "STATUS_ILLEGAL_AUTH";
                case STATUS_ILLEGAL_ARGS:
                    return "STATUS_ILLEGAL_ARGS";
                case STATUS_INTERNAL_ERROR:
                    return "STATUS_INTERNAL_ERROR";
                case STATUS_CHANNEL_NOT_EXIST:
                    return "STATUS_CHANNEL_NOT_EXIST";
                case STATUS_JSON_ERROR:
                    return "STATUS_JSON_ERROR";
                case STATUS_URL_ERROR:
                    return "STATUS_URL_ERROR";
                case ERR_NO_SUCH_USER:
                    return "ERR_NO_SUCH_USER";
                case ERR_NO_SUCH_MUSIC:
                    return "ERR_NO_SUCH_MUSIC";
                case ERR_NO_SUCH_CHANNEL:
                    return "ERR_NO_SUCH_CHANNEL";
                case ERR_CHANNEL_EXIST:
                    return "ERR_CHANNEL_EXIST";
                case ERR_DUPLICATED:
                    return "ERR_DUPLICATED";
                case ERR_NO_PRIVILEGE:
                    return "ERR_NO_PRIVILEGE";
                case ERR_NOT_READY_YET:
                    return "ERR_NOT_READY_YET";
                case ERR_MUSIC_FULL:
                    return "ERR_MUSIC_FULL";
                default:
                    return String.valueOf(status);
            }
        }
    }

    public static String getStatusDesc(int status) {
        switch (status) {
            case Constants.STATUS_OK:
                return "STATUS_OK";
            case Constants.STATUS_ILLEGAL_ARGS:
                return "STATUS_ILLEGAL_ARGS";
            default:
                return "STATUS_UNKNOWN";
        }
    }

    public static class ReplyData {
        int len;
        JSONArray obj;
    }

    public static class ReplyRoot {
        int status;
        String msg;
        ReplyData data;
    }

    /**
     * 用于服务端返回的消息一级解析
     *
     * @param reply
     */
    public static ReplyRoot parseRoot(String reply) {
        if (DEBUG) {
            Log.d(TAG, "[parseRoot] : " + reply);
        }
        JSONObject jo = null;
        ReplyRoot rr = new ReplyRoot();
        try {
            jo = new JSONObject(reply);
            rr.status = jo.getInt(Constants.KEY_STATUS);
            rr.msg = jo.getString(Constants.KEY_MSG);
            if (jo.isNull(Constants.KEY_DATA)) {
                //一定是有错误了
                rr.data = null;
            } else {
                JSONObject data = jo.getJSONObject(Constants.KEY_DATA);
                rr.data = new ReplyData();
                rr.data.len = data.getInt(Constants.KEY_LEN);
                JSONArray obj = data.getJSONArray(Constants.KEY_OBJ);
                int len = obj.length();
                if (rr.data.len != len) {
                    throw new JSONException(String.format("data.obj.length(%d) != data.len(%d)", rr.data.len, len));
                }
                rr.data.obj = obj;
            }
        } catch (JSONException e) {
            if (DEBUG) {
                Log.e(TAG, "[parseRoot]", e);
            }
            rr.status = Constants.STATUS_JSON_ERROR;
            rr.msg = e.getMessage();
            rr.data = null;
        }
        return rr;
    }

}
