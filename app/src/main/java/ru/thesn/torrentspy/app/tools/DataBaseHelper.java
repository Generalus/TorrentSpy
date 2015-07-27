package ru.thesn.torrentspy.app.tools;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class DataBaseHelper extends SQLiteOpenHelper implements BaseColumns {

    private SQLiteDatabase db;
    private boolean isDbAvailable = true;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "torrent_spy";
    public static final String TABLE_MOVIES = "movies";
    public static final String TABLE_GAMES = "games";
    public static final String TABLE_GAMES_GROUPS = "games_release_groups";
    public static final String TABLE_SERIALS = "serials";
    public static final String TABLE_SERIALS_STUDIOS = "serials_translate_studios";


    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = getWritableDatabase();
    }

    @Override
    public synchronized void close() {
        db.close();
        super.close();
    }

    @Override
    public synchronized void onCreate(SQLiteDatabase db) {
        String CREATE_GAMES_TABLE = "CREATE TABLE " + TABLE_GAMES + " (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, name_en TEXT," +
                " name_ru TEXT, description TEXT, year TEXT, locale_ru TEXT, notify TEXT, status TEXT, image_url TEXT); ";
        String CREATE_GAMES_GROUPS_TABLE = "CREATE TABLE " + TABLE_GAMES_GROUPS + " (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "game_id INTEGER NOT NULL, name TEXT NOT NULL); ";
        String CREATE_MOVIES_TABLE = "CREATE TABLE " + TABLE_MOVIES + " (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, name_en TEXT," +
                " name_ru TEXT, description TEXT, quality TEXT, year TEXT, good_sound TEXT," +
                " notify TEXT, status TEXT, image_url TEXT); ";
        String CREATE_SERIALS_TABLE = "CREATE TABLE " + TABLE_SERIALS + " ( _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                " name_en TEXT, name_ru TEXT, description TEXT, season TEXT, episode TEXT, notify TEXT, status TEXT, image_url TEXT" +
                "); ";
        String CREATE_SERIALS_STUDIOS_TABLE = "CREATE TABLE " + TABLE_SERIALS_STUDIOS + " (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                " serial_id INTEGER NOT NULL, name TEXT NOT NULL); ";

        db.execSQL(CREATE_GAMES_TABLE);
        db.execSQL(CREATE_GAMES_GROUPS_TABLE);
        db.execSQL(CREATE_MOVIES_TABLE);
        db.execSQL(CREATE_SERIALS_TABLE);
        db.execSQL(CREATE_SERIALS_STUDIOS_TABLE);

    }

    @Override
    public synchronized void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String s = "DROP TABLE IF EXISTS ";
        db.execSQL(s + TABLE_MOVIES + "; ");
        db.execSQL(s + TABLE_GAMES + "; ");
        db.execSQL(s + TABLE_GAMES_GROUPS + "; ");
        db.execSQL(s + TABLE_SERIALS + "; ");
        db.execSQL(s + TABLE_SERIALS_STUDIOS + "; ");
        onCreate(db);
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public boolean isDbAvailable() {
        return isDbAvailable;
    }

    public void setIsDbAvailable(boolean isDbAvailable) {
        this.isDbAvailable = isDbAvailable;
    }

    public void changeStatus(BasicListAdapter.Entity entity, String status){
        String id = entity.getId();
        String table = "";

        if (entity.getType().equals("game")) table = TABLE_GAMES;
        if (entity.getType().equals("movie")) table = TABLE_MOVIES;
        if (entity.getType().equals("serial")) table = TABLE_SERIALS;

        if (!table.equals(""))
            db.execSQL("UPDATE "+table+" SET status='"+status+"' WHERE _id = "+id+";");
    }

    public void addImageURL (BasicListAdapter.Entity entity, String url){
        String id = entity.getId();
        String table = "";

        if (entity.getType().equals("game")) table = TABLE_GAMES;
        if (entity.getType().equals("movie")) table = TABLE_MOVIES;
        if (entity.getType().equals("serial")) table = TABLE_SERIALS;

        if (!table.equals(""))
            db.execSQL("UPDATE "+table+" SET image_url='"+url+"' WHERE _id = "+id+";");
    }


    public synchronized List<BasicListAdapter.Entity> getGamesDataFromDB(Integer number){
        List<BasicListAdapter.Entity> data = new ArrayList<>();
        Cursor cursor = db.query(DataBaseHelper.TABLE_GAMES, new String[]{
                        "_id", "name_en", "name_ru", "description", "year", "locale_ru", "notify", "status", "image_url"},
                null, null, null, null, null
        );
        Cursor cursor2 = db.query(DataBaseHelper.TABLE_GAMES_GROUPS, new String[]{
                        "game_id", "name"},
                null, null, null, null, null
        );
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex("_id"));
            if (number != null && !id.equals(number + "")) continue;
            String nameEn = cursor.getString(cursor.getColumnIndex("name_en"));
            String nameRu = cursor.getString(cursor.getColumnIndex("name_ru"));
            String description = cursor.getString(cursor.getColumnIndex("description"));
            String year = cursor.getString(cursor.getColumnIndex("year"));
            String localeRu = cursor.getString(cursor.getColumnIndex("locale_ru"));
            String notify = cursor.getString(cursor.getColumnIndex("notify"));
            String status = cursor.getString(cursor.getColumnIndex("status"));
            String imageURL = cursor.getString(cursor.getColumnIndex("image_url"));

            List<String> list = new ArrayList<>();

            cursor2.moveToFirst();
            while (cursor2.moveToNext()){
                String gID = cursor2.getString(cursor2.getColumnIndex("game_id"));
                if (gID.equals(id)) list.add(cursor2.getString(cursor2.getColumnIndex("name")));
            }

            BasicListAdapter.Entity entity = new BasicListAdapter.Entity(id);
            entity.setEnName(nameEn);
            entity.setRuName(nameRu);
            entity.setDescription(description);
            entity.setYear(year);
            entity.setRuLocale(localeRu);
            entity.setNotify(notify);
            entity.setReleaseGroups(list);
            entity.setStatus(status);
            entity.setImageURL(imageURL);
            entity.setType("game");
            data.add(entity);
        }
        cursor.close();
        cursor2.close();

        Collections.reverse(data);
        return data;
    }

    public synchronized List<BasicListAdapter.Entity> getMoviesDataFromDB(Integer number){
        List<BasicListAdapter.Entity> data = new ArrayList<>();
        Cursor cursor = db.query(DataBaseHelper.TABLE_MOVIES, new String[]{
                        "_id", "name_en", "name_ru", "description", "year", "quality", "good_sound", "status", "notify", "image_url"},
                null, null, null, null, null
        );

        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex("_id"));
            if (number != null && !id.equals(number + "")) continue;
            String nameEn = cursor.getString(cursor.getColumnIndex("name_en"));
            String nameRu = cursor.getString(cursor.getColumnIndex("name_ru"));
            String description = cursor.getString(cursor.getColumnIndex("description"));
            String year = cursor.getString(cursor.getColumnIndex("year"));
            String quality = cursor.getString(cursor.getColumnIndex("quality"));
            String goodSound = cursor.getString(cursor.getColumnIndex("good_sound"));
            String status = cursor.getString(cursor.getColumnIndex("status"));
            String notify = cursor.getString(cursor.getColumnIndex("notify"));
            String imageURL = cursor.getString(cursor.getColumnIndex("image_url"));

            BasicListAdapter.Entity entity = new BasicListAdapter.Entity(id);
            entity.setEnName(nameEn);
            entity.setRuName(nameRu);
            entity.setDescription(description);
            entity.setYear(year);
            entity.setGoodSound(goodSound);
            entity.setQuality(quality);
            entity.setNotify(notify);
            entity.setStatus(status);
            entity.setImageURL(imageURL);
            entity.setType("movie");
            data.add(entity);
        }
        cursor.close();
        Collections.reverse(data);
        return data;
    }

    public synchronized List<BasicListAdapter.Entity> getSerialsDataFromDB(Integer number){
        List<BasicListAdapter.Entity> data = new ArrayList<>();
        Cursor cursor = db.query(DataBaseHelper.TABLE_SERIALS, new String[]{
                        "_id", "name_en", "name_ru", "description", "season", "episode", "notify", "status", "image_url"},
                null, null, null, null, null
        );
        Cursor cursor2 = db.query(DataBaseHelper.TABLE_SERIALS_STUDIOS, new String[]{
                        "serial_id", "name"},
                null, null, null, null, null
        );
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex("_id"));
            if (number != null && !id.equals(number + "")) continue;
            String nameEn = cursor.getString(cursor.getColumnIndex("name_en"));
            String nameRu = cursor.getString(cursor.getColumnIndex("name_ru"));
            String description = cursor.getString(cursor.getColumnIndex("description"));
            String season = cursor.getString(cursor.getColumnIndex("season"));
            String episode = cursor.getString(cursor.getColumnIndex("episode"));
            String notify = cursor.getString(cursor.getColumnIndex("notify"));
            String status = cursor.getString(cursor.getColumnIndex("status"));
            String imageURL = cursor.getString(cursor.getColumnIndex("image_url"));
            List<String> list = new ArrayList<>();

            cursor2.moveToFirst();
            while (cursor2.moveToNext()){
                String sID = cursor2.getString(cursor2.getColumnIndex("serial_id"));
                if (sID.equals(id)) list.add(cursor2.getString(cursor2.getColumnIndex("name")));
            }

            BasicListAdapter.Entity entity = new BasicListAdapter.Entity(id);
            entity.setEnName(nameEn);
            entity.setRuName(nameRu);
            entity.setDescription(description);
            entity.setSeason(season);
            entity.setEpisode(episode);
            entity.setNotify(notify);
            entity.setSoundStudios(list);
            entity.setStatus(status);
            entity.setImageURL(imageURL);
            entity.setType("serial");
            data.add(entity);
        }
        cursor.close();
        cursor2.close();

        Collections.reverse(data);
        return data;
    }

    public boolean delete(String type, String id){
        try {
            if (type.equals("game") || type.equals("serial") || type.equals("movie")) {
                String table = "";
                if (type.equals("game")) table = TABLE_GAMES;
                if (type.equals("serial")) table = TABLE_SERIALS;
                if (type.equals("movie")) table = TABLE_MOVIES;

                String query = "DELETE FROM " + table + " WHERE _id = '" + id + "' ;";
                db.execSQL(query);
                String query2;
                if (type.equals("game")) {
                    query2 = "DELETE FROM " + TABLE_GAMES_GROUPS + " WHERE game_id = '" + id + "' ;";
                    db.execSQL(query2);
                }
                if (type.equals("serial")) {
                    query2 = "DELETE FROM " + TABLE_SERIALS_STUDIOS + " WHERE serial_id = '" + id + "' ;";
                    db.execSQL(query2);
                }
                return true;
            }
        }catch(Exception e){Log.e("DEV_", "Database error!");}
        return false;
    }

}
