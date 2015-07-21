package ru.thesn.torrentspy.app.activities.add;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import ru.thesn.torrentspy.app.MyApplication;
import ru.thesn.torrentspy.app.R;
import ru.thesn.torrentspy.app.tools.Animations;
import ru.thesn.torrentspy.app.tools.DataBaseHelper;
import ru.thesn.torrentspy.app.tools.StyledSnackBar;

import java.util.*;


public class AddGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_game);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Добавление игры");
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
        String year = ((EditText) findViewById(R.id.year)).getText().toString().replaceAll(System.getProperty("line.separator"), " ");

        if (nameEn.equals("") && nameRu.equals("")) {
            StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Введите хотя бы одно название!");
            return;
        }

        ContentValues values = new ContentValues();
        values.put("name_en", nameEn);
        values.put("name_ru", nameRu);
        values.put("year", year);
        values.put("locale_ru", ((CheckBox)findViewById(R.id.rulanguage)).isChecked());
        values.put("notify", ((CheckBox)findViewById(R.id.notification)).isChecked());
        values.put("image_url", ((EditText) findViewById(R.id.image_url_text)).getText().toString());
        long id = db.insert(DataBaseHelper.TABLE_GAMES, null, values);

        if (id < 0) {
            StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Ошибка системы! Попробуйте позже.");
            return;
        }

        Set<String> set = new HashSet<>();
        if (((CheckBox)findViewById(R.id.rgmech)).isChecked()) set.add("R.G. Механики");
        if (((CheckBox)findViewById(R.id.steamgames)).isChecked()) set.add("R.G. Steamgames");
        if (((CheckBox)findViewById(R.id.catalyst)).isChecked()) set.add("R.G. Catalyst");
        if (((CheckBox)findViewById(R.id.xatab)).isChecked()) set.add("xatab");
        if (((CheckBox)findViewById(R.id.rggames)).isChecked()) set.add("R.G. Games");
        if (((CheckBox)findViewById(R.id.rgigromany)).isChecked()) set.add("R.G. Игроманы");
        String txt = ((EditText) findViewById(R.id.game_groops_text)).getText().toString();
        if (txt.length() > 2)
            set.addAll(Arrays.asList(txt.replaceAll(System.getProperty("line.separator"), " ").split(",")));
        Log.i("DEV_", set.toString());

        String query = "SELECT _id FROM " + DataBaseHelper.TABLE_GAMES + " ORDER BY _id DESC LIMIT 1;";
        Cursor cursor = db.rawQuery(query, null);
        int gameID = 0;
        while (cursor.moveToNext())
            gameID = cursor.getInt(cursor.getColumnIndex("_id"));
        cursor.close();

        for (String s: set) {
            values = new ContentValues();
            values.put("game_id", gameID);
            values.put("name", s.trim());
            long id2 = db.insert(DataBaseHelper.TABLE_GAMES_GROUPS, null, values);
            if (id2 < 0) id = -1;
        }
        if (id < 0) {
            StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Ошибка системы! Сохранено частично.");
        } else {
            StyledSnackBar.showGreenSnackBar(getCurrentFocus(), "Сохранено!");
        }
        MyApplication.adapters.get("game").addEntity(0, MyApplication.dbHelper.getGamesDataFromDB(gameID).get(0));
        MyApplication.adapters.get("game").notifyDataSetChanged();
        // TODO если во второй части передачи данных произошла ошибка, то отменить и первую часть (удалить строчку с номером id)
    }


}