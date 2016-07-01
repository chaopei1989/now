package com.nao.im.net;

import com.nao.im.GMEnv;

/**
 * Created by chaopei on 2015/9/7.
 * 网络接口
 */
public class GMApi {
    public static final String HOST = "182.92.103.53";

    public static final int PORT = GMEnv.TEST ? 3000 : 3333;

    public static final String URL = String.format("http://%s:%d/", GMApi.HOST, GMApi.PORT);

    public static final String URI_CHANNEL = URL + "channel";

    public static final String CURR_USER = URL + "channel/%d/users/curr";

    public static final String ALL_USERS = URL + "channel/%d/users/all";

    public static final String USER_LOGIN = URL + "user/login";

    public static final String USER_ICON = URL + "user/icon";

    public static final String CHANNEL_REQUEST = URL + "channel/request";

    public static final String CHANNEL_COMMEND = URL + "channel/commend";

    public static final String API = URL + "api?userId=%d&openId=%s";

    public final static class STATUS {
        public final static int ILLEGAL_ARGS = -2;
        public final static int INTERNAL_ERR = -1;
        public final static int OK = 0;
        public final static int CHANNEL_NOT_EXIST = 1;
    }
}
