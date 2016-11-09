package dev.nick.app.screencast.app;

import android.app.Application;
import android.util.Log;

import dev.nick.app.screencast.BuildConfig;
import dev.nick.logger.LoggerManager;

public class ScreencastApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(Factory.get());
        Factory.get().onApplicationCreate(this);
        LoggerManager.setDebugLevel(BuildConfig.DEBUG ? Log.VERBOSE : Log.WARN);
        LoggerManager.setTagPrefix(ScreencastApp.class.getSimpleName());
    }
}
