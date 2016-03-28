/**
 * Copyright (C) 2016 Andrei Ivanov support@commandus.com
 */
package com.commandus.cursor;

import android.database.AbstractWindowedCursor;
import android.database.CursorWindow;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class provides row data to the {@link android.content.ContentProvider} loaded
 * for instance, from the network page by page.
 * Cursor load data from the {@link com.commandus.cursor.PageCursor.LoaderInterface()}.
 * Implemented {@link com.commandus.cursor.PageCursor.LoaderInterface#fillWindow(android.database.CursorWindow, int)} }
 * method fills loaded data into the "window" of specified size.
 *
 */
public class PageCursor extends AbstractWindowedCursor {

    private int mActualCount;

    /**
     * Return actual count of loaded rows, -1 if not determined yet
     * @return actual row count
     */
    public int getActualCount() {
        return mActualCount;
    }

    protected void setActualCount(int value) {
        mActualCount = value;
        mLoader.setActualCount(value);
    }

    /**
     * Optional interface. Inform caller data set loaded successfully
     */
    public interface OnLoadedInterface {
        void onLoaded(int count);
    }
    /**
     * Pass implemented LoaderInterface class to {@link com.commandus.cursor.PageCursor#PageCursor(LoaderInterface, String[])} constructor.
     */
    public interface LoaderInterface {
        /**
         * Reads rows into a buffer.
         * PageCursor set column 0 ("_id") value to sequence number starting with 0, so you don't need
         * set it manually.
         * @param window The window to fill into. It contains {@link #getWindowSize()} rows already.
         * @param position The start position for filling the window.
         * @return loaded size. If size is less {@link #getWindowSize()}, "empty" rows window filled with NULLs
         */
        int fillWindow(CursorWindow window, int position);

        /**
         * If you don't know actual size, provide enough big value, e.g. 10000
         * @return size of data
         */
        int getCount();

        /**
         * Set up rows to be loaded in the window (page size)
         * @return row count in window buffer
         */
        int getWindowSize();

        /**
         * Return loaded row count
         * @param value returned actual row count loaded
         */
        void setActualCount(int value);
    }

    static final String TAG = PageCursor.class.getSimpleName();
    static final int NO_COUNT = -1;

    /** The names of the columns in the rows */
    private final String[] mColumns;
    private final LoaderInterface mLoader;
    /** A mapping of column names to column indices, to speed up lookups */
    private Map<String, Integer> mColumnNameMap;

    public PageCursor(LoaderInterface loader, String[] columns) {
        mColumnNameMap = null;
        mColumns = columns;
        mLoader = loader;
        mActualCount = -1;
    }

    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        if (mWindow == null || newPosition < mWindow.getStartPosition()
                || newPosition >= (mWindow.getStartPosition() + mWindow.getNumRows())) {
            fillWindow(newPosition);
        }
        return true;
    }

    @Override
    public int getCount() {
        return mLoader.getCount();
    }

    private static int cursorPickFillWindowStartPosition(int cursorPosition, int pageSize) {
        return pageSize * (cursorPosition / pageSize);
    }

    private int fillWindow(int requiredPos) {
        // The number of rows that can fit in the cursor window, 0 if unknown
        if (mWindow == null) {
            mWindow = new CursorWindow(null);
        } else {
            mWindow.clear();
        }
        int loaded = 0;
        try {
            mWindow.acquireReference();
            int pageSize = mLoader.getWindowSize();

            int startPos = cursorPickFillWindowStartPosition(requiredPos, pageSize);
            for (int i = 0; i < pageSize; i++) {
                mWindow.setNumColumns(mColumns.length);
                if (!mWindow.allocRow())
                    throw new IndexOutOfBoundsException();
                mWindow.putLong(startPos + i, i, 0);
            }

            loaded = mLoader.fillWindow(mWindow, startPos);
            if ((mActualCount < 0) && (loaded < pageSize))
                setActualCount(startPos + loaded);
            mWindow.setStartPosition(startPos);
            mWindow.releaseReference();
        } catch (RuntimeException ex) {
            // Close the cursor window if the query failed and therefore will not produce any results
            mWindow.close();
            mWindow = null;
            throw ex;
        }
        return loaded;
    }

    @Override
    public int getColumnIndex(String columnName) {
        // Create mColumnNameMap on demand
        if (mColumnNameMap == null) {
            HashMap<String, Integer> map = new HashMap<String, Integer>(mColumns.length, 1);
            for (int i = 0; i < mColumns.length; i++) {
                map.put(mColumns[i], i);
            }
            mColumnNameMap = map;
        }

        final int periodIndex = columnName.lastIndexOf('.');
        if (periodIndex != -1) {
            Exception e = new Exception();
            columnName = columnName.substring(periodIndex + 1);
        }

        Integer i = mColumnNameMap.get(columnName);
        if (i != null) {
            return i.intValue();
        } else {
            return -1;
        }
    }

    @Override
    public String[] getColumnNames() {
        return mColumns;
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void close() {
        super.close();
        synchronized (this) {
        }
    }

    @Override
    public boolean requery() {
        if (isClosed()) {
            return false;
        }

        synchronized (this) {
            if (mWindow != null) {
                mWindow.clear();
            }
            mPos = -1;
        }

        try {
            return super.requery();
        } catch (IllegalStateException e) {
            // for backwards compatibility, just return false
            return false;
        }
    }

    @Override
    public void setWindow(CursorWindow window) {
        super.setWindow(window);
    }

    /**
     * Release the native resources, if they haven't been released yet.
     */
    @Override
    protected void finalize() {
        try {
            // if the cursor hasn't been closed yet, close it first
            if (mWindow != null) {
                close();
            }
        } finally {
            super.finalize();
        }
    }
}
