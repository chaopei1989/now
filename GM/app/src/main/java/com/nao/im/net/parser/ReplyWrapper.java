package com.nao.im.net.parser;

import org.json.JSONArray;

import java.util.List;

/**
 * Created by chaopei on 2015/9/2.
 * 网络数据封装
 */
public class ReplyWrapper<T> {

    private ReplyParser.ReplyRoot mReply;

    IListParser<T> mListParser;

    private List<T> mList;

    private ReplyWrapper() {}

    public static ReplyWrapper parse(String msg, IListParser listParser) {
        ReplyWrapper data = new ReplyWrapper();
        data.mListParser = listParser;
        data.mReply = ReplyParser.parseRoot(msg);
        return data;
    }

    public static ReplyWrapper parse(String msg) {
        ReplyWrapper data = new ReplyWrapper();
        data.mReply = ReplyParser.parseRoot(msg);
        return data;
    }

    public boolean isError() {
        return mReply.status != ReplyParser.Constants.STATUS_OK;
    }

    public int getStatus() {
        return mReply.status;
    }

    public String getStatusName() {
        return ReplyParser.Constants.errName(mReply.status);
    }

    public String getMsg() {
        return mReply.msg;
    }

    public int getDataLen() {
        if (null == mReply.data) {
            return 0;
        }
        return mReply.data.len;
    }

    public JSONArray getJsonData() {
        if (null == mReply.data) {
            return null;
        }
        return mReply.data.obj;
    }

    public List<T> getDatas() {
        JSONArray obj = getJsonData();
        if (null == obj) {
            return null;
        }
        if (null == mList) {
            mList = mListParser.parseList(obj.toString());
        }
        return mList;
    }

    public T getData() {
        List<T> datas = getDatas();
        if (0 == datas.size()) {
            return null;
        }
        return datas.get(0);
    }

}
