package com.example.ckc.androidplugindemo;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Environment;
import android.util.ArrayMap;
import android.util.Log;

import com.example.ckc.androidplugindemo.utils.ReflectHelper;

import java.io.File;
import java.lang.ref.WeakReference;

import dalvik.system.DexClassLoader;

/**
 * Created by ckc on 18-5-29.
 */

public class MyApplication extends Application{

    public static final String TAG = "MyApplication";
    public static final String AppName = "maotaiorder.apk";
    public static int i = 0;

    public static DexClassLoader mClassLoader;


    public static final String apkPath= Environment.getExternalStorageDirectory().getPath()  + File.separator+ AppName;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "替换之前系统的classLoader");
        showClassLoader();
        try {
            String cachePath = this.getCacheDir().getAbsolutePath();
//            String apkPath = Environment.getExternalStorageDirectory().getPath()  + File.separator+ AppName;
            mClassLoader = new DexClassLoader(apkPath, cachePath, cachePath, getClassLoader());
//            loadApkClassLoader(mClassLoader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "替换之后系统的classLoader");
        showClassLoader();
    }


    @SuppressLint("NewApi")
    public void loadApkClassLoader(DexClassLoader loader) {
        try {
            Object currentActivityThread  = ReflectHelper.invokeMethod("android.app.ActivityThread", "currentActivityThread", new Class[] {},new Object[] {});
            String packageName = this.getPackageName();
            ArrayMap mpackages = (ArrayMap) ReflectHelper.getField("android.app.ActivityThread", "mPackages", currentActivityThread);
            WeakReference wr= (WeakReference)mpackages.get(packageName);
            Log.e(TAG, "mClassLoader:" + wr.get());
            ReflectHelper.setField("android.app.LoadedApk", "mClassLoader", wr.get(), loader);
            Log.e(TAG, "load:" + loader);
        } catch (Exception e) {
            Log.e(TAG, "load apk classloader error:" + Log.getStackTraceString(e));
        }
    }


    /**
     * 打印系统的classLoader
     */
    public void showClassLoader() {
        ClassLoader classLoader = getClassLoader();
        if (classLoader != null){
            Log.i(TAG, "[onCreate] classLoader " + i + " : " + classLoader.toString());
            while (classLoader.getParent()!=null){
                classLoader = classLoader.getParent();
                Log.i(TAG,"[onCreate] classLoader " + i + " : " + classLoader.toString());
                i++;
            }
        }
    }
}
