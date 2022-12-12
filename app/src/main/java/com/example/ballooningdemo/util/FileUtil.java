package com.example.ballooningdemo.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by snowbean on 16-8-5.
 */
public class FileUtil {
    private static final String TAG = "FileUtil";

    public static String getFolderName(String name) {
        File mediaStorageDir =
                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        name);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return "";
            }
        }
        return mediaStorageDir.getAbsolutePath();
    }

    /**
     * 判断sd卡是否可以用
     */
    private static boolean isSDAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static File getNewFile(Context context, String folderName) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);

        String timeStamp = simpleDateFormat.format(new Date());

        String path;
        if (isSDAvailable()) {
            path = getFolderName(folderName) + File.separator + timeStamp + ".jpg";
        } else {
            path = context.getFilesDir().getPath() + File.separator + timeStamp + ".jpg";
        }

        if (TextUtils.isEmpty(path)) {
            return null;
        }

        return new File(path);
    }

    public static File getSavePngFile(Context context, String folderName) {
        String path = getPublicDiskFileDir(context, folderName).getAbsolutePath() + File.separator + System.currentTimeMillis() + ".png";
        return new File(path);
    }

    public static File getSaveGifFile(Context context, String folderName) {
        String path = getPublicDiskFileDir(context, folderName).getAbsolutePath() + File.separator + System.currentTimeMillis() + ".gif";
        return new File(path);
    }


    public static File getPublicDiskFileDir(Context context, String fileName) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {//此目录下的是外部存储下的私有的fileName目录
            cachePath = context.getExternalFilesDir(fileName).getAbsolutePath();  //mnt/sdcard/Android/data/com.my.app/files/fileName
        } else {
            cachePath = context.getFilesDir().getPath() + "/" + fileName;        //data/data/com.my.app/files
        }
        cachePath = context.getFilesDir().getPath() + "/" + fileName;
        File file = new File(cachePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;  //mnt/sdcard/Android/data/com.my.app/files/fileName
    }

    public static File getPublicCacheDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getAbsolutePath();
        } else {
            cachePath = context.getCacheDir().getAbsolutePath();
        }
        File file = new File(cachePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }


    public static File saveImageToGallery(File file, Bitmap bmp) {
        if (bmp == null) {
            throw new IllegalArgumentException("bmp should not be null");
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Log.e("Exception", "msg: " + e.getMessage());
        }

        Log.e(TAG, "saveImageToGallery: the path of bmp is " + file.getAbsolutePath());
        return file;
    }

    public static void notifySystemGallery(@NonNull Context context, @NonNull File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("bmp should not be null");
        }
        try {
            Uri uri = null;
            ContentValues values = new ContentValues();
            String suffix = getFileSuffix(file.getName()).toLowerCase();
            if (".gif".equals(suffix)) {
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/gif");
            } else {
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            }
            if (Build.VERSION.SDK_INT >= 29) {
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/emoji");
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis() + suffix);
                uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                copyFile(context, file, uri);
                values.clear();
                values.put(MediaStore.MediaColumns.IS_PENDING, 0);
                values.putNull(MediaStore.MediaColumns.DATE_EXPIRES);
                context.getContentResolver().update(uri, values, null, null);
            } else {
                values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                uri = context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        } catch (Exception e) {
            Log.e("Exception", "msg: " + e.getMessage());
        }
    }


    public static File getPicturePath() {
        String filePath = Environment.DIRECTORY_PICTURES + "/emoji";
        File dir = new File(filePath);
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (Exception e) {
                Log.e("Exception", "msg: " + e.getMessage());
            }
        }
        return dir;
    }


    public static boolean saveGif(byte[] data, String gifPath) {
        if (data == null || data.length == 0 || TextUtils.isEmpty(gifPath)) {
            return false;
        }
        boolean result = true;
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(gifPath);
            outputStream.write(data);
            outputStream.flush();
        } catch (Exception e) {
            result = false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    Log.e("Exception", "msg: " + e.getMessage());
                }

            }
        }
        return result;
    }


    private static int copyStream(InputStream input, OutputStream output) throws Exception, IOException {
        final int BUFFER_SIZE = 1024 * 2;
        byte[] buffer = new byte[BUFFER_SIZE];
        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
        int count = 0, n = 0;
        try {
            while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
            try {
                in.close();
            } catch (IOException e) {
            }
        }
        return count;
    }

    private static void copyFile(Context context, Uri srcUri, File dstFile) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(srcUri);
            outputStream = new FileOutputStream(dstFile);
            copyStream(inputStream, outputStream);
        } catch (Exception e) {
            Log.e("Exception", "msg: " + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e("Exception", "msg: " + e.getMessage());
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e("Exception", "msg: " + e.getMessage());
                }
            }
        }
    }

    private static void copyFile(Context context, File srcFile, Uri dstUri) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(srcFile);
            outputStream = context.getContentResolver().openOutputStream(dstUri);
            copyStream(inputStream, outputStream);
        } catch (Exception e) {
            Log.e("Exception", "msg: " + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e("Exception", "msg: " + e.getMessage());
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e("Exception", "msg: " + e.getMessage());
                }
            }
        }

    }


    private static void copyFile(File srcFile, File dstFile) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(srcFile);
            outputStream = new FileOutputStream(dstFile);
            copyStream(inputStream, outputStream);
        } catch (Exception e) {
            Log.e("Exception", "msg: " + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e("Exception", "msg: " + e.getMessage());
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e("Exception", "msg: " + e.getMessage());
                }
            }
        }
    }

    private static String getFileName(Context context, Uri uri) {
        if (uri == null) return null;
        String fileName = null;
        if (uri.getScheme()==null){
            String path = uri.getPath();
            int cut = path.lastIndexOf('/');
            if (cut != -1) {
                fileName = path.substring(cut + 1);
            }
        }else {
            if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                ContentResolver contentResolver = context.getContentResolver();
                Cursor cursor = contentResolver.query(uri, null, null, null, null);
                if (cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } else {
                String path = uri.getPath();
                int cut = path.lastIndexOf('/');
                if (cut != -1) {
                    fileName = path.substring(cut + 1);
                }
            }
        }

        return System.currentTimeMillis() + fileName;
    }

    public static String getFileSuffix(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));//例如：abc.png  截取后：.png
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String[] projection = {MediaStore.Audio.Media.DATA};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;

    }


    public static String getPath(final Context context, final Uri uri) {
        if (Build.VERSION.SDK_INT <  Build.VERSION_CODES.Q) {
            return uri.getPath();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //判断版本获取路径的方式，在拿到uri之后进行版本判断大于等于24（即Android7.0）用最新的获取路径方式，否则用你之前的方式，
            //https://blog.csdn.net/m13984458297/article/details/83578231
            File rootDataDir = context.getFilesDir();
            String fileName = getFileName(context, uri);
            if (!TextUtils.isEmpty(fileName)) {
                Log.d(TAG, "getPath: ");
                File copyFile = new File(rootDataDir + File.separator + fileName);
                copyFile(context, uri, copyFile);
                return copyFile.getAbsolutePath();
            }
            return null;
        }
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if (!type.isEmpty()) {
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }

            } else if (isDownloadsDocument(uri)) { // DownloadsProvider

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) { // MediaProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if (type != null) {
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                } else {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }


                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) { // MediaStore (and general)
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) { // File
            return uri.getPath();
        } else {
            return uri.getPath();
        }
        Log.d(TAG, "getPath: uri.getScheme()" + (uri.getScheme()) + "--" + uri.getPath());
        return null;
    }


    /**
     * 保存文件到指定路径保存到相册
     */
    public static String saveImageToGallery(Activity mActivity, String bmpPath) {
        Log.d(TAG, "saveImageToGallery:  "+bmpPath);
        File imgFile = new File(bmpPath);
        Log.d(TAG, "saveImageToGallery:  imgFile"+imgFile +"---"+(!imgFile.exists()));
        if (!imgFile.exists()) {
            return "fail";
        }
        //插入图片到系统相册
        try {
            String path = MediaStore.Images.Media.insertImage(mActivity.getContentResolver(), bmpPath, System.currentTimeMillis() + ".jpg", "下载图片");
            Log.d(TAG, "saveImageToGallery: path"+path);
            //保存图片后发送广播通知更新数据库
            if (path!=null){
                Uri uri = Uri.parse(path);
                mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            }
            return path;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 保存文件到指定路径保存到相册
     */
    public static String saveImageToGallery(Activity mActivity, Bitmap bmpPath) {
        Log.d(TAG, "saveImageToGallery:  "+bmpPath);
        //插入图片到系统相册
        try {
            String path = MediaStore.Images.Media.insertImage(mActivity.getContentResolver(), bmpPath, System.currentTimeMillis() + ".jpg", "下载图片");
            Log.d(TAG, "saveImageToGallery: path"+path);
            //保存图片后发送广播通知更新数据库
            if (path!=null){
                Uri uri = Uri.parse(path);
                mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            }
            return path;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void sharePictures(Context context, String path) {
        try {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            Uri uri = getUri(context, path);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(shareIntent);
        } catch (Exception e) {
            Log.e("Exception", "msg: " + e.getMessage());
        }

    }


    public static Uri getUri(Context context, String url) {
        File tempFile = new File(url);
        //判断版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {   //如果在Android7.0以上,使用FileProvider获取Uri
            try {
                return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", tempFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {    //否则使用Uri.fromFile(file)方法获取Uri
            return Uri.fromFile(tempFile);
        }
        return null;
    }


    public interface FileCallBack {
        void path(String path);
    }

    public  static String  saveBitmap(Context context, Uri contentUri)  {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    /**
     * 创建制定目录下的视频文件
     *
     * @param context
     * @param
     * @return
     */
    public static File createVideoDiskFile(Context context, String fileName) {
        return createDiskFile(context, Environment.DIRECTORY_MOVIES, fileName);
    }
    /**
     * getExternalFilesDir()提供的是私有的目录,不可见性，在app卸载后会被删除
     * <p>
     * getExternalCacheDir():提供外部缓存目录，是可见性的。
     *
     *  通过Context
     *
     *
     * @param context
     * @param dirName
     * @param fileName
     * @return
     */
    private static File createDiskFile(Context context, String dirName, String fileName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            cachePath = Environment.getExternalStoragePublicDirectory(dirName).getAbsolutePath();
        } else {
            cachePath = context.getFilesDir().getAbsolutePath();
        }
        return new File(cachePath + File.separator + fileName);
    }
    /**
     * 创建Video的文件名
     *
     * @return
     */
    public static String createVideoFileName() {
        return createFileNameWithTime() + ".mp4";
    }
    /**
     * 以当前时间，加MD5编码后的文件名
     *
     * @return
     */
    private static String createFileNameWithTime() {
        String currentDate = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return currentDate;
    }

    public static File createFile() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.CHINA);
        File file =new  File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "VideoAudioClip");
        if (!file.exists())
            file.mkdirs();
        return new  File(file, "VID_"+sdf.format(new Date())+".mp4");
    }
}
