//package dev.nick.app.screencast.content;
//
//import android.Manifest;
//import android.os.AsyncTask;
//import android.os.Build;
//import android.support.annotation.RequiresApi;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.wandoujia.ads.sdk.Ads;
//
//import dev.nick.app.screencast.R;
//import dev.nick.app.screencast.provider.SettingsProvider;
//import dev.nick.logger.LoggerManager;
//import permissions.dispatcher.NeedsPermission;
//import permissions.dispatcher.RuntimePermissions;
//
//@RuntimePermissions
//public class WDJADActivity extends ScreenCastActivity {
//
//    private static final String APP_ID = "100045320";
//    private static final String SECRET_KEY = "1f65a47d478a67a97cdfa9363fe45e13";
//    private static final String BANNER = "450c79ac8289788a9b40c4e3df2636ca";
//
//    private View mBannerView;
//
//    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//    @NeedsPermission({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE})
//    public void showBanner() {
//
//        if (mBannerView != null && mBannerView.isAttachedToWindow()) {
//            LoggerManager.getLogger(WDJADActivity.class).debug("Skip dup ad.");
//            return;
//        }
//
//        new AsyncTask<Void, Void, Boolean>() {
//            @Override
//            protected Boolean doInBackground(Void... params) {
//                try {
//                    Ads.init(WDJADActivity.this, APP_ID, SECRET_KEY);
//                    return true;
//                } catch (Exception e) {
//                    LoggerManager.getLogger(WDJADActivity.class).error("Error load ad:" + e);
//                    return false;
//                }
//            }
//
//            @Override
//            protected void onPostExecute(Boolean success) {
//                final ViewGroup container = (ViewGroup) findViewById(R.id.adview_container);
//
//                if (success) {
//
//                    LoggerManager.getLogger(WDJADActivity.class).debug("AD init success");
//
//                    /**
//                     * pre load
//                     */
//                    Ads.preLoad(BANNER, Ads.AdFormat.banner);
//                    /**
//                     * add ad views
//                     */
//                    mBannerView = Ads.createBannerView(WDJADActivity.this, BANNER);
//                    container.addView(mBannerView, new ViewGroup.LayoutParams(
//                            ViewGroup.LayoutParams.MATCH_PARENT,
//                            ViewGroup.LayoutParams.WRAP_CONTENT));
//                } else {
//                    LoggerManager.getLogger(WDJADActivity.class).error("AD init failed");
//                }
//            }
//        }.execute();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        if (!mReadyToRun || SettingsProvider.get().firstStart()) return;
//
//        if (SettingsProvider.get().showAD()) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                WDJADActivityPermissionsDispatcher.showBannerWithCheck(WDJADActivity.this);
//            }
//        }
//    }
//}
