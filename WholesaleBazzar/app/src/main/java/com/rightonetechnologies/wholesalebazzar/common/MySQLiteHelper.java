package com.rightonetechnologies.wholesalebazzar.common;

import android.app.TaskStackBuilder;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

public class MySQLiteHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "WholesaleBazzar";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create HighScores table
        String CREATE_CART_TABLE = "CREATE TABLE Cart ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Did INTEGER, "+
                "DName STRING, "+
                "Pid INTEGER, "+
                "PackID INTEGER, "+
                "Company STRING, "+
                "PunitID INTEGER, "+
                "Punit STRING, "+
                "Pqty INTEGER, "+
                "Pmrp FLOAT, "+
                "Psp FLOAT, "+
                "PTax FLOAT, "+
                "Ptotal FLOAT, " +
                "Image STRING)";
        db.execSQL(CREATE_CART_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older HighScores table if existed
        db.execSQL("DROP TABLE IF EXISTS Cart");
        // create fresh books table
        this.onCreate(db);
    }

    // HighScores table name
    private static final String TABLE_CART = "Cart";
    // HighScores Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_DEAL_ID = "Did";
    private static final String KEY_DEAL_NAME = "DName";
    private static final String KEY_PRODUCT_ID = "Pid";
    private static final String KEY_PACKAGE_ID = "PackID";
    private static final String KEY_PRODUCT_IMAGE = "Image";
    private static final String KEY_PRODUCT_COMPANY = "Company";
    private static final String KEY_PRODUCT_UNIT = "Punit";
    private static final String KEY_PRODUCT_UNIT_ID = "PunitID";
    private static final String KEY_PRODUCT_QTY = "Pqty";
    private static final String KEY_PRODUCT_MRP = "Pmrp";
    private static final String KEY_PRODUCT_SP = "Psp";
    private static final String KEY_PRODUCT_TAX = "PTax";
    private static final String KEY_PRODUCT_TOTAL = "Ptotal";

    private static final String[] COLUMNS = {KEY_ID,KEY_DEAL_ID,KEY_DEAL_NAME,KEY_PRODUCT_ID,KEY_PACKAGE_ID,KEY_PRODUCT_COMPANY,KEY_PRODUCT_UNIT_ID,KEY_PRODUCT_UNIT,KEY_PRODUCT_QTY,KEY_PRODUCT_MRP,KEY_PRODUCT_SP,KEY_PRODUCT_TAX,KEY_PRODUCT_TOTAL,KEY_PRODUCT_IMAGE};

    private Integer addProduct(String did, String dname, String pid, String packid, String company, String unit_id, String unit, String qty, String mrp, String sp, String tax, String image){
        Integer mid = checkProduct(pid,packid);
        if(mid == 0) {
            // 1. get reference to writable DB
            SQLiteDatabase db = this.getWritableDatabase();
            // 2. create ContentValues to add key "column"/value
            ContentValues values = new ContentValues();
            values.put(KEY_DEAL_ID, did);
            values.put(KEY_DEAL_NAME, dname);
            values.put(KEY_PRODUCT_ID, pid);
            values.put(KEY_PACKAGE_ID, packid);
            values.put(KEY_PRODUCT_COMPANY, company);
            values.put(KEY_PRODUCT_UNIT_ID, unit_id);
            values.put(KEY_PRODUCT_UNIT, unit);
            values.put(KEY_PRODUCT_QTY, qty);
            values.put(KEY_PRODUCT_MRP, mrp);
            values.put(KEY_PRODUCT_SP, sp);
            values.put(KEY_PRODUCT_TAX, tax);
            values.put(KEY_PRODUCT_IMAGE, image);
            Float total = Float.parseFloat(qty) * Float.parseFloat(sp);
            values.put(KEY_PRODUCT_TOTAL, total);

            // 3. insert
            Long id = db.insert(TABLE_CART, // table
                    null, //nullColumnHack
                    values); // key/value -> keys = column names/ values = column values

            // 4. close
            db.close();
            return id.intValue();
        }else{
            updateCart(String.valueOf(mid), did, dname, pid, packid, company, unit_id, unit, qty, mrp, sp, tax, image);
            return mid;
        }
    }

    private Integer checkProduct(String pid,String packid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_CART + " WHERE TRIM(" + KEY_PRODUCT_ID + ") = '"+ pid +"' AND TRIM(" + KEY_PACKAGE_ID + ") = '" + packid + "' LIMIT 1", null);
        Integer ret = 0;
        if(c.getCount() > 0){
            if (c.moveToFirst()){
                do {
                    String column1 = c.getString(0);
                    ret = Integer.parseInt(column1);
                } while(c.moveToNext());
            }
        }
        return ret;
    }

    public void removeCart(String sid){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = new String[]{sid};

        // 3. delete
        db.delete(TABLE_CART, // table
                KEY_ID + "=?", //nullColumnHack
                args); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public void deleteCart(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART,null,null);
    }

    public Integer updateProduct(String sid, String did, String dname, String pid, String packid, String company, String unit_id, String unit, String qty, String mrp, String sp, String tax, String image){
        int i = 0;
        if(qty.equals("0")){
            removeCart(sid);
        }else{
            if(!sid.equals("null") && Integer.parseInt(sid) > 0){
                updateCartDirect(sid, did, dname, pid, packid, company, unit_id, unit, qty, mrp, sp, tax, image);
                i = Integer.parseInt(sid);
            }else {
                i = addProduct(did, dname, pid, packid, company, unit_id, unit, qty, mrp, sp, tax, image);
            }

        }
        return i;
    }

    private void updateCart(String sid, String did, String dname, String pid, String packid, String company, String unit_id, String unit, String qty, String mrp, String sp, String tax, String image){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        // 2. build query
        ContentValues values = new ContentValues();
        values.put(KEY_DEAL_ID, did);
        values.put(KEY_DEAL_NAME, dname);
        values.put(KEY_PRODUCT_ID, pid);
        values.put(KEY_PACKAGE_ID, packid);
        values.put(KEY_PRODUCT_COMPANY, company);
        values.put(KEY_PRODUCT_UNIT_ID, unit_id);
        values.put(KEY_PRODUCT_UNIT, unit);
        Integer totalQty = getQty(Integer.parseInt(sid)) + Integer.parseInt(qty);
        values.put(KEY_PRODUCT_QTY, totalQty);
        values.put(KEY_PRODUCT_MRP, mrp);
        values.put(KEY_PRODUCT_SP, sp);
        values.put(KEY_PRODUCT_IMAGE, image);
        Float total = totalQty * Float.parseFloat(sp);
        Float totalTax = totalQty * Float.parseFloat(tax);
        values.put(KEY_PRODUCT_TAX, totalTax);
        values.put(KEY_PRODUCT_TOTAL, total);

        String[] args = new String[]{sid};
        db.update(TABLE_CART, values,KEY_ID + "=?",args);
    }

    private void updateCartDirect(String sid, String did, String dname, String pid, String packid, String company, String unit_id, String unit, String qty, String mrp, String sp, String tax, String image){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        // 2. build query
        ContentValues values = new ContentValues();
        values.put(KEY_DEAL_ID, did);
        values.put(KEY_DEAL_NAME, dname);
        values.put(KEY_PRODUCT_ID, pid);
        values.put(KEY_PACKAGE_ID, packid);
        values.put(KEY_PRODUCT_COMPANY, company);
        values.put(KEY_PRODUCT_UNIT_ID, unit_id);
        values.put(KEY_PRODUCT_UNIT, unit);
        values.put(KEY_PRODUCT_QTY, qty);
        values.put(KEY_PRODUCT_MRP, mrp);
        values.put(KEY_PRODUCT_SP, sp);
        values.put(KEY_PRODUCT_TAX, tax);
        values.put(KEY_PRODUCT_IMAGE, image);
        Float total = Float.parseFloat(qty) * Float.parseFloat(sp);
        values.put(KEY_PRODUCT_TOTAL, total);

        String[] args = new String[]{sid};
        db.update(TABLE_CART, values,KEY_ID + "=?",args);
    }

    public Integer getQty(Integer sid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT " + KEY_PRODUCT_QTY + " FROM " + TABLE_CART + " WHERE TRIM(" + KEY_ID + ") = '"+ sid +"'", null);
        Integer ret = 0;
        if(c.getCount() > 0){
            if (c.moveToFirst()){
                do {
                    String column1 = c.getString(0);
                    ret = Integer.parseInt(column1);
                } while(c.moveToNext());
            }
        }
        return ret;
    }

    public Integer getQtyByPackID(String packid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT " + KEY_PRODUCT_QTY + " FROM " + TABLE_CART + " WHERE TRIM(" + KEY_PACKAGE_ID + ") = '"+ packid +"'", null);
        Integer ret = 0;
        if(c.getCount() > 0){
            if (c.moveToFirst()){
                do {
                    String column1 = c.getString(0);
                    ret = Integer.parseInt(column1);
                } while(c.moveToNext());
            }
        }
        return ret;
    }

    public String getCartTotal(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT " + KEY_PRODUCT_TOTAL + " FROM " + TABLE_CART, null);
        Float ret = 0.0f;
        if(c.getCount() > 0){
            if (c.moveToFirst()){
                do {
                    Float column1 = Float.parseFloat(c.getString(0));
                    ret = ret + column1;
                } while(c.moveToNext());
            }
        }
        return String.valueOf(ret);
    }

    public String getCartTaxTotal(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT " + KEY_PRODUCT_TAX + " FROM " + TABLE_CART, null);
        Float ret = 0.0f;
        if(c.getCount() > 0){
            if (c.moveToFirst()){
                do {
                    Float column1 = Float.parseFloat(c.getString(0));
                    ret = ret + column1;
                } while(c.moveToNext());
            }
        }
        return String.valueOf(ret);
    }

    public String getCartMRPTotal(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT " + KEY_PRODUCT_MRP + ", " + KEY_PRODUCT_QTY + " FROM " + TABLE_CART, null);
        Float ret = 0.0f;
        if(c.getCount() > 0){
            if (c.moveToFirst()){
                do {
                    Float column1 = Float.parseFloat(c.getString(0));
                    Integer column2 = Integer.parseInt(c.getString(1));
                    ret = ret + (column1 * column2);
                } while(c.moveToNext());
            }
        }
        return String.valueOf(ret);
    }

    public String getCartCount(){
        SQLiteDatabase db = this.getReadableDatabase();
        // 2. build query
        Cursor cursor =
                db.query(TABLE_CART, // a. table
                        COLUMNS, // b. column names
                        null, // c. selections
                        null,   // where clause
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        return String.valueOf(cursor.getCount());
    }

    public JSONArray getCart(){
        return getResults();
    }

    private JSONArray getResults()
    {
        SQLiteDatabase db = this.getReadableDatabase();

        String searchQuery = "SELECT  * FROM " + TABLE_CART;
        Cursor cursor = db.rawQuery(searchQuery, null );
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for( int i=0 ;  i< totalColumn ; i++ )
            {
                if( cursor.getColumnName(i) != null )
                {
                    try
                    {
                        if( cursor.getString(i) != null )
                        {
                            rowObject.put(cursor.getColumnName(i) ,  cursor.getString(i) );
                        }
                        else
                        {
                            rowObject.put( cursor.getColumnName(i) ,  "" );
                        }
                    }
                    catch( Exception e )
                    {
                        e.printStackTrace();
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        return resultSet;
    }
}
