package com.bill_prj;

import android.app.Application;
import android.content.Context;

public class BillApplication extends Application {

    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();

        // Global uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            String msg = "Uncaught in thread " + thread.getName() + ": "
                    + throwable.getClass().getName() + ": " + throwable.getMessage()
                    + "\n" + android.util.Log.getStackTraceString(throwable);
            android.util.Log.e("APP_CRASH", msg);
            writeCrashToFile(msg);
            // Re-throw to let Android handle it (show "App has stopped" dialog)
            if (thread == Thread.currentThread()) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }

    private void writeCrashToFile(String msg) {
        try {
            java.io.File file = new java.io.File(getFilesDir(), "crash_log.txt");
            java.io.FileOutputStream fos = openFileOutput("crash_log.txt", MODE_APPEND);
            fos.write((msg + "\n---\n").getBytes());
            fos.close();
        } catch (Exception ignored) {}
    }

    public static Context getAppContext() {
        return appContext;
    }
}
