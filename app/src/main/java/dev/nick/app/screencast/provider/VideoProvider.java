package dev.nick.app.screencast.provider;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.modle.Video;

public class VideoProvider {

    private static final File RECORDINGS_DIR = new File(Environment.getExternalStorageDirectory().getPath(), SettingsProvider.STORAGE_MP4_FOLDER_NAME);

    private Context context;

    public VideoProvider(Context context) {
        this.context = context;
    }

    @NonNull
    public List<Video> getList() {
        List<Video> list = new ArrayList<>();
        if (context != null) {
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null,
                    null, MediaStore.Video.Media.DATE_MODIFIED + " desc");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor
                            .getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    String title = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                    String album = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM));
                    String artist = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
                    String displayName = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                    String mimeType = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                    String path = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    File file = new File(path);
                    if (!file.exists()) continue;
                    if (!file.getParentFile().equals(RECORDINGS_DIR)) {
                        continue;
                    }
                    long duration = cursor
                            .getInt(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    long size = cursor
                            .getLong(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                    Video video = new Video(id, title, album, artist, displayName, mimeType, path, size, formatDuration(context, duration));
                    list.add(video);
                }
                cursor.close();
            }
        }
        return list;
    }

    private String formatDuration(Context c, long time) {
        return c.getString(R.string.video_length,
                DateUtils.formatElapsedTime(time / 1000));
    }

}
