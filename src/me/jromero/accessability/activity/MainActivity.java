package me.jromero.accessability.activity;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.jromero.accessability.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String ITEM_PARAM_NAME = "name";
    private static final String ITEM_PARAM_INTENT = "intent";

    private ListView mListView;
    private SimpleAdapter mAdapter;
    private List<Map<String, String>> mListItems = new ArrayList<Map<String,String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListItems.add(newListItem("GravityListView", GravityListViewActivity.class));
        mListItems.add(newListItem("HandsFreeListView", HandsFreeListViewActivity.class));

        mAdapter = new SimpleAdapter(this, mListItems,
                android.R.layout.simple_list_item_1,
                new String[] {ITEM_PARAM_NAME}, new int[] {android.R.id.text1});

        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mOnItemClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private Map<String, String> newListItem(String name,
            Class<? extends Activity> clazz) {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put(ITEM_PARAM_NAME, name);
        item.put(ITEM_PARAM_INTENT, new Intent(this, clazz).toUri(0));
        return item;
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            @SuppressWarnings("unchecked")
            Map<String, String> item = (Map<String, String>) mAdapter.getItem(position);
            String uri = item.get(ITEM_PARAM_INTENT);
            try {
                startActivity(Intent.parseUri(uri, 0));
            } catch (URISyntaxException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    };
}
