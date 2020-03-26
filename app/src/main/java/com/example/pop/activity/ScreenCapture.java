package com.example.pop.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;

/**
 * Android internals have been modified to store images in the media folder with
 * the correct date meta data
 * @author samuelkirton
 */

public class ScreenCapture {

    public static final void insertImage(ContentResolver cr, Bitmap source, String title, String description, String folderName) {
        String path = createDirectoryAndSaveFile(source, title, folderName);

        OutputStream fOut = null;
        File file = new File(path);

        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        source.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ContentValues values = new ContentValues();
        values.put(Images.Media.TITLE, title);
        values.put(Images.Media.DESCRIPTION, description);
        values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis ());
        values.put(Images.ImageColumns.BUCKET_ID, file.toString().toLowerCase(Locale.US).hashCode());
        values.put(Images.ImageColumns.BUCKET_DISPLAY_NAME, file.getName().toLowerCase(Locale.US));
        values.put("_data", file.getAbsolutePath());

        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }


    private static String createDirectoryAndSaveFile(Bitmap imageToSave, String fileName, String folderName) {

        File directory = new File(Environment.getExternalStorageDirectory() + "/PoP Receipts/" + folderName);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }
}