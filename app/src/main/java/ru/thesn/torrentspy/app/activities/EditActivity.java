package ru.thesn.torrentspy.app.activities;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import ru.thesn.torrentspy.app.MyApplication;
import ru.thesn.torrentspy.app.R;
import ru.thesn.torrentspy.app.tools.Animations;
import ru.thesn.torrentspy.app.tools.BasicListAdapter;
import ru.thesn.torrentspy.app.tools.DataBaseHelper;
import ru.thesn.torrentspy.app.tools.StyledSnackBar;
import java.util.*;



public class EditActivity extends AppCompatActivity {

    private String type;
    private String mainID;

    private String temp = "";
    private boolean isEntryAvailable = true;

    private EditText enName;
    private EditText ruName;
    private EditText imageUrl;

    private EditText year;
    private EditText season;
    private EditText episode;
    private EditText serialStudios;
    private EditText releaseGroups;
    private EditText quality;

    private CheckBox goodSound;
    private CheckBox notification;
    private CheckBox ruLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getIntent().getStringExtra("type");
        mainID = getIntent().getStringExtra("id");
        String edit = "Редактирование ";
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        if (type.equals("movie")) {
            setContentView(R.layout.activity_edit_movie);
            temp = "фильма";
            toolbar.setTitle(edit + temp);
        }

        if (type.equals("game")){
            setContentView(R.layout.activity_edit_game);
            temp = "игры";
            toolbar.setTitle(edit + temp);
        }

        if (type.equals("serial")){
            setContentView(R.layout.activity_edit_serial);
            temp = "сериала";
            toolbar.setTitle(edit + temp);
        }

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        BasicListAdapter.Entity entity = MyApplication.adapters.get(type).getEntityByID(mainID);

        TextInputLayout enNameLayout = (TextInputLayout)findViewById(R.id.view);
        enName = new EditText(enNameLayout.getContext());
        enName.setHint("Название " + temp + " на английском");
        enName.setText(entity.getEnName());
        enNameLayout.addView(enName);

        TextInputLayout ruNameLayout = (TextInputLayout)findViewById(R.id.view2);
        ruName = new EditText(ruNameLayout.getContext());
        ruName.setHint("Название " + temp + " на русском");
        ruName.setText(entity.getRuName());
        ruNameLayout.addView(ruName);

        TextInputLayout imageUrlLayout = (TextInputLayout)findViewById(R.id.image_url);
        imageUrl = new EditText(imageUrlLayout.getContext());
        imageUrl.setHint("Ссылка на постер");
        imageUrl.setText(entity.getImageURL());
        imageUrlLayout.addView(imageUrl);



        notification = (CheckBox) findViewById(R.id.notification);
        ruLanguage = (CheckBox)findViewById(R.id.rulanguage);
        goodSound = (CheckBox)findViewById(R.id.goodsound);



        if (type.equals("serial")) {

            TextInputLayout seasonLayout = (TextInputLayout)findViewById(R.id.view3);
            season = new EditText(seasonLayout.getContext());
            season.setHint("Номер сезона");
            season.setInputType(InputType.TYPE_CLASS_NUMBER);
            season.setText(entity.getSeason());
            seasonLayout.addView(season);

            TextInputLayout episodeLayout = (TextInputLayout)findViewById(R.id.view4);
            episode = new EditText(episodeLayout.getContext());
            episode.setHint("Номер серии");
            episode.setInputType(InputType.TYPE_CLASS_NUMBER);
            episode.setText(entity.getEpisode());
            episodeLayout.addView(episode);

            TextInputLayout serialStudiosLayout = (TextInputLayout)findViewById(R.id.othergroops);
            serialStudios = new EditText(serialStudiosLayout.getContext());
            serialStudios.setHint("Студии озвучки (через запятую)");
            serialStudios.setText(entity.getResultStringFromList(entity.getSoundStudios()));
            serialStudiosLayout.addView(serialStudios);

        }
        else {
            TextInputLayout yearLayout = (TextInputLayout)findViewById(R.id.view3);
            year = new EditText(yearLayout.getContext());
            year.setHint("Год выхода");
            year.setInputType(InputType.TYPE_CLASS_NUMBER);
            if (type.equals("game")) year.setEms(10);
            year.setText(entity.getYear());
            yearLayout.addView(year);

            if (type.equals("movie")) {
                TextInputLayout qualityLayout = (TextInputLayout)findViewById(R.id.view4);
                quality = new EditText(qualityLayout.getContext());
                quality.setHint("Качество");
                quality.setText(entity.getQuality());
                qualityLayout.addView(quality);

                goodSound.setChecked(entity.getGoodSound() != null && entity.getGoodSound().equals("1"));
            }

            if (type.equals("game")) {
                TextInputLayout releaseGroupsLayout = (TextInputLayout)findViewById(R.id.othergroops);
                releaseGroups = new EditText(releaseGroupsLayout.getContext());
                releaseGroups.setHint("Релиз группы (через запятую)");
                releaseGroups.setText(entity.getResultStringFromList(entity.getReleaseGroups()));
                releaseGroupsLayout.addView(releaseGroups);

                ruLanguage.setChecked(entity.getRuLocale() != null && entity.getRuLocale().equals("1"));
            }

        }

        notification.setChecked(entity.getNotify() != null && entity.getNotify().equals("1"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
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
            case R.id.action_delete:
                Animations.hideKeyboard(getCurrentFocus());
                Snackbar snackbar = Snackbar.make(getCurrentFocus(), "Вы подтверждаете удаление?", Snackbar.LENGTH_LONG);
                snackbar.setAction("Ок", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        delete(true);
                        Animations.hideKeyboard(getCurrentFocus());
                    }
                });
                snackbar.setActionTextColor(Color.parseColor("#ffffff"));
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#f57c00"));
                snackbar.show();
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


    public boolean delete(boolean showAlerts){
        if (isEntryAvailable) {
            BasicListAdapter adapter = MyApplication.adapters.get(type);
            if (!MyApplication.dbHelper.isDbAvailable()){
                if (showAlerts) StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Подождите, пока завершится проверка!");
                return false;
            }
            adapter.deleteEntity(adapter.getLocation(adapter.getmData(), adapter.getEntityByID(mainID)));
            if (!MyApplication.dbHelper.delete(type, mainID)){
                if (showAlerts) StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Возникли ошибки!");
            }
            isEntryAvailable = false;
            adapter.notifyDataSetChanged();
            if (showAlerts) StyledSnackBar.showGreenSnackBar(getCurrentFocus(), "Удалено!");
            return true;
        } else {
            if (showAlerts) StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Вы уже удалили этот элемент!");
        }
        return false;
    }


    public void save(){
        if (!MyApplication.dbHelper.isDbAvailable()){
            StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Подождите, пока завершится проверка!");
            return;
        }

        if (!isEntryAvailable) {
            StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Невозможно изменить элемент!");
            return;
        }

        SQLiteDatabase db = MyApplication.dbHelper.getDb();

        String nameEn = enName.getText().toString().replaceAll(System.getProperty("line.separator"), " ");
        String nameRu = ruName.getText().toString().replaceAll(System.getProperty("line.separator"), " ");
        String image = imageUrl.getText().toString().replaceAll(System.getProperty("line.separator"), "");
        boolean notify = notification.isChecked();

        if (nameEn.equals("") && nameRu.equals("")) {
            StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Введите хотя бы одно название!");
            return;
        }

        ContentValues values = new ContentValues();
        values.put("name_en", nameEn);
        values.put("name_ru", nameRu);
        values.put("notify", notify);
        values.put("image_url", image);


        if (type.equals("movie")){
            String yearStr = year.getText().toString().replaceAll(System.getProperty("line.separator"), " ");

            values.put("year", yearStr);
            values.put("quality", quality.getText().toString());
            values.put("good_sound", goodSound.isChecked());

            long id = db.insert(DataBaseHelper.TABLE_MOVIES, null, values);


            if (id < 0) {
                StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Ошибка системы! Попробуйте позже.");
                return;
            } else {
                StyledSnackBar.showGreenSnackBar(getCurrentFocus(), "Сохранено!");
            }

            String query = "SELECT _id FROM " + DataBaseHelper.TABLE_MOVIES + " ORDER BY _id DESC LIMIT 1;";
            Cursor cursor = db.rawQuery(query, null);
            int movieID = 0;
            while (cursor.moveToNext())
                movieID = cursor.getInt(cursor.getColumnIndex("_id"));
            cursor.close();

            delete(false);
            MyApplication.adapters.get(type).addEntity(0, MyApplication.dbHelper.getMoviesDataFromDB(movieID).get(0));
            MyApplication.adapters.get(type).notifyDataSetChanged();
            isEntryAvailable = true;
            mainID = movieID + "";
        }



        if (type.equals("game")){
            String yearStr = year.getText().toString().replaceAll(System.getProperty("line.separator"), " ");

            values.put("year", yearStr);
            values.put("locale_ru", ruLanguage.isChecked());
            long id = db.insert(DataBaseHelper.TABLE_GAMES, null, values);
            if (id < 0) {
                StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Ошибка системы! Попробуйте позже.");
                return;
            }

            Set<String> set = new HashSet<>();
            String txt = releaseGroups.getText().toString();
            if (txt.length() > 2)
                set.addAll(Arrays.asList(txt.replaceAll(System.getProperty("line.separator"), " ").split(",")));

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
            delete(false);

            MyApplication.adapters.get(type).addEntity(0, MyApplication.dbHelper.getGamesDataFromDB(gameID).get(0));
            MyApplication.adapters.get(type).notifyDataSetChanged();
            isEntryAvailable = true;
            mainID = gameID + "";
        }



        if (type.equals("serial")){

            String s = season.getText().toString();
            String s2 = episode.getText().toString();

            if (s.charAt(0) == '0') s = s.substring(1, s.length());
            if (s2.charAt(0) == '0') s2 = s2.substring(1, s2.length());

            values.put("season", s);
            values.put("episode", s2);

            long id = db.insert(DataBaseHelper.TABLE_SERIALS, null, values);

            if (id < 0) {
                StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Ошибка системы! Попробуйте позже.");
                return;
            }

            Set<String> set = new HashSet<>();
            String txt = serialStudios.getText().toString();
            if (txt.length() > 2)
                set.addAll(Arrays.asList(txt.replaceAll(System.getProperty("line.separator"), " ").split(",")));

            String query = "SELECT _id FROM " + DataBaseHelper.TABLE_SERIALS + " ORDER BY _id DESC LIMIT 1;";
            Cursor cursor = db.rawQuery(query, null);
            int serialID = 0;
            while (cursor.moveToNext())
                serialID = cursor.getInt(cursor.getColumnIndex("_id"));
            cursor.close();

            for (String str: set) {
                values = new ContentValues();
                values.put("serial_id", serialID);
                values.put("name", str.trim());
                long id2 = db.insert(DataBaseHelper.TABLE_SERIALS_STUDIOS, null, values);
                if (id2 < 0) id = -1;
            }
            if (id < 0) {
                StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Ошибка системы! Сохранено частично.");
            } else {
                StyledSnackBar.showGreenSnackBar(getCurrentFocus(), "Сохранено!");
            }

            delete(false);
            MyApplication.adapters.get(type).addEntity(0, MyApplication.dbHelper.getSerialsDataFromDB(serialID).get(0));
            MyApplication.adapters.get(type).notifyDataSetChanged();
            isEntryAvailable = true;
            mainID = serialID + "";
        }



    }

}