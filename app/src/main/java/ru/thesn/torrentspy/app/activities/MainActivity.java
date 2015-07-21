package ru.thesn.torrentspy.app.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import ru.thesn.torrentspy.app.AlarmReceiver;
import ru.thesn.torrentspy.app.MyApplication;
import ru.thesn.torrentspy.app.activities.add.AddGameActivity;
import ru.thesn.torrentspy.app.activities.add.AddMovieActivity;
import ru.thesn.torrentspy.app.activities.add.AddSerialActivity;
import ru.thesn.torrentspy.app.download.DownloadService;

import ru.thesn.torrentspy.app.R;
import ru.thesn.torrentspy.app.tools.Animations;
import ru.thesn.torrentspy.app.tools.BasicListAdapter;
import ru.thesn.torrentspy.app.tools.StyledSnackBar;

import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private LinearLayout searchContainer;
    private static long back_pressed;
    private ViewPager viewPager;
    private Fragment f1;
    private Fragment f2;
    private Fragment f3;
    private CheckProgress checkProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Bundle b1 = new Bundle();
        Bundle b2 = new Bundle();
        Bundle b3 = new Bundle();

        f1 = new MyFragment();
        b1.putString("type", "movie");
        f1.setArguments(b1);

        f2 = new MyFragment();
        b2.putString("type", "serial");
        f2.setArguments(b2);

        f3 = new MyFragment();
        b3.putString("type", "game");
        f3.setArguments(b3);

        if (!MyApplication.isReceiverAlive) {
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.setAction("ru.thesn.torrentspy.app.START_AlarmReceiver");
            sendBroadcast(intent);
        }


        // Initializing Toolbar and setting it as the actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        searchContainer = (LinearLayout)findViewById(R.id.search_container);
        searchContainer.setVisibility(View.GONE);

        checkProgress = new CheckProgress();
        checkProgress.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        //searchContainer.setVisibility(View.VISIBLE); для показа или View.GONE для скрытия


        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.pager);

        viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.myFAB);
        ColorStateList rippleColor = getResources().getColorStateList(R.color.fab_ripple_color);
        myFab.setBackgroundTintList(rippleColor);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Class clazz;
                switch (viewPager.getCurrentItem()){
                    case 0: clazz = AddMovieActivity.class; break;
                    case 1: clazz = AddSerialActivity.class; break;
                    case 2: clazz = AddGameActivity.class; break;
                    default: clazz = null;
                }

                if (clazz != null){
                    Intent intent = new Intent(getApplicationContext(), clazz);
                    startActivity(intent);
                }
            }
        });
        myFab.animate();

        //Initializing NavigationView
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {


                //Checking if the item is in checked state or not, if not make it in checked state
                if(menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                //Closing drawer on item click
                drawerLayout.closeDrawers();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()){

                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.inbox:
                        Intent intent = new Intent(MainActivity.this, AppConfigActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.starred:
                        Intent intent2 = new Intent(MainActivity.this, AboutActivity.class);
                        startActivity(intent2);
                        return true;
                    //case R.id.google_play:
                    //    Intent intent = new Intent(Intent.ACTION_VIEW);
                    //    intent.setData(Uri.parse("market://details?id=")); // TODO указать адрес приложения
                    //    startActivity(intent);
                    //    return true;
                    default:
                        Toast.makeText(getApplicationContext(), "Возникла ошибка...", Toast.LENGTH_SHORT).show();
                        return true;

                }
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.openDrawer, R.string.closeDrawer){

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem downloadItem = menu.findItem(R.id.action_download);
        MenuItem cancelItem = menu.findItem(R.id.action_cancel);
        boolean f = DownloadService.currentPercent == -1;
        downloadItem.setVisible(f);
        cancelItem.setVisible(!f);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return id == R.id.action_download || id == R.id.action_cancel || super.onOptionsItemSelected(item);
    }


    public class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) { super(fm); }
        @Override
        public Fragment getItem(int position) {
            Log.i("DEV_", "MyPagerAdapter getItem");
            switch (position) {
                case 0: return f1;
                case 1: return f2;
                case 2: return f3;
                default: return null;
            }
        }
        @Override
        public int getCount() { return 3; }
        @Override
        public CharSequence getPageTitle(int position) {
            Log.i("DEV_", "MyPagerAdapter getPageTitle");
            switch (position) {
                case 0: return "Фильмы";
                case 1: return "Сериалы";
                case 2: return "Игры для PC";
                default: return "Not found";
            }
        }
    }



    public void download(MenuItem item){
        startService(new Intent(this, DownloadService.class));
    }

    public void cancel(MenuItem item){
        DownloadService.needInterrupt = true;
    }

    class CheckProgress extends AsyncTask<Void, Integer, Boolean> {
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        @Override
        protected Boolean doInBackground(Void... params) {
            int percent;
            int previousPercent = -1;
            while (!isCancelled()) {
                try {
                    Thread.sleep(60);
                    percent = DownloadService.currentPercent;
                    int isInterrupted = DownloadService.needInterrupt ? 1 : 0;
                    if (percent != previousPercent) publishProgress(percent, previousPercent, isInterrupted);
                    previousPercent = percent;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] > -1) {
                if (values[1] == -1) {
                    searchContainer.setVisibility(View.VISIBLE);
                    invalidateOptionsMenu();
                }
                progressBar.setProgress(values[0]);
            } else {
                Animations.disappearance(searchContainer);
                invalidateOptionsMenu();
                if (values[2] == 0)
                    StyledSnackBar.showGreenSnackBar(getCurrentFocus(), "Проверка завершена!");
                else
                    StyledSnackBar.showRedSnackBar(getCurrentFocus(), "Проверка отменена!");



                for(Map.Entry<String, List<BasicListAdapter.Entity>> entry: MyApplication.changedElements.entrySet()){
                    BasicListAdapter adapter = MyApplication.adapters.get(entry.getKey());
                    if (adapter != null) {
                        List<BasicListAdapter.Entity> changedElements = entry.getValue();
                        List<BasicListAdapter.Entity> currentElements = adapter.getmData();
                        for(BasicListAdapter.Entity entity: changedElements)
                            for (int j = 0; j < currentElements.size(); j++)
                                if (currentElements.get(j).getId().equals(entity.getId())) {
                                    currentElements.set(j, entity);
                                    Log.i("DEV_", "Изменился элемент " + entity.getEnName());
                                    Log.i("DEV_", "Текущая картинка: " + entity.getImageURL());
                                    adapter.notifyItemChanged(adapter.getLocation(currentElements, entity));
                                }


                    }
                }
                for(Map.Entry<String, BasicListAdapter> entry: MyApplication.adapters.entrySet()) {
                    BasicListAdapter adapter = entry.getValue();
                    if (adapter != null)
                        adapter.notifyDataSetChanged();
                }

            }
            Log.i("DEV_", "Загрузка завершена!");

            super.onProgressUpdate(values);
        }
    }


    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis())
            super.onBackPressed();
        else
            StyledSnackBar.showGreenSnackBar(getCurrentFocus(), "Нажмите снова для выхода!");
        back_pressed = System.currentTimeMillis();
    }

    @Override
    protected void onPostResume() {
        Log.i("DEV__", "Main onPostResume");
        if (checkProgress == null) {
            checkProgress = new CheckProgress();
            checkProgress.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        super.onPostResume();
    }

    @Override
    protected void onStop() {
        Log.i("DEV_", "Main onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i("DEV_", "Main onDestroy");
        checkProgress.cancel(true);
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        Log.i("DEV_", "Main onPostCreate");
        super.onPostCreate(savedInstanceState);
    }
}

