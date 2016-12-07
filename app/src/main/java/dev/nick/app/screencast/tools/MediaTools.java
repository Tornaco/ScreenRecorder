package dev.nick.app.screencast.tools;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.File;

public abstract class MediaTools {

    public static Intent buildSharedIntent(Context context, File imageFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("video/mp4");
            Uri uri;

            String filePath = imageFile.getAbsolutePath();
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID},
                    MediaStore.Images.Media.DATA + "=? ",
                    new String[]{filePath}, null);

            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int id = cursor.getInt(cursor
                            .getColumnIndex(MediaStore.MediaColumns._ID));
                    Uri baseUri = Uri.parse("content://media/external/images/media");
                    uri = Uri.withAppendedPath(baseUri, "" + id);
                } else {
                    if (imageFile.exists()) {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.DATA, filePath);
                        uri = context.getContentResolver().insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    } else {
                        return null;
                    }
                }
                sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
                return sharingIntent;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("video/mp4");
            Uri uri = Uri.parse("file://" + imageFile.getAbsolutePath());
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, imageFile.getName());
            return sharingIntent;

        }
    }

    public static Intent buildOpenIntent(Context context, File imageFile) {
        Intent open = new Intent(Intent.ACTION_VIEW);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String filePath = imageFile.getAbsolutePath();
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID},
                    MediaStore.Images.Media.DATA + "=? ",
                    new String[]{filePath}, null);

            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int id = cursor.getInt(cursor
                            .getColumnIndex(MediaStore.MediaColumns._ID));
                    Uri baseUri = Uri.parse("content://media/external/images/media");
                    uri = Uri.withAppendedPath(baseUri, "" + id);
                } else {
                    if (imageFile.exists()) {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.DATA, filePath);
                        uri = context.getContentResolver().insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    } else {
                        return null;
                    }
                }

                return open;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            uri = Uri.parse("file://" + imageFile.getAbsolutePath());
        }
        open.setDataAndType(uri, "video/mp4");
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return open;
    }
}
