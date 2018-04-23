package com.demo.mybutterknife.lib;

import android.app.Activity;
import android.view.View;

import com.demo.mybutterknife.lib.annotation.Unbinder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class MyButterKnife {

    public static Unbinder bindViews(Activity act){
        View view = act.getWindow().getDecorView();
        String className = act.getClass().getName();
        try {
            Class<?> aptClass = act.getClassLoader().loadClass(className+"_ViewBinding");
            Constructor<? extends Unbinder> constructor = (Constructor<? extends Unbinder>)aptClass.getConstructor(act.getClass(), View.class);
            return constructor.newInstance(act, view);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}
