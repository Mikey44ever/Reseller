package com.store.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import com.store.dbs.DBHelper;
import com.store.util.RESTAPICaller;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

import static com.store.util.Constants.COD_AREAS_URL;
import static com.store.util.Constants.COD_SHIPPING_RATES;
import static com.store.util.Constants.THIRD_PARTY_SHIPPING_RATES;


/**
 * Created by MorphsysLaptop on 23/10/2017.
 */

public class DBCreateActivity extends AppCompatActivity {

    private DBHelper mydb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            mydb = new DBHelper(this);
            //mydb.doDBRefresh();
            callRESTAPIForData();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            Intent intent = new Intent();
            setResult(CommonStatusCodes.SUCCESS, intent);
            finish();
        }
    }

    private void callRESTAPIForData(){
        callAPICaller(COD_SHIPPING_RATES,null,"codShippingRates");
        callAPICaller(COD_AREAS_URL,null,"codAreas");
        callAPICaller(THIRD_PARTY_SHIPPING_RATES,null,"thirdPartyShippingRates");
    }

    private void callAPICaller(String url,String data,String identifier){
        new CallAPICaller().execute(url,identifier,data);
    }

    private void saveCodShippingRates(String data){
        try{
            JsonParser parser = new JsonParser();
            JsonElement tradeElement = parser.parse(data);
            JsonArray jsonData = tradeElement.getAsJsonObject().getAsJsonArray("cod_shipping_rates");
            for(JsonElement obj:jsonData){
                int rateId=Integer.valueOf(obj.getAsJsonObject().get("rate_id").toString().replaceAll("\"",""));
                int quantity=Integer.valueOf(obj.getAsJsonObject().get("quantity").toString().replaceAll("\"",""));
                double priceRate = Double.valueOf(obj.getAsJsonObject().get("price_rate").toString().replaceAll("\"",""));
                mydb.insertCodShippingRates(rateId,quantity,priceRate);
            }
        }catch (Exception e){
            Log.i(null,e.getMessage());
        }
        Cursor codShippingRatesCursor = mydb.getAllData(mydb.COD_SHIPPING_RATES);
        int count = codShippingRatesCursor.getCount();
        Log.i(null,String.valueOf(count));
    }

    private void saveThirdPartyShippingRates(String data){
        try{
            JsonParser parser = new JsonParser();
            JsonElement tradeElement = parser.parse(data);
            JsonArray jsonData = tradeElement.getAsJsonObject().getAsJsonArray("third_party_shipping_rates");
            for(JsonElement obj:jsonData){
                int rateId=Integer.valueOf(obj.getAsJsonObject().get("rate_id").toString().replaceAll("\"",""));
                int quantity=Integer.valueOf(obj.getAsJsonObject().get("quantity").toString().replaceAll("\"",""));
                double lbcRate = Double.valueOf(obj.getAsJsonObject().get("lbc_rate").toString().replaceAll("\"",""));
                double jrsRate = Double.valueOf(obj.getAsJsonObject().get("jrs_rate").toString().replaceAll("\"",""));
                mydb.insertThirdPartyShippingRates(rateId,quantity,lbcRate,jrsRate);
            }
        }catch (Exception e){
            Log.i(null,e.getMessage());
        }
        Cursor thirdPartyShippingRatesursor = mydb.getAllData(mydb.THIRD_PARTY_SHIPPING_RATES);
        int count = thirdPartyShippingRatesursor.getCount();
        Log.i(null,String.valueOf(count));
    }

    private void saveCodAreas(String data){
        try{
            JsonParser parser = new JsonParser();
            JsonElement tradeElement = parser.parse(data);
            JsonArray jsonData = tradeElement.getAsJsonObject().getAsJsonArray("cod_areas");
            for(JsonElement obj:jsonData){
                int areaId=Integer.valueOf(obj.getAsJsonObject().get("area_id").toString().replaceAll("\"",""));
                String province=obj.getAsJsonObject().get("province").toString().replaceAll("\"","");
                String city = obj.getAsJsonObject().get("city").toString().replaceAll("\"","");
                mydb.insertCodAreas(areaId,province,city);
            }
        }catch (Exception e){
            Log.i(null,e.getMessage());
        }
        Cursor codAreasCursor = mydb.getAllData(mydb.COD_AREAS);
        int count = codAreasCursor.getCount();
        Log.i(null,String.valueOf(count));
    }

    public class CallAPICaller extends AsyncTask<String, Void, String> {

        private String identifier;
        @Override
        protected String doInBackground(String... strings) {
            try{
                String jsonParam=null;
                String url = strings[0];
                identifier=strings[1];

                if(strings.length>2)
                    jsonParam=strings[2];

                RESTAPICaller restapiCaller = new RESTAPICaller();
                return restapiCaller.doAPICall(url, jsonParam);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            switch (identifier){
                case "codShippingRates" :
                    saveCodShippingRates(s);
                    break;
                case "codAreas" :
                    saveCodAreas(s);
                    break;
                case "thirdPartyShippingRates" :
                    saveThirdPartyShippingRates(s);
                    break;
            }
        }
    }
}
