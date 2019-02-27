package com.xzhou.book.utils;

import android.content.Context;
import android.os.Environment;
import android.text.format.Formatter;

import com.xzhou.book.MyApp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {

    public static boolean hasCacheChapter(String bookId, int chapter) {
        File file = getChapterFile(bookId, chapter);
        return file.exists() && file.length() > 10;
    }

    public static String getChapterPath(String bookId, int chapter) {
        return getBookDir(bookId) + File.separator + chapter + ".txt";
    }

    public static File getChapterFile(String bookId, int chapter) {
        return new File(getChapterPath(bookId, chapter));
    }

    public static String getBookDir(String bookId) {
        File file = new File(getFilePath(MyApp.getContext()), bookId);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    public static void deleteBookDir(String bookId) {
        File file = new File(getCachePath(MyApp.getContext()), bookId);
        if (file.exists()) {
            deleteFileOrDirectory(file);
        }
    }

    public static String getFilePath(Context context) {
        String cacheRootPath;
        File file = context.getExternalFilesDir("book");
        if (file != null && file.exists()) {
            cacheRootPath = file.getPath();
        } else {
            cacheRootPath = context.getFilesDir().getPath();
        }
        return cacheRootPath;
    }

    public static String getCachePath(Context context) {
        String cacheRootPath;
        File file = context.getExternalCacheDir();
        if (file != null && file.exists()) {
            cacheRootPath = file.getPath();
        } else {
            cacheRootPath = context.getCacheDir().getPath();
        }
        return cacheRootPath;
    }

    public static boolean isSdCardAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static void writeFile(String filePath, String content, boolean isAppend) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath, isAppend);
            byte[] bytes = content.getBytes();
            fos.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            AppUtils.close(fos);
        }
    }

    public static boolean deleteFileOrDirectory(File file) {
        try {
            if (file != null && file.isFile()) {
                return file.delete();
            }
            if (file != null && file.isDirectory()) {
                File[] childFiles = file.listFiles();
                if (childFiles == null || childFiles.length == 0) {
                    return file.delete();
                }
                for (File childFile : childFiles) {
                    deleteFileOrDirectory(childFile);
                }
                return file.delete();
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public static long getFolderSize(String dir) {
        File file = new File(dir);
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (File f : fileList) {
                if (f.isDirectory()) {
                    size = size + getFolderSize(f.getAbsolutePath());
                } else {
                    size = size + f.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    public static String getCharset(String fileName) {
        BufferedInputStream bis = null;
        FileInputStream fis = null;
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            fis = new FileInputStream(fileName);
            bis = new BufferedInputStream(fis);
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
                return charset;
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE
                    && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF
                    && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8";
                checked = true;
            }
            bis.mark(0);
            if (!checked) {
                while ((read = bis.read()) != -1) {
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
                            // (0x80 - 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {// 也有可能出错，但是几率较小
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            AppUtils.close(bis);
            AppUtils.close(fis);
        }

        return charset;
    }

    public static String getCharset1(String fileName) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName));
        int p = (bin.read() << 8) + bin.read();

        String code;
        switch (p) {
        case 0xefbb:
            code = "UTF-8";
            break;
        case 0xfffe:
            code = "Unicode";
            break;
        case 0xfeff:
            code = "UTF-16BE";
            break;
        default:
            code = "GBK";
        }
        return code;
    }
}