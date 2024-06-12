package com.EEEITSolutions.elearning.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Arrays;

public class MySQLiteHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "Elearning";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create HighScores table
        String CREATE_CHAPTER_TABLE = "CREATE TABLE Chapters ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Pid INTEGER, "+
                "Favourite BOOLEAN)";
        db.execSQL(CREATE_CHAPTER_TABLE);

        String CREATE_VIDEO_TABLE = "CREATE TABLE Videos ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Pid INTEGER, "+
                "Favourite BOOLEAN)";
        db.execSQL(CREATE_VIDEO_TABLE);

        String CREATE_EBOOK_TABLE = "CREATE TABLE Ebooks ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Pid INTEGER, "+
                "Favourite BOOLEAN)";
        db.execSQL(CREATE_EBOOK_TABLE);

        String CREATE_ACTIVITY_TABLE = "CREATE TABLE Activity ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Pid INTEGER, "+
                "Favourite BOOLEAN)";
        db.execSQL(CREATE_ACTIVITY_TABLE);

        String CREATE_FVIDEO_TABLE = "CREATE TABLE FVideos ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Pid INTEGER, "+
                "Favourite BOOLEAN)";
        db.execSQL(CREATE_FVIDEO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older HighScores table if existed
        db.execSQL("DROP TABLE IF EXISTS Chapters");
        db.execSQL("DROP TABLE IF EXISTS Videos");
        db.execSQL("DROP TABLE IF EXISTS Ebooks");
        db.execSQL("DROP TABLE IF EXISTS Activity");
        db.execSQL("DROP TABLE IF EXISTS FVideos");
        // create fresh books table
        this.onCreate(db);
    }
    //---------------------------------------------------------------------

    /**
     * CRUD operations (create "add", read "get", update, delete) book + get all books + delete all books
     */

    // HighScores table name
    private static final String TABLE_CHAPTER = "Chapters";
    private static final String TABLE_VIDEO = "Videos";
    private static final String TABLE_EBOOK = "Ebooks";
    private static final String TABLE_ACTIVITY = "Activity";
    private static final String TABLE_FVIDEO = "FVideos";

    // HighScores Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_PROVIDER = "Pid";
    private static final String KEY_FAVOURITE = "Favourite";

    private static final String[] COLUMNS = {KEY_ID,KEY_PROVIDER,KEY_FAVOURITE};

    public void addFavouriteChapter(Integer pid, Boolean fav){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_PROVIDER, pid);
        values.put(KEY_FAVOURITE, fav);

        // 3. insert
        db.insert(TABLE_CHAPTER, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public void removeFavouriteChapter(Integer pid){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_PROVIDER, pid);

        // 3. delete
        db.delete(TABLE_CHAPTER, // table
                KEY_PROVIDER + "=" + pid, //nullColumnHack
                null); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public Boolean getFavouriteChapter(Integer pid){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        // 2. build query
        Cursor cursor =
                db.query(TABLE_CHAPTER, // a. table
                        COLUMNS, // b. column names
                        " Pid = ?", // c. selections
                        new String[] { String.valueOf(pid) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.moveToFirst();
        boolean value = cursor.getInt(2) > 0;
        return value;
    }

    public String getFavouriteChapters(){
        String providerString = "";
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        // 2. build query
        Cursor cursor =
                db.query(TABLE_CHAPTER, // a. table
                        COLUMNS, // b. column names
                        null, // c. selections
                        null,   // where clause
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one

        if(cursor.getCount() <= 0){
            cursor.close();
            return providerString;
        }
        Integer i = 0;
        Integer data[] = new Integer[cursor.getCount()];
        if (cursor.moveToFirst()) {
            do {
                // get the data into array, or class variable
                data[i] = cursor.getInt(1);
                i+=1;
            } while (cursor.moveToNext());
        }
        cursor.close();
        providerString = Arrays.toString(data)
                .replace("[", "")  //remove the right bracket
                .replace("]", "")  //remove the left bracket
                .trim();
        return providerString;
    }

    //************************** VIDEO FUNCTIONS ******************************//

    public void addFavouriteVideo(Integer pid, Boolean fav){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_PROVIDER, pid);
        values.put(KEY_FAVOURITE, fav);

        // 3. insert
        db.insert(TABLE_VIDEO, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public void removeFavouriteVideo(Integer pid){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_PROVIDER, pid);

        // 3. delete
        db.delete(TABLE_VIDEO, // table
                KEY_PROVIDER + "=" + pid, //nullColumnHack
                null); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public Boolean getFavouriteVideo(Integer pid){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        // 2. build query
        Cursor cursor =
                db.query(TABLE_VIDEO, // a. table
                        COLUMNS, // b. column names
                        " Pid = ?", // c. selections
                        new String[] { String.valueOf(pid) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.moveToFirst();
        boolean value = cursor.getInt(2) > 0;
        return value;
    }

    public String getFavouriteVideos(){
        String providerString = "";
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        // 2. build query
        Cursor cursor =
                db.query(TABLE_VIDEO, // a. table
                        COLUMNS, // b. column names
                        null, // c. selections
                        null,   // where clause
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one

        if(cursor.getCount() <= 0){
            cursor.close();
            return providerString;
        }
        Integer i = 0;
        Integer data[] = new Integer[cursor.getCount()];
        if (cursor.moveToFirst()) {
            do {
                // get the data into array, or class variable
                data[i] = cursor.getInt(1);
                i+=1;
            } while (cursor.moveToNext());
        }
        cursor.close();
        providerString = Arrays.toString(data)
                .replace("[", "")  //remove the right bracket
                .replace("]", "")  //remove the left bracket
                .trim();
        return providerString;
    }

    //************************** EBOOK FUNCTIONS ******************************//

    public void addFavouriteEbook(Integer pid, Boolean fav){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_PROVIDER, pid);
        values.put(KEY_FAVOURITE, fav);

        // 3. insert
        db.insert(TABLE_EBOOK, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public void removeFavouriteEbook(Integer pid){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_PROVIDER, pid);

        // 3. delete
        db.delete(TABLE_EBOOK, // table
                KEY_PROVIDER + "=" + pid, //nullColumnHack
                null); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public Boolean getFavouriteEbook(Integer pid){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        // 2. build query
        Cursor cursor =
                db.query(TABLE_EBOOK, // a. table
                        COLUMNS, // b. column names
                        " Pid = ?", // c. selections
                        new String[] { String.valueOf(pid) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.moveToFirst();
        boolean value = cursor.getInt(2) > 0;
        return value;
    }

    public String getFavouriteEbooks(){
        String providerString = "";
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        // 2. build query
        Cursor cursor =
                db.query(TABLE_EBOOK, // a. table
                        COLUMNS, // b. column names
                        null, // c. selections
                        null,   // where clause
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one

        if(cursor.getCount() <= 0){
            cursor.close();
            return providerString;
        }
        Integer i = 0;
        Integer data[] = new Integer[cursor.getCount()];
        if (cursor.moveToFirst()) {
            do {
                // get the data into array, or class variable
                data[i] = cursor.getInt(1);
                i+=1;
            } while (cursor.moveToNext());
        }
        cursor.close();
        providerString = Arrays.toString(data)
                .replace("[", "")  //remove the right bracket
                .replace("]", "")  //remove the left bracket
                .trim();
        return providerString;
    }


    //************************************* ACTIVITY FUNCTIONS *************************//

    public void addFavouriteActivity(Integer pid, Boolean fav){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_PROVIDER, pid);
        values.put(KEY_FAVOURITE, fav);

        // 3. insert
        db.insert(TABLE_ACTIVITY, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public void removeFavouriteActivity(Integer pid){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_PROVIDER, pid);

        // 3. delete
        db.delete(TABLE_ACTIVITY, // table
                KEY_PROVIDER + "=" + pid, //nullColumnHack
                null); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public Boolean getFavouriteActivity(Integer pid){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        // 2. build query
        Cursor cursor =
                db.query(TABLE_ACTIVITY, // a. table
                        COLUMNS, // b. column names
                        " Pid = ?", // c. selections
                        new String[] { String.valueOf(pid) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.moveToFirst();
        boolean value = cursor.getInt(2) > 0;
        return value;
    }

    public String getFavouriteActivities(){
        String providerString = "";
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        // 2. build query
        Cursor cursor =
                db.query(TABLE_ACTIVITY, // a. table
                        COLUMNS, // b. column names
                        null, // c. selections
                        null,   // where clause
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one

        if(cursor.getCount() <= 0){
            cursor.close();
            return providerString;
        }
        Integer i = 0;
        Integer data[] = new Integer[cursor.getCount()];
        if (cursor.moveToFirst()) {
            do {
                // get the data into array, or class variable
                data[i] = cursor.getInt(1);
                i+=1;
            } while (cursor.moveToNext());
        }
        cursor.close();
        providerString = Arrays.toString(data)
                .replace("[", "")  //remove the right bracket
                .replace("]", "")  //remove the left bracket
                .trim();
        return providerString;
    }

    //************************** FEATURED VIDEO FUNCTIONS ******************************//

    public void addFavouriteFVideo(Integer pid, Boolean fav){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_PROVIDER, pid);
        values.put(KEY_FAVOURITE, fav);

        // 3. insert
        db.insert(TABLE_FVIDEO, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public void removeFavouriteFVideo(Integer pid){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_PROVIDER, pid);

        // 3. delete
        db.delete(TABLE_FVIDEO, // table
                KEY_PROVIDER + "=" + pid, //nullColumnHack
                null); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public Boolean getFavouriteFVideo(Integer pid){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        // 2. build query
        Cursor cursor =
                db.query(TABLE_FVIDEO, // a. table
                        COLUMNS, // b. column names
                        " Pid = ?", // c. selections
                        new String[] { String.valueOf(pid) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.moveToFirst();
        boolean value = cursor.getInt(2) > 0;
        return value;
    }

    public String getFavouriteFVideos(){
        String providerString = "";
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        // 2. build query
        Cursor cursor =
                db.query(TABLE_FVIDEO, // a. table
                        COLUMNS, // b. column names
                        null, // c. selections
                        null,   // where clause
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one

        if(cursor.getCount() <= 0){
            cursor.close();
            return providerString;
        }
        Integer i = 0;
        Integer data[] = new Integer[cursor.getCount()];
        if (cursor.moveToFirst()) {
            do {
                // get the data into array, or class variable
                data[i] = cursor.getInt(1);
                i+=1;
            } while (cursor.moveToNext());
        }
        cursor.close();
        providerString = Arrays.toString(data)
                .replace("[", "")  //remove the right bracket
                .replace("]", "")  //remove the left bracket
                .trim();
        return providerString;
    }
}
