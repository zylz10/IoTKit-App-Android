package com.cylan.jiafeigou.n.base;

import android.app.Application;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.cylan.jiafeigou.n.engine.DaemonService;
import com.cylan.jiafeigou.support.DebugOptionsImpl;

import java.io.File;

/**
 * Created by hunt on 16-5-14.
 */
public class BaseApplication extends Application {

    private static final String TAG = "BaseApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        enableDebugOptions();
        Log.d(TAG, "application onCreate");
        startService(new Intent(this, DaemonService.class));
        startService(new Intent(this, FirstTaskInitService.class));
    }

    private void enableDebugOptions() {
        DebugOptionsImpl options = new DebugOptionsImpl("test");
        options.enableCrashHandler(this, Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "debug");
        options.enableStrictMode();
    }
}
