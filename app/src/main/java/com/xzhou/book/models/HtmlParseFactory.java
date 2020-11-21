package com.xzhou.book.models;

import android.util.SparseArray;

public class HtmlParseFactory {

    private static final SparseArray<HtmlParse> PARSES = new SparseArray<>();

    public static HtmlParse getHtmlParse(String host) {
        Integer type = SearchModel.getType(host);
        if (type == null) {
            return null;
        }
        return getHtmlParse(type);
    }

    public static HtmlParse getHtmlParse(@SearchModel.ParseType int type) {
        HtmlParse parse = PARSES.get(type);
        if (parse == null) {
            parse = createParse(type);
            PARSES.put(type, parse);
        }
        return parse;
    }

    private static HtmlParse createParse(@SearchModel.ParseType int type) {
        switch (type) {
        case SearchModel.ParseType.PARSE_TYPE_1:
            return new HtmlParse1();
        case SearchModel.ParseType.PARSE_TYPE_2:
            return new HtmlParse2();
        case SearchModel.ParseType.PARSE_TYPE_3:
            return new HtmlParse3();
        case SearchModel.ParseType.PARSE_TYPE_4:
            return new HtmlParse4();
        case SearchModel.ParseType.PARSE_TYPE_5:
            return new HtmlParse5();
        case SearchModel.ParseType.PARSE_TYPE_6:
            return new HtmlParse6();
        case SearchModel.ParseType.PARSE_TYPE_7:
            return new HtmlParse7();
        }
        return null;
    }
}
