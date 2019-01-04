package com.xzhou.book.read;

import android.util.SparseArray;

import com.xzhou.book.utils.FileUtils;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class ChapterBuffer {
    private static final String TAG = "ChapterBuffer";
    private MappedByteBuffer mBuffer;
    private long mBufferLen;
    private String mBookId;
    private int mChapter;
    private PageContent mPageContent;
    private SparseArray<PageContent> mPageList = new SparseArray<>();
    private String mCharset = "UTF-8";

    public ChapterBuffer(String bookId, int chapter) {
        mBookId = bookId;
        mChapter = chapter;
    }

    public boolean openBookChapter() {
        boolean success = false;
        File file = FileUtils.getChapterFile(mBookId, mChapter);
        if (file.exists() && file.length() > 10) {
            mCharset = FileUtils.getCharset(file.getAbsolutePath());
            try {
                mBufferLen = file.length();
                mBuffer = new RandomAccessFile(file, "r")
                        .getChannel()
                        .map(FileChannel.MapMode.READ_ONLY, 0, mBufferLen);
                success = true;
            } catch (Exception e) {
                success = false;
            }
        }
        return success;
    }

    /**
     * 从pos开始读取一个段落
     */
//    private byte[] readParagraphForward(int pos) {
//        byte b0;
//        int i = readPos;
//        while (i < mBufferLen) {
//            b0 = mBuffer.get(i++);
//            if (b0 == 0x0a) {
//                break;
//            }
//        }
//        int nParaSize = i - curEndPos;
//        byte[] buf = new byte[nParaSize];
//        for (i = 0; i < nParaSize; i++) {
//            buf[i] = mbBuff.get(curEndPos + i);
//        }
//        return buf;
//    }

    /**
     * @param maxLineCount 一页最大行数
     * @param lineTextCount 一行最多能显示的字数
     */
    public void calcPageLines(int maxLineCount, int lineTextCount) {
        int page = 0;
        while () {

        }
    }

    public int getPageCount() {
        return mPageList.size();
    }

    public String getPageContent(int pageNumber) {
        if (pageNumber < 0 || pageNumber >= mPageList.size()) {
            throw new IndexOutOfBoundsException("pageNumber >= mPageList.size()");
        }
        PageContent content = mPageList.get(pageNumber);
        StringBuilder sb = new StringBuilder();
        for (String line : content.lines) {
            sb.append(line);
        }
        return sb.toString();
    }

}
