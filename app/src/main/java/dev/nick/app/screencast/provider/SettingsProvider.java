package dev.nick.app.screencast.provider;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import java.util.Observable;

import dev.nick.app.screencast.BuildConfig;
import dev.nick.app.screencast.app.Factory;
import dev.nick.app.screencast.camera.PreviewSize;
import dev.nick.app.screencast.cast.CasterAudioSource;
import dev.nick.app.screencast.cast.Orientations;
import dev.nick.app.screencast.cast.ValidResolutions;
import dev.nick.logger.LoggerManager;

public abstract class SettingsProvider extends Observable {

    public static final int APP_VERSION_INT = BuildConfig.VERSION_CODE;
    public static final long START_DELAY_DEFAULT = 5000;

    public static final String STORAGE_MP4_FOLDER_NAME = "ScreenRecorder";
    public static final String STORAGE_GIF_FOLDER_NAME = "ScreenRecorder/gif";

    private static Impl sImpl;

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

    public abstract int audioSource();

    public abstract boolean setAudioSource(int source);

    public abstract boolean audioSourceNoRemind();

    public abstract void setAudioSourceNoRemind(boolean value);

    public abstract int resolutionIndex();

    public abstract void setResolutionIndex(int index);

    public abstract float resolutionScaleFactor();

    public abstract void setResolutionScaleFactor(float factor);

    public abstract int orientation();

    public abstract void setOrientation(int orientation);

    public abstract boolean setShowTouch(boolean show);

    public abstract boolean showTouch();

    public abstract int preferredCamera();

    public abstract void setPreferredCamera(int index);

    public abstract boolean hideAppWhenStart();

    public abstract void setHideAppWhenStart(boolean hide);

    public abstract long startDelay();

    public abstract void setStartDelay(long mills);

    public abstract boolean showCD();

    public abstract void setShowCD(boolean show);

    public abstract boolean showAD();

    public abstract void setShowAD(boolean show);

    public abstract boolean clickAD();

    public abstract void setClickAD(boolean click);

    public abstract boolean firstStart();

    public abstract void setFirstStart(boolean first);

    public abstract boolean soundEffect();

    public abstract void setSoundEffect(boolean use);

    public abstract boolean shakeAction();

    public abstract void setShakeAction(boolean use);

    public abstract int getAppVersionNum();

    private static class Impl extends SettingsProvider {
        // Keys in Settings.
        private static final String KEY_FIRST_START = "settings.first.start";
        private static final String KEY_WITH_AUDIO = "settings.with.audio";
        private static final String KEY_WITH_CAMERA = "settings.with.camera";
        private static final String KEY_PREVIEW_SIZE = "settings.preview.size";
        private static final String KEY_AUDIO_SOURCE = "settings.audio.source";
        private static final String KEY_AUDIO_SOURCE_NO_REMIND = "settings.audio.source.no.remind";
        private static final String KEY_RES_FACTOR = "settings.res.factor";
        private static final String KEY_RES_INDEX = "settings.res.index";
        private static final String KEY_ORIENTATION = "settings.orientation";
        private static final String KEY_PREFERRED_CAM = "settings.preferred.cam";
        private static final String KEY_HIDE_AUTO = "settings.hide.app.auto";
        private static final String KEY_START_DELAY = "settings.start.delay";
        private static final String KEY_SHOW_COUNTDOWN = "settings.show.countdown";
        private static final String KEY_SHOW_AD = "settings.show.ad";
        private static final String KEY_CLICK_AD = "settings.click.ad";
        private static final String KEY_SOUND_EFFECT = "settings.sound.effect";
        private static final String KEY_SHAKE_ACTION = "settings.shake.action";
        private static final String KEY_APP_VERSION = "settings.app.code";
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
        public int audioSource() {

            if (!hasXposed()) {
                LoggerManager.getLogger(getClass()).error("No Xposed installed, r_submix is not available");
            }

            int s = PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getInt(KEY_AUDIO_SOURCE, CasterAudioSource.MIC);
            LoggerManager.getLogger(getClass()).verbose("Returning source:" + s);
            return s;
        }

        private boolean hasXposed() {
            String packageName = "de.robv.android.xposed.installer";
            try {
                Factory.get().getApplicationContext().getPackageManager().getApplicationInfo(
                        packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
                LoggerManager.getLogger(getClass()).debug("Xpoded installer deteced!");
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }

        @Override
        public boolean setAudioSource(int source) {
            LoggerManager.getLogger(getClass()).verbose("set source:" + source + Log.getStackTraceString(new Throwable()));
            if (!hasXposed()) return false;
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putInt(KEY_AUDIO_SOURCE, source).apply();
            return true;
        }

        @Override
        public boolean audioSourceNoRemind() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_AUDIO_SOURCE_NO_REMIND, false);
        }

        @Override
        public void setAudioSourceNoRemind(boolean value) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_AUDIO_SOURCE_NO_REMIND, value).apply();
        }

        @Override
        public int resolutionIndex() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getInt(KEY_RES_INDEX, ValidResolutions.INDEX_MASK_AUTO);
        }

        @Override
        public void setResolutionIndex(int index) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putInt(KEY_RES_INDEX, index).apply();
        }

        @Override
        public float resolutionScaleFactor() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getFloat(KEY_RES_FACTOR, 0.5f);
        }

        @Override
        public void setResolutionScaleFactor(float factor) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putFloat(KEY_RES_FACTOR, factor).apply();
        }

        @Override
        public int orientation() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getInt(KEY_ORIENTATION, Orientations.AUTO);
        }

        @Override
        public void setOrientation(int orientation) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putInt(KEY_ORIENTATION, orientation).apply();
        }

        @Override
        @TargetApi(Build.VERSION_CODES.M)
        public boolean setShowTouch(boolean show) {
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//                    Settings.System.putInt(Factory.get().getApplicationContext().getContentResolver(), SHOW_TOUCHES, show ? 1 : 0);
                    return true;
                }
//                if (Settings.System.canWrite(Factory.get().getApplicationContext())) {
//                    Settings.System.putInt(Factory.get().getApplicationContext().getContentResolver(), SHOW_TOUCHES, show ? 1 : 0);
//                    return true;
//                } else {
//                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
//                    intent.setData(Uri.parse("package:" + Factory.get().getApplicationContext().getPackageName()));
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    Factory.get().getApplicationContext().startActivity(intent);
//                    return false;
//                }
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
                    .getInt(KEY_PREFERRED_CAM, Camera.CameraInfo.CAMERA_FACING_FRONT);
        }

        @Override
        public void setPreferredCamera(int index) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putInt(KEY_PREFERRED_CAM, index).apply();
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
        public boolean showCD() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_SHOW_COUNTDOWN, true);
        }

        @Override
        public void setShowCD(boolean show) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_SHOW_COUNTDOWN, show).apply();
        }

        @Override
        public boolean showAD() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_SHOW_AD, true);
        }

        @Override
        public void setShowAD(boolean show) {
            if (!show && !clickAD()) return;
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_SHOW_AD, show).apply();
        }

        @Override
        public boolean clickAD() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_CLICK_AD, false);
        }

        @Override
        public void setClickAD(boolean click) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_CLICK_AD, click).apply();
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

        @Override
        public boolean soundEffect() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_SOUND_EFFECT, true);
        }

        @Override
        public void setSoundEffect(boolean use) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_SOUND_EFFECT, use).apply();
        }

        @Override
        public boolean shakeAction() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_SHAKE_ACTION, true);
        }

        @Override
        public void setShakeAction(boolean use) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_SHAKE_ACTION, use).apply();
        }

        @Override
        public int getAppVersionNum() {
            int code = PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getInt(KEY_APP_VERSION, -1);
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putInt(KEY_APP_VERSION, APP_VERSION_INT).apply();
            return code;
        }
    }
}
