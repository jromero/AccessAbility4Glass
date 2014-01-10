package me.jromero.accessability.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.jromero.accessability.R;
import me.jromero.accessability.widget.GravityListView;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.SimpleAdapter;

public class GravityListViewActivity extends Activity {

    private static final String ITEM_PARAM_NAME = "name";

    private GravityListView mListView;
    private SimpleAdapter mAdapter;
    private List<Map<String, String>> mListItems = new ArrayList<Map<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gravity_list_view);

        for (int i = 0; i < 20; i++) {
            mListItems.add(newListItem("ListItem " + i));
        }

        mAdapter = new SimpleAdapter(this, mListItems,
                android.R.layout.simple_list_item_1,
                new String[] { ITEM_PARAM_NAME },
                new int[] { android.R.id.text1 });

        mListView = (GravityListView) findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);
        mListView.onCreate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gravity_list_view, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mListView.onResume();
    }

    @Override
    protected void onPause() {
        mListView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mListView.onDestroy();
        super.onDestroy();
    }

    private Map<String, String> newListItem(String name) {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put(ITEM_PARAM_NAME, name);
        return item;
    }
}
