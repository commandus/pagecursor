/**
 * Copyright (C) 2016 Andrei Ivanov support@commandus.com
 */
package com.commandus.pagecursorexample;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Loader;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.CursorWindow;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.commandus.cursor.PageCursor;

/**
 * Example shows how to load data page by page into the {@link android.content.ContentProvider}
 * {@link PageCursor} cursor.
 * {@link PageLoaderExample} provides actual data.
 * @see PageCursor
 * @see MainActivity
 */
public class ProviderExample extends ContentProvider implements PageCursor.OnLoadedInterface{
    public static final String CONTENT_AUTHORITY = "com.commandus.example";
    public static final Uri CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static UriMatcher URL_MATCHER;
    private static final int MATCH_EXAMPLE = 1;
    private static final java.lang.String[] FIELDS = {"_id", "name"};

    // We don't know how much rows in the dataset therefore set reasonable big value.
    private int mEstimatedCount = 501;

    /**
     * {@link com.commandus.pagecursorexample.ProviderExample.PageLoaderExample#onLoaded(int)} call this
     * interface method.
     * Set {@link #mEstimatedCount} to actual size then notify dataset is changed.
     * It causes cursor re-creation with actual size.
     * If you know exact row count, you don't need implement this.
     * Anyway in case data set smaller than {@link #mEstimatedCount}, provider creates rows with NULLS.
     * {@link MainActivity#onLoadFinished(Loader, Cursor)}
     * @param count actual row count
     */
    @Override
    public void onLoaded(int count) {
        mEstimatedCount = count;
        getContext().getContentResolver().notifyChange(CONTENT_URI, null);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (URL_MATCHER.match(uri)) {
            case MATCH_EXAMPLE:
                break;
            default:
                throw new IllegalArgumentException("Invalid content provider URI: " + uri.toString());
        }
        PageCursor c = new PageCursor(new PageLoaderExample(mEstimatedCount, this), FIELDS);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return "text/plain";
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    static {
        URL_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URL_MATCHER.addURI(CONTENT_AUTHORITY, null, MATCH_EXAMPLE);
    }

    class PageLoaderExample implements PageCursor.LoaderInterface {
        private int mEstimatedRowCount;

        private final PageCursor.OnLoadedInterface mOnLoaded;

        PageLoaderExample(int estimatedRowCount, PageCursor.OnLoadedInterface onLoaded) {
            mEstimatedRowCount = estimatedRowCount;
            mOnLoaded = onLoaded;
        }

        public int getCount()
        {
            return mEstimatedRowCount; // estimated size
        }

        public int getWindowSize()
        {
            return 10;
        }

        /**
         * Last row in dataset, no more data to load.
         * Inform provider via {@link com.commandus.cursor.PageCursor.OnLoadedInterface} to rebuild cursor with actual size
         * @param value actual row count loaded
         */
        @Override
        public void setActualCount(int value) {
            if (mOnLoaded != null)
                mOnLoaded.onLoaded(value);
        }

        @Override
        public int fillWindow(CursorWindow window, int position){
            int r = 0;
            int p = position;
            for (r = 0; r < getWindowSize(); r++) {
                if (p >= 99) return r;  // actual size
                String v = Integer.toString(p);
                window.putLong(p, r, 0);
                window.putString(v, r, 1);
                p++;
            }
            return r;
        }
     }
}