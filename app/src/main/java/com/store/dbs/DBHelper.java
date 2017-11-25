package com.store.dbs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;

public class DBHelper extends SQLiteOpenHelper{
    public static final String DATABASE_NAME = "RESELLER.db";
    public static final String APP_SESSION_HISTORY = "APP_SESSION_HISTORY";
    public static final String COD_AREAS = "COD_AREAS";
    public static final String COD_SHIPPING_RATES = "COD_SHIPPING_RATES";
    public static final String THIRD_PARTY_SHIPPING_RATES = "THIRD_PARTY_SHIPPING_RATES";
    private HashMap hp;
    String timeStamp = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss a").format(new Date());

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS APP_SESSION_HISTORY('SESSION_ID' number PRIMARY KEY, " +
                "'SESSION_DATE' text)");
        db.execSQL("CREATE TABLE IF NOT EXISTS COD_AREAS('AREA_ID' number PRIMARY KEY, " +
                "'PROVINCE' text, 'CITY' text)");
        db.execSQL("CREATE TABLE IF NOT EXISTS COD_SHIPPING_RATES('RATE_ID' number PRIMARY KEY, " +
                "'QUANTITY' number, 'PRICE_RATE' number)");
        db.execSQL("CREATE TABLE IF NOT EXISTS THIRD_PARTY_SHIPPING_RATES('RATE_ID' number PRIMARY KEY, " +
                "'QUANTITY' number, 'LBC_RATE' number,'JRS_RATE' number)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS APP_SESSION_HISTORY");
        db.execSQL("DROP TABLE IF EXISTS COD_AREAS");
        db.execSQL("DROP TABLE IF EXISTS COD_SHIPPING_RATES");
        db.execSQL("DROP TABLE IF EXISTS THIRD_PARTY_SHIPPING_RATES");
        onCreate(db);
    }

    public void doDBRefresh(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS COD_AREAS");
        db.execSQL("DROP TABLE IF EXISTS COD_SHIPPING_RATES");
        db.execSQL("DROP TABLE IF EXISTS THIRD_PARTY_SHIPPING_RATES");
        onCreate(db);
    }

    public void insertCodAreas(int areaId,String province,String city)throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("area_id",areaId);
        contentValues.put("province",province);
        contentValues.put("city",city);
        db.insert(COD_AREAS, null, contentValues);
    }

    public void insertThirdPartyShippingRates(int rateId,int qty,double lbcRate,double jrsRate){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("rate_id",rateId);
        contentValues.put("quantity",qty);
        contentValues.put("lbc_rate",lbcRate);
        contentValues.put("jrs_rate",jrsRate);
        db.insert(THIRD_PARTY_SHIPPING_RATES, null, contentValues);
    }

    public void insetSession(int sessionId)throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("session_id",sessionId);
        contentValues.put("session_date",timeStamp);
        db.insert(APP_SESSION_HISTORY, null, contentValues);
    }

    public void insertCodShippingRates(int rateId,int qty,double priceRate)throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("rate_id",rateId);
        contentValues.put("quantity",qty);
        contentValues.put("price_rate",priceRate);
        db.insert(COD_SHIPPING_RATES, null, contentValues);
    }

    public Cursor getAllData(String table) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from "+table, null);
        return res;
    }

    public Cursor getAllProductsWithPrice() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("SELECT DISTINCT pp.PRODUCT_ID,pp.PRODUCT_PRICE,P.PRODUCT_NAME" +
                " FROM PRODUCTS p INNER JOIN PRODUCTS_PRICE pp" +
                " ON p.PRODUCT_ID = pp.PRODUCT_ID ORDER BY p.PRODUCT_NAME", null);
        return res;
    }

    public Cursor getSpecificProduct(String table,String column,String value) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from "+table+" where "+column+" = ?", new String[]{value});
        return res;
    }

    public Cursor getGenericData(String query){
        Cursor res = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            res =  db.rawQuery(query, null);
        }catch (Exception e){
            Log.e("",e.getMessage());
        }
        return res;
    }
}
