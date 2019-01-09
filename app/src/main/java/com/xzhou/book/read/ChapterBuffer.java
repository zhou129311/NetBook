package com.xzhou.book.read;

import android.graphics.Paint;

import com.xzhou.book.models.Entities;
import com.xzhou.book.utils.AppUtils;
import com.xzhou.book.utils.FileUtils;
import com.xzhou.book.utils.Log;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ChapterBuffer {
    private static final String TAG = "ChapterBuffer";
    private byte[] mBuffer;
    private long mBufferLen;
    private String mBookId;
    private int mChapter;
    private List<PageLines> mPageList = new ArrayList<>();
    private String mCharset = "UTF-8";
    private int mReadPos = 0;

    public ChapterBuffer(String bookId, int chapter) {
        mBookId = bookId;
        mChapter = chapter;
    }

    public boolean openCacheBookChapter() {
        boolean success = false;
        File file = FileUtils.getChapterFile(mBookId, mChapter);
        if (file.exists() && file.length() > 10) {
            mCharset = FileUtils.getCharset(file.getAbsolutePath());
            RandomAccessFile raf = null;
            try {
                mBufferLen = file.length();
                mBuffer = new byte[(int) mBufferLen];
                raf = new RandomAccessFile(file, "r");
                int i = raf.read(mBuffer);
                Log.i(TAG, "i = " + i);
                if (i == -1 || i == mBufferLen) {
                    success = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            } finally {
                AppUtils.close(raf);
            }
        }
        return success;
    }

    public boolean openNetBookChapter(Entities.Chapter data) {
        mCharset = "UTF-8";
        File file = FileUtils.getChapterFile(mBookId, mChapter);
        FileUtils.writeFile(file.getAbsolutePath(), formatContent(data.body), false);
        try {
            mBuffer = data.body.getBytes(mCharset);
            return true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 格式化小说内容。
     * <p/>
     * <li>小说的开头，缩进2格。在开始位置，加入2格空格。
     * <li>所有的段落，缩进2格。所有的\n,替换为2格空格。
     */
    private static String formatContent(String str) {
        str = str.replaceAll("[ ]*", "");//替换来自服务器上的，特殊空格
        str = str.replaceAll("[ ]*", "");//
        str = str.replace("\n\n", "\n");
        str = str.replace("\n", "\n" + "\u3000\u3000");
        str = "\u3000\u3000" + str;
        return str;
    }

    /**
     * 从pos开始读取一个段落
     */
    private byte[] readParagraphForward(int pos) {
        byte b0;
        int i = pos;
        while (i < mBufferLen) {
            b0 = mBuffer[i++];
            if (b0 == 0x0a) {
                break;
            }
        }
        int size = i - pos;
        byte[] buf = new byte[size];
        for (i = 0; i < size; i++) {
            buf[i] = mBuffer[pos + i];
        }
        return buf;
    }

    /**
     * 计算一共有多少页，保存每一页的数据
     *
     * @param maxLineCount 一页最大行数
     * @param paint        TextView Paint
     * @param width        TextView width
     */
    public void calcPageLines(int maxLineCount, Paint paint, int width) {
        mReadPos = 0;
        mPageList.clear();
        int pageNumber = 0;
        while (mReadPos < mBufferLen) {
            mPageList.add(calcOnePage(maxLineCount, paint, width, pageNumber));
            pageNumber++;
        }
    }

    private PageLines calcOnePage(int maxLineCount, Paint paint, int width, int pageNumber) {
        String paragraphStr = "";
        PageLines pageContent = new PageLines();
        pageContent.lines = new ArrayList<>();
        pageContent.startPos = mReadPos;
        pageContent.page = pageNumber;
        while (pageContent.lines.size() < maxLineCount && mReadPos < mBufferLen) {
            byte[] paragraph = readParagraphForward(mReadPos);
            mReadPos += paragraph.length;
            try {
                paragraphStr = new String(paragraph, mCharset);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            while (paragraphStr.length() > 0) {
                int paintSize = paint.breakText(paragraphStr, true, width, null);
                pageContent.lines.add(paragraphStr.substring(0, paintSize));
                paragraphStr = paragraphStr.substring(paintSize);
                if (pageContent.lines.size() >= maxLineCount) {
                    break;
                }
            }
            if (paragraphStr.length() > 0) {
                try {
                    mReadPos -= (paragraphStr).getBytes(mCharset).length;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        pageContent.endPos = mReadPos;
        return pageContent;
    }

    public PageLines getPageForReadPos(int readPos) {
        for (PageLines content : mPageList) {
            if (readPos >= content.startPos && readPos < content.endPos) {
                return content;
            }
        }
        return mPageList.get(0);
    }

    public PageLines getPageForPos(int pageNumber) {
        if (pageNumber > getPageCount() || pageNumber < 0) {
            Log.e(TAG, "getPageForPos " + pageNumber + " error!");
            return mPageList.get(0);
        }
        return mPageList.get(pageNumber);
    }

    public int getPageCount() {
        return mPageList.size();
    }
}
