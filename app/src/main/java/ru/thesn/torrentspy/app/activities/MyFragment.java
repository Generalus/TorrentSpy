package ru.thesn.torrentspy.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ru.thesn.torrentspy.app.MyApplication;
import ru.thesn.torrentspy.app.R;
import ru.thesn.torrentspy.app.activities.EditActivity;
import ru.thesn.torrentspy.app.tools.BasicListAdapter;
import ru.thesn.torrentspy.app.tools.UtilsScreen;

import java.util.List;


public class MyFragment extends Fragment {

    private List<BasicListAdapter.Entity> data;

    private RecyclerView mRecyclerView;
    private TextView addElementText;
    private String type;

    private RecyclerView.LayoutManager getLayoutManager() {
        return new StaggeredGridLayoutManager(UtilsScreen.getDisplayColumns(getActivity()), StaggeredGridLayoutManager.VERTICAL);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("DEV_", "MyFragment onCreate begin");
        type = getArguments().getString("type");
        if (type.equals("movie"))
            data = MyApplication.dbHelper.getMoviesDataFromDB(null);
        if (type.equals("serial"))
            data = MyApplication.dbHelper.getSerialsDataFromDB(null);
        if (type.equals("game"))
            data = MyApplication.dbHelper.getGamesDataFromDB(null);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("DEV_", "Fragment begin: " + type);
        final View v = inflater.inflate(R.layout.fragment_main, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerview);
        addElementText = (TextView)v.findViewById(R.id.add_element_text);
        addElementText.setVisibility(View.GONE);
        mRecyclerView.setLayoutManager(getLayoutManager());

        final BasicListAdapter adapter = new BasicListAdapter(v.getContext());
        adapter.setData(!MyApplication.adapters.containsKey(type) ? data : MyApplication.adapters.get(type).getmData());

        adapter.setOnItemClickListener(new BasicListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BasicListAdapter.Entity entity) {
                Intent intent = new Intent(getActivity(), EditActivity.class);
                intent.putExtra("type", entity.getType());
                intent.putExtra("id", entity.getId());
                startActivity(intent);
            }
        });
        MyApplication.adapters.put(type, adapter);
        mRecyclerView.setAdapter(adapter);
        if (adapter.getmData().isEmpty()){
            mRecyclerView.setVisibility(View.GONE);
            addElementText.setVisibility(View.VISIBLE);
            addElementText.setTextColor(Color.GRAY);
        }
        Log.i("DEV_", "MyFragment end");
        return v;
    }

    @Override
    public void onResume() {
        if (!MyApplication.adapters.get(type).getmData().isEmpty()){
            addElementText.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
        super.onResume();
    }
}