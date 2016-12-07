package dev.nick.app.screencast.hook;

import java.util.Random;
import java.util.UUID;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class IdentifyModule implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (lpparam.packageName.endsWith("android")) {
            XposedBridge.log("Loading n-android");

            Class sysClz = XposedHelpers.findClass("android.provider.Settings.System", lpparam.classLoader);
            XposedBridge.log("sysClz:" + sysClz);

            XposedBridge.hookAllMethods(sysClz, "putString", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    XposedBridge.log("beforeHookedMethod, putString:" + param.args[1] + "-" + param.args[2]);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    XposedBridge.log("afterHookedMethod, putString");
                }
            });
        }

        if (lpparam.packageName.equals("dev.nick.app.screencast")) {
            XposedBridge.log("IdentifyModule: It's me!");

            Class bdClz = XposedHelpers.findClass("com.baidu.android.bba.common.util.DeviceId", lpparam.classLoader);
            XposedBridge.log("IdentifyModule, clz:" + bdClz);

            XposedBridge.hookAllMethods(bdClz, "getDeviceID", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    String id = UUID.randomUUID().toString();
                    XposedBridge.log("IdentifyModule, using id:" + id);
                    param.setResult(id);
                }
            });

            XposedBridge.hookAllMethods(bdClz, "getAndroidId", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    XposedBridge.log("IdentifyModule, getAndroidId: beforeHookedMethod:" + param.getResult());
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    XposedBridge.log("IdentifyModule, getAndroidId: afterHookedMethod:" + param.getResult());
                }
            });

            XposedBridge.hookAllMethods(bdClz, "getIMEI", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    String res = String.valueOf(new Random(100000).nextInt(Integer.MAX_VALUE - 1));
                    param.setResult(res);
                    XposedBridge.log("IdentifyModule, getIMEI: using imei:" + res);
                }
            });
        }

    }
}
