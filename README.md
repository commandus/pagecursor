PageCursor
==========

Android cursor loads rows page by page with example.

PageCursor is an Android cursor class loads rows page by page from web service or other remote data providers.

It helps scroll rows in the ListView binded to the huge dataset especially when web service send rows in chunks (pages).

Example
-------

### MainActivity.java

This is simple example of ListView binding to the ContentProvider.

### ProviderExample.java

Example shows how to load data when actual row count is uknown. 

In this example data provider set initial very big row count value.

When cursor reaches end of data, cursor inform data provider about actual row count and send data changed signal
on which cursor re-created with actual row count size.

If you can get actual row count, you can skip this. Knowledge of actual row count is important, ListView's cursor must have valid row count.


PageCursor source
=================

### LoaderInterface

Web service class read data from the network resources must implement LoaderInterface.

#### int getCount();

Set row count in this method if you know actual size or appropriate "big" value.

#### int getWindowSize();

Set row count in one page. E.g. 10 if your web service return 10 rows per request.

#### int fillWindow(CursorWindow window, int position);

You need read page and fill window. This methods must return row count in the page. 
If this value is less than getWindowSize(), it indicates end of data.

position parameter aligned to getWindowSize(), for example, if page = 10, position can be 0, 10, 20...

Note: position is NOT page number. 

Note: It is important to provide row identifier in the column 0 ("_id"), so PageCursor do it if you don't. Please remember first column must be _id.

