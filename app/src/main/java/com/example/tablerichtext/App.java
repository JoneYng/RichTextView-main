package com.example.tablerichtext;

import android.app.Application;

import org.scilab.forge.jlatexmath.core.AjLatexMath;

import java.util.concurrent.Callable;

import bolts.Task;

/**
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                AjLatexMath.init(getApplicationContext());
                return null;
            }
        });
    }
}
