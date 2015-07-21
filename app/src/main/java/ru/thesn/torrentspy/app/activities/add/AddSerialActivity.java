package ru.thesn.torrentspy.app.activities.add;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import ru.thesn.torrentspy.app.MyApplication;
import ru.thesn.torrentspy.app.R;
import ru.thesn.torrentspy.app.tools.Animations;
import ru.thesn.torrentspy.app.tools.DataBaseHelper;
import ru.thesn.torrentspy.app.tools.StyledSnackBar;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class AddSerialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_serial);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Добавление сериала");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                Animations.hideKeyboard(getCurrentFocus());
                save();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class SafeExit extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Animations.hideKeyboard(getCurrentFocus());
            try {Thread.sleep(150);} catch (Exception e){e.printStackTrace();}
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            onBackPressed();
        }
    }

    public void save(){
        SQLiteDatabase db = MyApplication.dbHelper.getDb();

        if (!MyApplication.dbHelper.isDbAvailable()){
            StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Подождите, пока завершится проверка!");
            return;
        }

        String nameEn = ((EditText) findViewById(R.id.name_en)).getText().toString().replaceAll(System.getProperty("line.separator"), " ");
        String nameRu = ((EditText) findViewById(R.id.name_ru)).getText().toString().replaceAll(System.getProperty("line.separator"), " ");
        String season = ((EditText) findViewById(R.id.edit_season)).getText().toString();
        String episode = ((EditText) findViewById(R.id.edit_episode)).getText().toString();

        if (nameEn.equals("") && nameRu.equals("")) {
            StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Введите хотя бы одно название!");
            return;
        }

        if (season.equals("") || episode.equals("")) {
            StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Укажите сезон и эпизод!");
            return;
        }

        ContentValues values = new ContentValues();
        values.put("name_en", nameEn);
        values.put("name_ru", nameRu);
        values.put("season", season);
        values.put("episode", episode);
        values.put("notify", ((CheckBox)findViewById(R.id.notification)).isChecked());
        values.put("image_url", ((EditText) findViewById(R.id.image_url_text)).getText().toString());
        long id = db.insert(DataBaseHelper.TABLE_SERIALS, null, values);

        if (id < 0) {
            StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Ошибка системы! Попробуйте позже.");
            return;
        }

        Set<String> set = new HashSet<>();
        if (((CheckBox)findViewById(R.id.LostFilm)).isChecked()) set.add("LostFilm");
        if (((CheckBox)findViewById(R.id.AlexFilm)).isChecked()) set.add("AlexFilm");
        if (((CheckBox)findViewById(R.id.NewStudio)).isChecked()) set.add("NewStudio");
        if (((CheckBox)findViewById(R.id.fox)).isChecked()) set.add("FOX");
        if (((CheckBox)findViewById(R.id.baibako)).isChecked()) set.add("Baibak&Ko");
        if (((CheckBox)findViewById(R.id.kerob)).isChecked()) set.add("Kerob");
        String txt = ((EditText) findViewById(R.id.serial_other_studios)).getText().toString();
        if (txt.length() > 2)
            set.addAll(Arrays.asList(txt.replaceAll(System.getProperty("line.separator"), " ").split(",")));

        String query = "SELECT _id FROM " + DataBaseHelper.TABLE_SERIALS + " ORDER BY _id DESC LIMIT 1;";
        Cursor cursor = db.rawQuery(query, null);
        int serialID = 0;
        while (cursor.moveToNext())
            serialID = cursor.getInt(cursor.getColumnIndex("_id"));
        cursor.close();

        for (String s: set) {
            values = new ContentValues();
            values.put("serial_id", serialID);
            values.put("name", s.trim());
            long id2 = db.insert(DataBaseHelper.TABLE_SERIALS_STUDIOS, null, values);
            if (id2 < 0) id = -1;
        }
        if (id < 0) {
            StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Ошибка системы! Сохранено частично.");
        } else {
            StyledSnackBar.showGreenSnackBar(getCurrentFocus(), "Сохранено!");
        }
        MyApplication.adapters.get("serial").addEntity(0, MyApplication.dbHelper.getSerialsDataFromDB(serialID).get(0));
        MyApplication.adapters.get("serial").notifyDataSetChanged();
        // TODO если во второй части передачи данных произошла ошибка, то отменить и первую часть (удалить строчку с номером id)
    }

}