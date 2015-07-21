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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import ru.thesn.torrentspy.app.MyApplication;
import ru.thesn.torrentspy.app.R;
import ru.thesn.torrentspy.app.tools.Animations;
import ru.thesn.torrentspy.app.tools.DataBaseHelper;
import ru.thesn.torrentspy.app.tools.StyledSnackBar;


public class AddMovieActivity extends AppCompatActivity {

    private RadioGroup rg1;
    private RadioGroup rg2;
    private RadioGroup.OnCheckedChangeListener listener1 = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                rg2.setOnCheckedChangeListener(null);
                rg2.clearCheck(); // clear the second RadioGroup!
                rg2.setOnCheckedChangeListener(listener2); //reset the listener
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener listener2 = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                rg1.setOnCheckedChangeListener(null);
                rg1.clearCheck();
                rg1.setOnCheckedChangeListener(listener1);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Добавление фильма");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        rg1 = (RadioGroup) findViewById(R.id.rg1);
        rg2 = (RadioGroup) findViewById(R.id.rg2);
        rg1.clearCheck(); // this is so we can start fresh, with no selection on both RadioGroups
        rg2.clearCheck();
        rg1.check(R.id.thebestquality);
        rg1.setOnCheckedChangeListener(listener1);
        rg2.setOnCheckedChangeListener(listener2);
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
        String year = ((EditText) findViewById(R.id.year)).getText().toString().replaceAll(System.getProperty("line.separator"), " ");

        if (nameEn.equals("") && nameRu.equals("")) {
            StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Введите хотя бы одно название!");
            return;
        }

        int rgID = 0;
        if (rg1.getCheckedRadioButtonId() != -1) rgID = rg1.getCheckedRadioButtonId(); else
            if (rg2.getCheckedRadioButtonId() != -1) rgID = rg2.getCheckedRadioButtonId();


        ContentValues values = new ContentValues();
        values.put("name_en", nameEn);
        values.put("name_ru", nameRu);
        values.put("year", year);
        values.put("quality", ((RadioButton)findViewById(rgID)).getText().toString());
        values.put("good_sound", ((CheckBox)findViewById(R.id.goodsound)).isChecked());
        values.put("notify", ((CheckBox)findViewById(R.id.notification)).isChecked());
        values.put("image_url", ((EditText) findViewById(R.id.image_url_text)).getText().toString());
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
        MyApplication.adapters.get("movie").addEntity(0, MyApplication.dbHelper.getMoviesDataFromDB(movieID).get(0));
        MyApplication.adapters.get("movie").notifyDataSetChanged();
        // TODO если во второй части передачи данных произошла ошибка, то отменить и первую часть (удалить строчку с номером id)
    }


}
