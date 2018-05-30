package com.example.ckc.androidplugindemo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.ckc.androidplugindemo.utils.BundlerResourceLoader;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    public Resources mBundleResources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        findViewById(R.id.text2).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
////                finish();
//            }
//        });


        inject(MyApplication.mClassLoader);
        Class clazz = null;
        try {
            clazz = MyApplication.mClassLoader.loadClass("com.example.ckc.maotaiorder.SecondActivity");

            Method repay2 = null;//得到方法对象,有参的方法需要指定参数类型
            repay2 = clazz.getMethod("setView", View.class);

            int id = mBundleResources.getIdentifier("activity_second", "layout", "com.example.ckc.maotaiorder");
            System.out.println("resources id=" + id);
            repay2.invoke(clazz.newInstance(), LayoutInflater.from(MainActivity.this.getBaseContext()).inflate(id,null));//
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(MainActivity.this, clazz);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        mBundleResources = BundlerResourceLoader.getBundleResource(newBase.getApplicationContext());
        replaceContextResources(newBase);
        super.attachBaseContext(newBase);
    }

    /**
     * 使用反射的方式，使用Bundle的Resource对象，替换Context的mResources对象
     *
     * @param context
     */
    public void replaceContextResources(Context context) {
        try {
            Field field = context.getClass().getDeclaredField("mResources");
            field.setAccessible(true);
            field.set(context, mBundleResources);
            System.out.println("debug:repalceResources succ");
        } catch (Exception e) {
            System.out.println("debug:repalceResources error");
            e.printStackTrace();
        }
    }

    private void inject(DexClassLoader loader) {

        PathClassLoader pathLoader = (PathClassLoader) getClassLoader();
        try {
            Object dexElements = combineArray(
                    getDexElements(getPathList(pathLoader)),
                    getDexElements(getPathList(loader)));
            Object pathList = getPathList(pathLoader);
            setField(pathList, pathList.getClass(), "dexElements", dexElements);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Object getPathList(Object baseDexClassLoader)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        ClassLoader bc = (ClassLoader) baseDexClassLoader;
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    private static Object getField(Object obj, Class<?> cl, String field)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }

    private static Object getDexElements(Object paramObject)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        return getField(paramObject, paramObject.getClass(), "dexElements");
    }

    private static void setField(Object obj, Class<?> cl, String field,
                                 Object value) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {

        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        localField.set(obj, value);
    }

    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> localClass = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);
        int j = i + Array.getLength(arrayRhs);
        Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }
        return result;
    }
}
