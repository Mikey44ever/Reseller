package com.store.dbs;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    public static final String USERS = "USERS";
    public static final String COD_SHIPPING_RATES = "COD_SHIPPING_RATES";
    public static final String THIRD_PARTY_SHIPPING_RATES = "THIRD_PARTY_SHIPPING_RATES";
    private HashMap hp;
    String timeStamp = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss a").format(new Date());

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS USERS('USER_ID' text PRIMARY KEY,'SITE' text,'WEBSITE' text, " +
                "'SPECIAL_ACCT' text,'ID_NO' text,'USER_GROUP_ID' text,'CREATED_BY' text,'USERNAME' text," +
                "'PASSWORD' text,'FIRSTNAME' text,'MIDDLENAME' text,'LASTNAME' text,'EWALLET' text,'EMAIL' text," +
                "'IP' text,'BANK_NAME' text,'ACCOUNT_NAME' text,'ACCOUNT_NO' text,'REFER_BY_ID' text,'CODE' text," +
                "'STATUS' text,'DATE_ADDED' tex,'BIRTHDATE' text,'CONTACT' text,'DATE_MODIFIED' text,'GENDER' text," +
                "'FACEBOOK' text,'ADDRESS' text,'CITY' text,'BRGY' text,'LANDMARKS' text,'NOTES' text,'LOGGED_IN' text)");
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
        db.execSQL("DROP TABLE IF EXISTS USERS");
        db.execSQL("DROP TABLE IF EXISTS APP_SESSION_HISTORY");
        db.execSQL("DROP TABLE IF EXISTS COD_AREAS");
        db.execSQL("DROP TABLE IF EXISTS COD_SHIPPING_RATES");
        db.execSQL("DROP TABLE IF EXISTS THIRD_PARTY_SHIPPING_RATES");
        onCreate(db);
    }

    public void doDBRefresh(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS USERS");
        db.execSQL("DROP TABLE IF EXISTS COD_AREAS");
        db.execSQL("DROP TABLE IF EXISTS COD_SHIPPING_RATES");
        db.execSQL("DROP TABLE IF EXISTS THIRD_PARTY_SHIPPING_RATES");
        onCreate(db);
    }

    private String extractValue(JsonObject obj,String key){
        return obj.get(key) != null ? obj.get(key).toString().replaceAll("\"","") : "";
    }

    public void updateLoggedInStatus(String userId,String loginStatus){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("LOGGED_IN",loginStatus);
        db.update(USERS, contentValues,"USER_ID="+userId,null);
    }

    public Cursor getLoggedInUser() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from USERS where LOGGED_IN='true'", null);
        return res;
    }

    public void saveUpdateUser(String userId,String userName,String password,JsonObject jsonData){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("USER_ID",extractValue(jsonData,"user_id"));
        contentValues.put("SITE",extractValue(jsonData,"site"));
        contentValues.put("WEBSITE",extractValue(jsonData,"website"));
        contentValues.put("SPECIAL_ACCT",extractValue(jsonData,"special_acct"));
        contentValues.put("ID_NO",extractValue(jsonData,"id_no"));
        contentValues.put("USER_GROUP_ID",extractValue(jsonData,"user_group_id"));
        contentValues.put("CREATED_BY",extractValue(jsonData,"created_by"));
        contentValues.put("USERNAME",extractValue(jsonData,"username"));
        contentValues.put("PASSWORD",extractValue(jsonData,"password"));
        contentValues.put("FIRSTNAME",extractValue(jsonData,"firstname"));
        contentValues.put("MIDDLENAME",extractValue(jsonData,"middlename"));
        contentValues.put("LASTNAME",extractValue(jsonData,"lastname"));
        contentValues.put("EWALLET",extractValue(jsonData,"ewallet"));
        contentValues.put("EMAIL",extractValue(jsonData,"email"));
        contentValues.put("IP",extractValue(jsonData,"ip"));
        contentValues.put("BANK_NAME",extractValue(jsonData,"bank_name"));
        contentValues.put("ACCOUNT_NAME",extractValue(jsonData,"account_name"));
        contentValues.put("ACCOUNT_NO",extractValue(jsonData,"account_no"));
        contentValues.put("REFER_BY_ID",extractValue(jsonData,"refer_by_id"));
        contentValues.put("CODE",extractValue(jsonData,"code"));
        contentValues.put("DATE_ADDED",extractValue(jsonData,"date_added"));
        contentValues.put("BIRTHDATE",extractValue(jsonData,"birthdate"));
        contentValues.put("CONTACT",extractValue(jsonData,"contact"));
        contentValues.put("DATE_MODIFIED",extractValue(jsonData,"date_modified"));
        contentValues.put("GENDER",extractValue(jsonData,"gender"));
        contentValues.put("FACEBOOK",extractValue(jsonData,"facebook"));
        contentValues.put("ADDRESS",extractValue(jsonData,"address"));
        contentValues.put("CITY",extractValue(jsonData,"city_town"));
        contentValues.put("BRGY",extractValue(jsonData,"brgy"));
        contentValues.put("LANDMARKS",extractValue(jsonData,"landmarks"));
        contentValues.put("NOTES",extractValue(jsonData,"notes"));

        String query = "SELECT * FROM USERS WHERE user_id= ? AND username= ? AND password= ?";
        Cursor res =  db.rawQuery(query, new String[]{userId,userName,password});
        if(res.getCount()>0){
            db.update(USERS, contentValues,"USER_ID="+userId,null);
        }else{
            db.insert(USERS, null, contentValues);
        }
    }

    public Cursor checkUser(String username,String password){
        Cursor res = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(password.getBytes());
            byte messageDigest[] = digest.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            Log.i(null,hexString.toString());

            SQLiteDatabase db = this.getWritableDatabase();
            String query = "SELECT * FROM USERS WHERE username= ? AND password= ?";
            res =  db.rawQuery(query, new String[]{username,hexString.toString()});
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return res;
    }

    public void insertCodAreas(int areaId,String province,String city)throws Exception{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        String query = "SELECT * FROM "+COD_AREAS+" WHERE area_id= ?";
        Cursor res =  db.rawQuery(query, new String[]{String.valueOf(areaId)});
        if(res.getCount()==0){
            contentValues.put("area_id",areaId);
            contentValues.put("province",province);
            contentValues.put("city",city);
            db.insert(COD_AREAS, null, contentValues);
        }
    }

    public void insertThirdPartyShippingRates(int rateId,int qty,double lbcRate,double jrsRate){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        String query = "SELECT * FROM "+THIRD_PARTY_SHIPPING_RATES+" WHERE rate_id= ?";
        Cursor res =  db.rawQuery(query, new String[]{String.valueOf(rateId)});
        if(res.getCount()==0){
            contentValues.put("rate_id",rateId);
            contentValues.put("quantity",qty);
            contentValues.put("lbc_rate",lbcRate);
            contentValues.put("jrs_rate",jrsRate);
            db.insert(THIRD_PARTY_SHIPPING_RATES, null, contentValues);
        }
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

        String query = "SELECT * FROM "+COD_SHIPPING_RATES+" WHERE rate_id= ?";
        Cursor res =  db.rawQuery(query, new String[]{String.valueOf(rateId)});
        if(res.getCount()==0){
            contentValues.put("rate_id",rateId);
            contentValues.put("quantity",qty);
            contentValues.put("price_rate",priceRate);
            db.insert(COD_SHIPPING_RATES, null, contentValues);
        }
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
