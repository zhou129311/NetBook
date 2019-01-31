package com.xzhou.book.models;

import android.util.SparseArray;

public class HtmlParseFactory {

    private static final SparseArray<HtmlParse> PARSES = new SparseArray<>();

    public static HtmlParse getHtmlParse(String host) {
        Integer type = BaiduModel.getType(host);
        if (type == null) {
            return null;
        }
        HtmlParse parse = PARSES.get(type);
        if (parse == null) {
            parse = createParse(type);
            PARSES.put(type, parse);
        }
        return parse;
    }

    private static HtmlParse createParse(@BaiduModel.ParseType int type) {
        switch (type) {
        case BaiduModel.ParseType.PARSE_TYPE_1:
            return new HtmlParse1();
        case BaiduModel.ParseType.PARSE_TYPE_2:
            return new HtmlParse2();
        case BaiduModel.ParseType.PARSE_TYPE_3:
            return new HtmlParse3();
        }
        return null;
    }
}
