package ru.thesn.torrentspy.app.activities;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import ru.thesn.torrentspy.app.MyApplication;
import ru.thesn.torrentspy.app.R;
import ru.thesn.torrentspy.app.tools.StyledSnackBar;


public class AppConfigActivity extends AppCompatActivity {

    private Switch autoCheck;
    private Switch afterRestart;
    private EditText checkCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Настройки приложения");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        autoCheck = (Switch)findViewById(R.id.autocheck);
        afterRestart = (Switch)findViewById(R.id.check_after_restart);

        TextInputLayout checkCountLayout = (TextInputLayout)findViewById(R.id.check_count_layout);
        checkCount = new EditText(checkCountLayout.getContext());
        checkCount.setHint("Количество проверок за день");
        checkCount.setInputType(InputType.TYPE_CLASS_NUMBER);
        checkCount.setEms(10);
        checkCount.setText(MyApplication.mSettings.getInt(MyApplication.APP_PREFERENCES_NUMBER_OF_TIMES, 1) + "");
        checkCountLayout.addView(checkCount);

        autoCheck.setChecked(MyApplication.mSettings.getBoolean(MyApplication.APP_PREFERENCES_BACKGROUND_CHECKS, true));
        afterRestart.setChecked(MyApplication.mSettings.getBoolean(MyApplication.APP_PREFERENCES_CHECK_AFTER_RESTART, true));


        if (!autoCheck.isChecked()) {
            afterRestart.setEnabled(false);
            checkCount.setEnabled(false);
        }

        autoCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                afterRestart.setEnabled(isChecked);
                checkCount.setEnabled(isChecked);
            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // если убрать android, то кнопка "назад" работать перестанет =)
                new SafeExit().execute();
                break;
            case R.id.action_save:
                hideKeyboard();
                save();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class SafeExit extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            hideKeyboard();
            try {Thread.sleep(150);} catch (Exception e){e.printStackTrace();}
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            onBackPressed();
        }
    }

    public void save(){
        int count = -1;
        try {
            count = Integer.parseInt(checkCount.getText().toString());
        }catch(Exception e){e.printStackTrace();}

        if (count == -1) {
            StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Ошибка распознания числа");
            return;
        }
        if (count <= 0){
            StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Задайте положительное число");
            return;
        }
        if (count > 50){
            StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Нельзя так часто выполнять проверку!");
            return;
        }


        SharedPreferences.Editor editor = MyApplication.mSettings.edit();

        editor.putBoolean(MyApplication.APP_PREFERENCES_BACKGROUND_CHECKS, autoCheck.isChecked());
        editor.putBoolean(MyApplication.APP_PREFERENCES_CHECK_AFTER_RESTART, afterRestart.isChecked() && autoCheck.isChecked());
        editor.putInt(MyApplication.APP_PREFERENCES_NUMBER_OF_TIMES, count);

        editor.apply();

        StyledSnackBar.showGreenSnackBar(getCurrentFocus(), "Настройки успешно сохранены!");

    }

    public void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
