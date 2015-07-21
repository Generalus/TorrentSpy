package ru.thesn.torrentspy.app.tools;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;


public class Animations {
    public static void disappearance(final View view){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                // get the center for the clipping circle
                int cx = (view.getLeft() + view.getRight()) / 4;
                int cy = (view.getTop() + view.getBottom()) / 4;

                // get the initial radius for the clipping circle
                int initialRadius = view.getWidth();

                // create the animation (the final radius is zero)

                Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);
                // ВСЕ ЧТО НИЖЕ ЛОЛИПОПА ВАЛИТСЯ ТУТ (НЕТ КЛАССА), поэтому проверяем версию андроида в начале метода

                // make the view invisible when the animation is done
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.GONE);
                    }
                });

                // start the animation
                anim.start();
            }catch (Exception e){
                Log.w("DEV", "Animation error");}
        } else {
            view.setVisibility(View.GONE);
        }
    }

    public static void hideKeyboard(View view){
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
