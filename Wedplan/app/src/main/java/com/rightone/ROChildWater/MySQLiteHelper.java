package com.rightone.ROChildWater;

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
    private static final String DATABASE_NAME = "WedPlan";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create HighScores table
        String CREATE_Favourite_TABLE = "CREATE TABLE Favourites ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Pid INTEGER, "+
                "Favourite BOOLEAN)";

        // create HighScores table
        db.execSQL(CREATE_Favourite_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older HighScores table if existed
        db.execSQL("DROP TABLE IF EXISTS Favourites");
        // create fresh books table
        this.onCreate(db);
    }
    //---------------------------------------------------------------------

    /**
     * CRUD operations (create "add", read "get", update, delete) book + get all books + delete all books
     */

    // HighScores table name
    private static final String TABLE_FAVOURITE = "Favourites";

    // HighScores Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_PROVIDER = "Pid";
    private static final String KEY_FAVOURITE = "Favourite";

    private static final String[] COLUMNS = {KEY_ID,KEY_PROVIDER,KEY_FAVOURITE};

    public void addFavourites(Integer pid, Boolean fav){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_PROVIDER, pid);
        values.put(KEY_FAVOURITE, fav);

        // 3. insert
        db.insert(TABLE_FAVOURITE, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public void removeFavourites(Integer pid){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_PROVIDER, pid);

        // 3. delete
        db.delete(TABLE_FAVOURITE, // table
                KEY_PROVIDER + "=" + pid, //nullColumnHack
                null); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public Boolean getFavourites(Integer pid){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        // 2. build query
        Cursor cursor =
                db.query(TABLE_FAVOURITE, // a. table
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

    public String getFavouriteProviders(){
        String providerString = "";
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        // 2. build query
        Cursor cursor =
                db.query(TABLE_FAVOURITE, // a. table
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