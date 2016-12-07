package dev.nick.app.screencast.content;

import android.Manifest;
import android.view.ViewGroup;

import com.baidu.appx.BDBannerAd;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.logger.LoggerManager;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class BDADActivity extends DrawerScreencastActivity {

    private static final String SDK_APP_KEY = "4w9zhqeiYE4wWEAnPMpknZOee1nqNiRW";
    private static final String SDK_BANNER_AD_ID = "Twys91rl9b6haXIEQ5BVAdsO";

    private BDBannerAd bannerview;

    @NeedsPermission({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE})
    public void showBanner() {
        if (null == bannerview) {
            bannerview = new BDBannerAd(this, SDK_APP_KEY, SDK_BANNER_AD_ID);
            bannerview.setAdSize(BDBannerAd.SIZE_FULL_FLEXIBLE);
            bannerview.setAdListener(new BDBannerAd.BannerAdListener() {
                @Override
                public void onAdvertisementDataDidLoadSuccess() {
                    LoggerManager.getLogger(getClass()).funcEnter();
                }

                @Override
                public void onAdvertisementDataDidLoadFailure() {
                    LoggerManager.getLogger(getClass()).funcEnter();
                }

                @Override
                public void onAdvertisementViewDidShow() {
                    LoggerManager.getLogger(getClass()).funcEnter();
                }

                @Override
                public void onAdvertisementViewDidClick() {
                    LoggerManager.getLogger(getClass()).funcEnter();
                    SettingsProvider.get().setClickAD(true);
                }

                @Override
                public void onAdvertisementViewWillStartNewIntent() {
                    LoggerManager.getLogger(getClass()).funcEnter();
                }
            });
            ViewGroup container = (ViewGroup) findViewById(R.id.adview_container);
            container.addView(bannerview);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mReadyToRun || SettingsProvider.get().firstStart()) return;
        if (SettingsProvider.get().showAD()) {
            BDADActivityPermissionsDispatcher.showBannerWithCheck(BDADActivity.this);
        }
    }
}
