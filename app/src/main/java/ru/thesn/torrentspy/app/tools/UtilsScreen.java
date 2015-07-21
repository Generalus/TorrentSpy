package ru.thesn.torrentspy.app.tools;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;


public class UtilsScreen {
    public static boolean isLargeTablet(Context context) {
        return ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
    }
    public static boolean isXLargeTablet(Context context) {
        return ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
    }

    private static String getScreenOrientation(Context context){
        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            return "PORTRAIT";
        else if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            return "LANDSCAPE";
        else
            return "UNKNOWN";
    }

    public static int getDisplayColumns(Activity activity) {
        int columnCount = 1;
        if (getScreenOrientation(activity).equals("LANDSCAPE"))
            columnCount = 2;
        if (isLargeTablet(activity)) {
            if (getScreenOrientation(activity).equals("LANDSCAPE"))
                columnCount = 3;
            else
                columnCount = 2;
        }
        if (isXLargeTablet(activity)) {
            if (getScreenOrientation(activity).equals("LANDSCAPE"))
                columnCount = 4;
            else
                columnCount = 3;
        }
        return columnCount;
    }
}
