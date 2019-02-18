package com.xzhou.book.utils;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RichTextUtils {

    /**
     * 获取带颜色的文本，设定关键字颜色，这里只接受一个关键字，并且没有点击事件
     *
     * @param originText 原始文本
     * @param keyword    需要颜色的文字
     * @param color      颜色
     * @return CharSequence 处理后的文字
     */
    public static CharSequence getColorString(String originText, String keyword, int color) {
        return getColorString(originText, keyword, color, null);
    }

    /**
     * 获取带颜色的文本，将给定的元是字符串
     *
     * @param originText 原始文本
     * @param keyword    关键字
     * @param color      颜色
     * @param listener   点击关键字的监听回调，可空
     * @return
     */
    public static CharSequence getColorString(String originText, String keyword,final int color,
                                              final View.OnClickListener listener) {
        SpannableString s = new SpannableString(originText);
        Pattern p = Pattern.compile(keyword);
        Matcher m = p.matcher(s);

        while (m.find()) {
            int start = m.start();
            int end = m.end();
            s.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (listener != null) {
                s.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        listener.onClick(widget);
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        ds.setColor(color);
                        ds.setUnderlineText(false);
                    }
                }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return s;
    }

    public static CharSequence getColorString(String originText, List<String> keywords,
                                              Map<String, Integer> colorMap) {
        return getColorString(originText, keywords, colorMap, null);
    }

    public static CharSequence getColorString(String originText, List<String> keywords,
                                              final Map<String, Integer> colorMap, Map<String, View.OnClickListener> listenerMap) {
        SpannableString s = new SpannableString(originText);

        for (int i = 0; i < keywords.size(); i++) {
            final String keyword = keywords.get(i);
            Pattern p = Pattern.compile(keyword);
            Matcher m = p.matcher(s);

            while (m.find()) {
                int start = m.start();
                int end = m.end();

                s.setSpan(new ForegroundColorSpan(colorMap.get(keyword)), start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                if (listenerMap != null) {
                    final View.OnClickListener listener = listenerMap.get(keyword);
                    if (listener != null) {
                        s.setSpan(new ClickableSpan() {
                            @Override
                            public void onClick(View widget) {
                                listener.onClick(widget);
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                ds.setColor(colorMap.get(keyword));
                                ds.setUnderlineText(false);
                            }
                        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }

        return s;
    }

    private SpannableStringBuilder builder = null;

    /**
     * 拼接出特殊的文字，可以选择加入颜色和点击事件
     *
     * @param str 加入的文字
     * @return 本对象
     */
    public RichTextUtils append(String str) {
        return append(str, -1, null);
    }

    /**
     * 拼接出特殊的文字，可以选择加入颜色和点击事件
     *
     * @param str   加入的文字
     * @param color 颜色
     * @return 本对象
     */
    public RichTextUtils append(String str, int color) {
        return append(str, color, null);
    }

    /**
     * 拼接出特殊的文字，可以选择加入颜色和点击事件
     *
     * @param str      加入的文字
     * @param color    颜色
     * @param listener 点击事件
     * @return 本对象
     */
    public RichTextUtils append(String str,final int color,final View.OnClickListener listener) {
        if (TextUtils.isEmpty(builder)) {
            builder = new SpannableStringBuilder();
        }

        if (TextUtils.isEmpty(str)) {
            return null;
        }

        if (color == -1) {
            builder.append(str);
            return this;
        }

        SpannableString span = new SpannableString(str);
        span.setSpan(new ForegroundColorSpan(color), 0, str.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (listener != null) {
            span.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    listener.onClick(widget);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    ds.setColor(color);
                    ds.setUnderlineText(false);
                }
            }, 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        builder.append(span);
        return this;
    }

    public SpannableStringBuilder finish() {
        return builder;
    }
}
