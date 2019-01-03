package com.xzhou.book.utils;

public class AppSettings {
    public static final String PRE_KEY_ISNIGHT = "pre_key_is_night";

    public void saveReadProgress(String bookId, int currentChapter, int bufBeginPos, int bufEndPos) {
        SPUtils.get().putInt(getChapterKey(bookId), currentChapter)
                .putInt(getStartPosKey(bookId), bufBeginPos)
                .putInt(getEndPosKey(bookId), bufEndPos);
    }

    public int[] getReadProgress(String bookId) {
        int lastChapter = SPUtils.get().getInt(getChapterKey(bookId), 1);
        int startPos = SPUtils.get().getInt(getStartPosKey(bookId), 0);
        int endPos = SPUtils.get().getInt(getEndPosKey(bookId), 0);
        return new int[] { lastChapter, startPos, endPos };
    }

    private String getChapterKey(String bookId) {
        return bookId + "-chapter";
    }

    private String getStartPosKey(String bookId) {
        return bookId + "-startPos";
    }

    private String getEndPosKey(String bookId) {
        return bookId + "-endPos";
    }
}
