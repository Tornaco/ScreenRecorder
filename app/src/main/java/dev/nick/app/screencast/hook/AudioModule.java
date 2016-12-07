package dev.nick.app.screencast.hook;

import android.content.pm.PackageManager;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AudioModule implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("android")) {
            Class AMSClz = XposedHelpers.findClass("com.android.server.am.ActivityManagerService", lpparam.classLoader);
            XposedBridge.log("AMS:" + AMSClz);
            XposedBridge.hookAllMethods(AMSClz, "checkPermission", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    int uid = (int) param.args[2];
                    if (uid <= 1000) return;
                    if ("android.permission.CAPTURE_AUDIO_OUTPUT".equals(param.args[0])) {
                        XposedBridge.log("beforeHookedMethod: Hooked AUDIO_OUTPUT permissions");
                        param.setResult(PackageManager.PERMISSION_GRANTED);
                    }
                }
            });
        }
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {

    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

    }
}
