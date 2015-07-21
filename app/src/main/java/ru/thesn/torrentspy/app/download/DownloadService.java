package ru.thesn.torrentspy.app.download;



import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationManager;
import android.util.Log;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import ru.thesn.torrentspy.app.MyApplication;
import ru.thesn.torrentspy.app.R;
import ru.thesn.torrentspy.app.activities.MainActivity;
import ru.thesn.torrentspy.app.download.torrents.RuTor;
import ru.thesn.torrentspy.app.download.torrents.TFile;
import ru.thesn.torrentspy.app.download.torrents.TorrentSite;
import ru.thesn.torrentspy.app.tools.BasicListAdapter;
import ru.thesn.torrentspy.app.tools.DataBaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DownloadService extends Service {

    public static int currentPercent = -1;
    public static boolean needInterrupt = false;
    private static DataBaseHelper dbHelper;


    private static List<BasicListAdapter.Entity> data;
    private static TorrentSite[] torrentSites = {new RuTor(), new TFile()};

    public static void setDbHelper(DataBaseHelper dbh) {
        dbHelper = dbh;
    }
    private static Context context;
    private static List<String> notificationList;



    public static synchronized void refreshDataBase(){
        Log.i("DEV__", "refreshDataBase begin");
        // TODO если совсем недавно была проверка, то return
        if (dbHelper == null || !dbHelper.isDbAvailable()) return;

        data = new ArrayList<>();
        data.addAll(dbHelper.getGamesDataFromDB(null));
        data.addAll(dbHelper.getMoviesDataFromDB(null));
        data.addAll(dbHelper.getSerialsDataFromDB(null));

        new DownloadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        Log.i("DEV__", "refreshDataBase end");
    }

    private static class DownloadTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            dbHelper.setIsDbAvailable(false);
            currentPercent = 0;
            needInterrupt = false;
            Log.i("DEV__", "DownloadTask PREexecute.");


            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                Notification.Builder builder = new Notification.Builder(context);
                Intent intent = new Intent(context, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                builder
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setTicker("Выполняется проверка!")
                        .setAutoCancel(true)
                        .setContentTitle("Проверка всех элементов")
                        .setContentText("Нажмите здесь для открытия программы...");

                builder.setProgress(100, 0, false);
                Notification notification = builder.build();
                nm.notify(127, notification);
            }

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Map <String, List<BasicListAdapter.Entity>> changedElements = MyApplication.changedElements;
            if (!changedElements.isEmpty())
                changedElements.clear();

            notificationList = new ArrayList<>();

            changedElements.put("movie", new ArrayList<BasicListAdapter.Entity>());
            changedElements.put("serial", new ArrayList<BasicListAdapter.Entity>());
            changedElements.put("game", new ArrayList<BasicListAdapter.Entity>());



            publishProgress(0);
            Log.i("DEV__", "data.size() = " + data.size());
            String result = "";
            for(int i = 0; i < data.size(); i++){
                BasicListAdapter.Entity entity = data.get(i);
                boolean flag = false;
                Log.i("DEV__", entity.getEnName() + " check...");

                for(TorrentSite site: torrentSites){
                    if (needInterrupt) return null;
                    if (!site.isServerAvailable() && (System.currentTimeMillis() - site.getLastTime()) < 300000) {
                        Log.w("DEV_", "Сервер " + site.toString() + " недоступен!");
                        continue; // если сервер недоступен, забываем про него на 5 минут
                    }
                    long sleepTime = (System.currentTimeMillis() - site.getLastTime());
                    long realSleepTime = sleepTime < 1000 ? 1000 - sleepTime : 1;
                    try {
                        if (realSleepTime > 1)
                            Log.w("DEV_", "Ожидаем " + realSleepTime + " ms!");
                        Thread.sleep(realSleepTime);
                    } catch (Exception e){e.printStackTrace();}

                    Log.w("DEV_", "Начинаем работу с " + site.toString());

                    if (site.find(entity)) {
                        flag = true;
                        result = site.toString();
                        // TODO отменять результат, если раздача закрыта правообладателем (например, на tfile игра watch dogs)
                        //break; // лучше осмотреть все, надо найти картинки на tfile.me
                    }
                }
                Log.w("DEV_", "Доступно для загрузки: " + flag);

                boolean oldFlag = entity.getStatus() != null && !entity.getStatus().equals("0") && !entity.getStatus().equals("");

                dbHelper.changeStatus(entity, flag ? result : "0");

                if (flag && entity.getNotify().equals("1")) notificationList.add(!entity.getRuName().equals("") ? entity.getRuName() : entity.getEnName());

                String url = TFile.newImageURL;
                if(!url.equals("")) {
                    dbHelper.addImageURL(entity, url);
                    entity.setImageURL(url);
                    TFile.newImageURL = "";
                    Log.w("DEV_", "Добавлена картинка для элемента " + url);
                }
                final String url2 = entity.getImageURL();
                if (url2 != null && !url2.equals(""))
                    Picasso.with(context).load(url2).fetch(new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.w("DEV_", "Картинка загружена (из кэша или инета): " + url2);
                        }

                        @Override
                        public void onError() {
                            Log.w("DEV_", "Ошибка при загрузке картинки: " + url2);
                        }
                    });

                if (MyApplication.adapters != null && ((oldFlag && !flag) || (!oldFlag && flag) || !url.equals(""))) { // если oldFlag != flag по значению
                    entity.setStatus(flag ? result : "0");
                    if (entity.getType().equals("movie"))
                        MyApplication.changedElements.get("movie").add(entity);
                    if (entity.getType().equals("serial"))
                        MyApplication.changedElements.get("serial").add(entity);
                    if (entity.getType().equals("game"))
                        MyApplication.changedElements.get("game").add(entity);

                    Log.w("DEV_", "Изменилось entity: " + entity.toString());
                }
                publishProgress((i+1) * 100 / data.size());
                Log.w("DEV_", (i+1) * 100 / data.size() + "");
            }




            try{Thread.sleep(1000);} catch (Exception e){e.printStackTrace();}
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            currentPercent = values[0];
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                Notification.Builder builder = new Notification.Builder(context);
                Intent intent = new Intent(context, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                builder
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setContentTitle("Выполняется проверка!")
                        .setProgress(100, currentPercent, false);
                Notification notification = builder.build();
                notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
                nm.notify(127, notification);
            }
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i("DEV__", "DownloadTask Finished");
            super.onPostExecute(aVoid);
            dbHelper.setIsDbAvailable(true);
            currentPercent = -1;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                if (!needInterrupt && notificationList.size() > 0) {
                    Notification.Builder builder = new Notification.Builder(context);
                    Intent intent = new Intent(context, MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    builder
                            .setContentIntent(pendingIntent)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setWhen(System.currentTimeMillis())
                            .setAutoCancel(true)
                            .setContentTitle("Проверка завершена!");


                    if (notificationList.size() == 1)
                        builder.setContentText("Новая загрузка: " + notificationList.get(0));
                    else builder.setContentText("Новые загрузки: " + notificationList.size() + " шт.");

                    Notification notification = builder.build();
                    notification.flags = notification.flags | Notification.DEFAULT_VIBRATE;
                    nm.notify(127, notification);
                } else {
                    nm.cancel(127);
                }
            }
        }

    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate()
    {
        Log.e("DEV_", "Service created");
    }

    @Override
    public synchronized int onStartCommand(Intent i, int flags, int startId) {
        Log.e("DEV_", "Service started");
        context = getApplicationContext();
        refreshDataBase();

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e("DEV_", "Service stopped");
        super.onDestroy();
    }
}
