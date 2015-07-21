package ru.thesn.torrentspy.app.tools;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;

public class StyledSnackBar {
    public static void showRedSnackBar(View v, String text){
        Snackbar snackbar = Snackbar.make(v, text, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.parseColor("#f57c00"));
        snackbar.show();
    }
    public static void showGreenSnackBar(View v, String text){
        Snackbar snackbar = Snackbar.make(v, text, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.parseColor("#00796b"));
        snackbar.show();
    }
}
