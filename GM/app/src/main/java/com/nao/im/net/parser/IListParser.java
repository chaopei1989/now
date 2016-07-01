package com.nao.im.net.parser;

import java.util.List;

/**
 * Created by chaopei on 2015/9/2.
 * 列表数据解析器接口
 */
public interface IListParser<T> {

    List<T> parseList(String json);

}
