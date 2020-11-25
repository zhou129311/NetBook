package com.xzhou.book.models;

import android.util.SparseArray;

/**
 * File Description:
 * Author: zhouxian
 * Create Date: 20-11-24
 * Change List:
 */
public class TTFEncodeMap {
    public static final SparseArray<String> XSYDW = new SparseArray<>();
    public static final SparseArray<String> QDZWW = new SparseArray<>();
    public static final SparseArray<String> XXSY = new SparseArray<>();
    public static final String[] XSYDW_WORDS = {
            "一", "三", "上", "下", "不", "两", "个", "中", "为", "主", "么", "义", "也", "了", "事", "二",
            "于", "些", "人", "什", "从", "他", "以", "们", "会", "但", "做", "你", "候", "像", "几", "出", "到",
            "前", "动", "去", "又", "发", "只", "可", "同", "后", "向", "命", "和", "回", "国", "在", "地", "声",
            "多", "大", "天", "头", "她", "好", "妈", "子", "学", "它", "家", "对", "小", "山", "工", "已", "年",
            "开", "很", "得", "心", "志", "想", "成", "我", "战", "手", "把", "方", "时", "是", "有", "来", "样",
            "正", "民", "水", "没", "点", "然", "现", "理", "生", "用", "的", "看", "眼", "着", "知", "经", "给",
            "老", "而", "能", "自", "要", "见", "说", "走", "起", "身", "边", "过", "还", "这", "进", "那", "都",
            "里", "长", "问", "面", "高"
    };
    public static final String[] QDZWW_WORDS = {
            ".", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
    };

    public static final String[] XXSY_WORDS = {
            "的", "一", "是", "了", "我", "不", "人", "在", "他", "有", "这", "个", "上", "们", "来", "到",
            "时", "大", "地", "为", "子", "中", "你", "说", "生", "国", "年", "着", "就", "那", "和", "要", "她",
            "出", "也", "得", "里", "后", "自", "以", "会", "家", "可", "下", "而", "过", "天", "去", "能", "对",
            "小", "多", "然", "于", "心", "学", "么", "之", "都", "好", "看", "起", "发", "当", "没", "成", "只",
            "如", "事", "把", "还", "用", "第", "样", "道", "想", "作", "种", "开", "美", "总", "从", "无", "情",
            "已", "面", "最", "女", "但", "现", "前", "些", "所", "同", "日", "手", "又", "行", "意", "动"
    };

    static {
        for (int i = 0; i < XSYDW_WORDS.length; i++) {
            XSYDW.put(i, XSYDW_WORDS[i]);
        }
        for (int i = 0; i < QDZWW_WORDS.length; i++) {
            QDZWW.put(i, QDZWW_WORDS[i]);
        }
        for (int i = 59392; i < 59392 + XXSY_WORDS.length; i++) {
            XXSY.put(i, XXSY_WORDS[i - 59392]);
        }
    }

}
