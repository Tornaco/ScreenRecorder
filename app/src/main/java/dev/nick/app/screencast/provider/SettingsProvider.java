package dev.nick.app.screencast.provider;

import android.annotation.TargetApi;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;

import dev.nick.app.screencast.app.Factory;
import dev.nick.app.screencast.camera.PreviewSize;

public abstract class SettingsProvider {

    public static final String STORAGE_FOLDER_NAME = "ScreenRecorder";

    static Impl sImpl;

    public static synchronized SettingsProvider get() {
        if (sImpl == null) sImpl = new Impl();
        return sImpl;
    }

    public abstract boolean withAudio();

    public abstract void setWithAudio(boolean value);

    public abstract boolean withCamera();

    public abstract void setWithCamera(boolean value);

    public abstract int previewSize();

    public abstract void setPreviewSize(int size);

    public abstract float resolutionScaleFactor();

    public abstract void setResolutionScaleFactor(float factor);

    public abstract boolean setShowTouch(boolean show);

    public abstract boolean showTouch();

    public abstract int preferredCamera();

    public abstract void setPreferredCamera(int index);

    public abstract boolean hideAppWhenStart();

    public abstract void setHideAppWhenStart(boolean hide);

    public abstract long startDelay();

    public abstract void setStartDelay(long mills);

    public abstract boolean showAD();

    public abstract void setShowAD(boolean show);

    public abstract boolean firstStart();

    public abstract void setFirstStart(boolean first);

    static class Impl extends SettingsProvider {

        private static final String KEY_FIRST_START = "settings.first.start";
        private static final String KEY_WITH_AUDIO = "settings.with.audio";
        private static final String KEY_WITH_CAMERA = "settings.with.camera";
        private static final String KEY_PREVIEW_SIZE = "settings.preview.size";
        private static final String KEY_RES_FACTOR = "settings.res.factor";
        private static final String KEY_PREDERRED_CAM = "settings.preferred.cam";
        private static final String KEY_HIDE_AUTO = "settings.hide.app.auto";
        private static final String KEY_START_DELAY = "settings.start.delay";
        private static final String KEY_SHOW_AD = "settings.show.ad";

        private static final String SHOW_TOUCHES = "show_touches";

        @Override
        public boolean withAudio() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_WITH_AUDIO, false);
        }

        @Override
        public void setWithAudio(boolean value) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_WITH_AUDIO, value).apply();
        }

        @Override
        public boolean withCamera() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_WITH_CAMERA, false);
        }

        @Override
        public void setWithCamera(boolean value) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_WITH_CAMERA, value).apply();
        }

        @Override
        public int previewSize() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getInt(KEY_PREVIEW_SIZE, PreviewSize.SMALL);
        }

        @Override
        public void setPreviewSize(int size) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putInt(KEY_PREVIEW_SIZE, size).apply();
        }

        @Override
        public float resolutionScaleFactor() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getFloat(KEY_RES_FACTOR, 1f);
        }

        @Override
        public void setResolutionScaleFactor(float factor) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putFloat(KEY_RES_FACTOR, factor).apply();
        }

        @Override
        @TargetApi(Build.VERSION_CODES.M)
        public boolean setShowTouch(boolean show) {
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    Settings.System.putInt(Factory.get().getApplicationContext().getContentResolver(), SHOW_TOUCHES, show ? 1 : 0);
                    return true;
                }
                if (Settings.System.canWrite(Factory.get().getApplicationContext())) {
                    Settings.System.putInt(Factory.get().getApplicationContext().getContentResolver(), SHOW_TOUCHES, show ? 1 : 0);
                    return true;
                } else {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + Factory.get().getApplicationContext().getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Factory.get().getApplicationContext().startActivity(intent);
                    return false;
                }
            } catch (SecurityException | IllegalArgumentException ignored) {
            }
            return false;
        }

        @Override
        public boolean showTouch() {
            try {
                return Settings.System.getInt(Factory.get().getApplicationContext().getContentResolver(), SHOW_TOUCHES) == 1;
            } catch (Settings.SettingNotFoundException | SecurityException ignored) {
            }
            return false;
        }

        @Override
        public int preferredCamera() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getInt(KEY_PREDERRED_CAM, Camera.CameraInfo.CAMERA_FACING_FRONT);
        }

        @Override
        public void setPreferredCamera(int index) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putInt(KEY_PREDERRED_CAM, index).apply();
        }

        @Override
        public boolean hideAppWhenStart() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_HIDE_AUTO, false);
        }

        @Override
        public void setHideAppWhenStart(boolean hide) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_HIDE_AUTO, hide).apply();
        }

        @Override
        public long startDelay() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getLong(KEY_START_DELAY, 0);
        }

        @Override
        public void setStartDelay(long mills) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putLong(KEY_START_DELAY, mills).apply();
        }

        @Override
        public boolean showAD() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_SHOW_AD, true);
        }

        @Override
        public void setShowAD(boolean show) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_SHOW_AD, show).apply();
        }

        @Override
        public boolean firstStart() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_FIRST_START, true);
        }

        @Override
        public void setFirstStart(boolean first) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_FIRST_START, first).apply();
        }
    }
}
