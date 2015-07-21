package ru.thesn.torrentspy.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import ru.thesn.torrentspy.app.download.DownloadService;
import ru.thesn.torrentspy.app.tools.BasicListAdapter;
import ru.thesn.torrentspy.app.tools.DataBaseHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Никита on 14.06.2015.
 */
public class MyApplication extends Application {
    public static DataBaseHelper dbHelper;
    public static Map<String, BasicListAdapter> adapters;
    public static boolean isReceiverAlive = false;
    public static Map<String, List<BasicListAdapter.Entity>> changedElements;
    public static final String APP_PREFERENCES = "mysettings";

    public static final String APP_PREFERENCES_BACKGROUND_CHECKS = "checks";
    public static final String APP_PREFERENCES_CHECK_AFTER_RESTART = "restart";
    public static final String APP_PREFERENCES_NUMBER_OF_TIMES = "count";
    public static final String APP_PREFERENCES_MY_SITES = "mysites";
    public static SharedPreferences mSettings;

    @Override
    public void onCreate() {
        super.onCreate();
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        dbHelper = new DataBaseHelper(getApplicationContext());
        adapters = new HashMap<>();
        changedElements = new HashMap<>();
        DownloadService.setDbHelper(dbHelper);
        Log.i("DEV_", "Create application!");
    }

    @Override
    public void onTrimMemory(int level) {
        Log.i("DEV_", "onTrimMemory");
        super.onTrimMemory(level);
    }

}