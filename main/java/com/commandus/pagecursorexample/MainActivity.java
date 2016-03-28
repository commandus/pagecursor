/**
 * Copyright (C) 2016 Andrei Ivanov support@commandus.com
 */
package com.commandus.pagecursorexample;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.commandus.cursor.PageCursor;

/**
 *  This is simple example of ListView binding to the {@link com.commandus.cursor.PageCursor}.
 *  @see ProviderExample
 */
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private ListView mListView1;
    SimpleCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView1 = (ListView) findViewById(R.id.listview);
        mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null,
                new String[]{"name"}, new int[]{android.R.id.text1}, 0);
        mListView1.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, ProviderExample.CONTENT_URI, null, null, null, null);
    }

    /**
     *  {@link ProviderExample#onLoaded(int)} initiate signal that data has been changed (got actual row count).
     * @param loader
     * @param data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}
