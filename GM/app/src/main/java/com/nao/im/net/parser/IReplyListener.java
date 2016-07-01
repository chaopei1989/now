package com.nao.im.net.parser;

import java.util.List;

/**
 * Created by chaopei on 2015/9/7.
 */
public interface IReplyListener<T> {

    void onReplyParserResponse(List<T> response);

    void onReplyParserError(ReplyWrapper<T> wrapper);
}
