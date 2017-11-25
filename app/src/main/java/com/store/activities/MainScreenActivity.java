package com.store.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.store.R;
import com.store.dbs.DBHelper;
import com.store.drawables.TextDrawable;
import com.store.pojos.CODShippingRatesPOJO;
import com.store.pojos.CustomerPOJO;
import com.store.pojos.JRSLBCShippingRatesPOJO;
import com.store.pojos.OrderPOJO;
import com.store.pojos.builders.CODShippingRatesPOJOBuilder;
import com.store.pojos.builders.CustomerPOJOBuilder;
import com.store.pojos.builders.JRSLBCShippingRatePOJOBuilder;
import com.store.pojos.builders.OrderPOJOBuilder;
import com.store.sales.adapter.CODAreasAdapter;
import com.store.sales.adapter.CODShippingRatesAdapter;
import com.store.sales.adapter.GridAdapter;
import com.store.sales.adapter.JRSLBCShippingRatesAdapter;
import com.store.sales.adapter.OrderAdapter;
import com.store.util.RESTAPICaller;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;

import static com.store.util.Constants.*;


public class MainScreenActivity extends AppCompatActivity {

    private GridView gridview;
    public static int navItemIndex = 0;
    public static String CURRENT_TAG = TAG_HOME;
    private ListView commonListView;
    private View commonView, commonTitleView;
    private TextView commonTextViewHeader;
    private DBHelper myDb;
    private CustomerPOJO customerPOJO;
    private String methodForReflection,userGroupId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        myDb = new DBHelper(this);
        customerPOJO = (CustomerPOJO) getIntent().getSerializableExtra(CUSTOMER_POJO_SERIAL_KEY);
        userGroupId = getIntent().getStringExtra("userGroupId");

        Toolbar toolbar = (Toolbar) findViewById(R.id.mainToolbar);
        toolbar.setTitle("");
        toolbar.setBackgroundResource(R.drawable.kirei_logo);
        setSupportActionBar(toolbar);

        ArrayList mThumbs= getThumbs();

        gridview = (GridView) findViewById(R.id.gridView);
        GridAdapter gridAdapter = new GridAdapter(MainScreenActivity.this,R.layout.row_grid, mThumbs);
        gridAdapter.setActivity(MainScreenActivity.this);
        gridview.setAdapter(gridAdapter);

        setUpGridViewView();
    }

    private ArrayList getThumbs(){
        if(userGroupId.equals("43"))
            return new ArrayList(Arrays.asList(getResources().getStringArray(R.array.reseller_grid_array)));
        else
            return new ArrayList();
    }

    private void showOrderHistory(){
        ArrayList<OrderPOJO> orderPOJOs = new ArrayList<OrderPOJO>();
        OrderPOJO orderPOJO = new OrderPOJOBuilder()
                .poId("100001")
                .date("2017-11-15")
                .name("Geroge E. W. Bush")
                .qty("1")
                .build();

        orderPOJOs.add(orderPOJO);

        orderPOJO = new OrderPOJOBuilder()
                .poId("100002")
                .date("2017-11-15")
                .name("The quick brown fox jumps over the lazy dog")
                .qty("44")
                .build();

        orderPOJOs.add(orderPOJO);

        ArrayAdapter adapter = new OrderAdapter(MainScreenActivity.this,R.layout.orders,orderPOJOs);

        showCommonPopUp(R.layout.orders_header, adapter, R.string.order_history, R.drawable.add_cart, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent orderIntent = new Intent(getApplicationContext(),MainScreenActivity.class);
                startActivityForResult(orderIntent,CREATE_ORDER_REQUEST_CODE);
            }
        });
    }

    private void resetReflectVariables(){
        methodForReflection=null;
    }

    private void showCODShippingRates(){
        methodForReflection="showCODShippingRates";
        callAPICaller(COD_SHIPPING_RATES,null);
    }

    private void showCODAreas(){
        methodForReflection="showCODAreas";
        callAPICaller(COD_AREAS_URL,null);
    }

    private void showJRSLBCShippingRates(){
        methodForReflection="showJRSLBCShippingRates";
        callAPICaller(THIRD_PARTY_SHIPPING_RATES,null);
    }

    private void callAPICaller(String url,String data){
        new CallRestAPICaller().execute(url,data);
    }

    private void showJRSLBCShippingRatesAfterAPI(String data){
        resetReflectVariables();

        JsonParser parser = new JsonParser();
        JsonElement tradeElement = parser.parse(data);
        JsonArray jsonData = tradeElement.getAsJsonObject().getAsJsonArray("third_party_shipping_rates");

        ArrayList<JRSLBCShippingRatesPOJO> ljRatesPOJOs = new ArrayList<JRSLBCShippingRatesPOJO>();
        JRSLBCShippingRatesPOJO ljPOJO = null;
        for(JsonElement obj:jsonData){
            int qty = Integer.valueOf(obj.getAsJsonObject().get("quantity").toString().replaceAll("\"",""));
            ljPOJO = new JRSLBCShippingRatePOJOBuilder()
                    .qty(qty+" bxs & below")
                    .lbcRate(obj.getAsJsonObject().get("lbc_rate").toString().replaceAll("\"",""))
                    .jrsRate(obj.getAsJsonObject().get("jrs_rate").toString().replaceAll("\"",""))
                    .build();
            ljRatesPOJOs.add(ljPOJO);
        }

        ArrayAdapter adapter = new JRSLBCShippingRatesAdapter(MainScreenActivity.this,R.layout.lj_shipping_rates_header,ljRatesPOJOs);
        showCommonPopUp(R.layout.lj_shipping_rates_header, adapter, R.string.lbc_jrc_shipping_rate,0,null);
    }

    private void showCODShippingRatesAfterAPI(String data){
        resetReflectVariables();

        JsonParser parser = new JsonParser();
        JsonElement tradeElement = parser.parse(data);
        JsonArray jsonData = tradeElement.getAsJsonObject().getAsJsonArray("cod_shipping_rates");

        ArrayList<CODShippingRatesPOJO> srPOJOs = new ArrayList<CODShippingRatesPOJO>();
        CODShippingRatesPOJO csrPOJO = null;
        for(JsonElement obj:jsonData){
            int qty = Integer.valueOf(obj.getAsJsonObject().get("quantity").toString().replaceAll("\"",""));
            csrPOJO = new CODShippingRatesPOJOBuilder()
                    .qty(qty+" bxs & below")
                    .rates(obj.getAsJsonObject().get("price_rate").toString().replaceAll("\"",""))
                    .build();

            srPOJOs.add(csrPOJO);
        }

        ArrayAdapter adapter = new CODShippingRatesAdapter(MainScreenActivity.this,R.layout.cod_shipping_rates,srPOJOs);
        showCommonPopUp(R.layout.cod_shipping_rates_header, adapter, R.string.cod_shipping_rate,0,null);
    }

    private void showCODAreasAfterAPI(String data){
        resetReflectVariables();

        JsonParser parser = new JsonParser();
        JsonElement tradeElement = parser.parse(data);
        JsonArray jsonData = tradeElement.getAsJsonObject().getAsJsonArray("cod_areas");

        ArrayList codAreas = new ArrayList();
        for(JsonElement obj:jsonData){
            codAreas.add(obj.getAsJsonObject().get("city").toString().replaceAll("\"",""));
        }

        CODAreasAdapter adapter = new CODAreasAdapter(MainScreenActivity.this,R.layout.cod_area, codAreas);
        showCommonPopUp(R.layout.cod_areas_header, adapter, R.string.cod_areas,0,null);
    }

    private void showCommonPopUp(int headerId, ArrayAdapter adapter, int popTitleId, int popUpBtn, View.OnClickListener listener){
        commonView = View.inflate(MainScreenActivity.this, R.layout.common_list_view, null);
        commonListView = (ListView) commonView.findViewById(R.id.commonListView);

        commonListView.setAdapter(adapter);
        View headerView = (View) getLayoutInflater().inflate(headerId,null);
        commonListView.addHeaderView(headerView);

        /*commonTextViewHeader=(TextView) commonView.findViewById(R.id.txtHeaderForList);
        commonTextViewHeader.setText("TEST");*/

        commonTitleView=(View) getLayoutInflater().inflate(R.layout.custom_title,null);
        ImageButton action = (ImageButton) commonTitleView.findViewById(R.id.imgActionBtn);
        TextView titleText = (TextView) commonTitleView.findViewById(R.id.txtTitle);

        if(popUpBtn==0)
            action.setImageResource(0);
        else{
            action.setImageResource(popUpBtn);
            action.setOnClickListener(listener);
        }
        titleText.setText(popTitleId);

        showPopUp(commonTitleView);
    }

    private void showPopUp(View title){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainScreenActivity.this);
        builder.setView(commonView);
        builder.setCustomTitle(title);
        builder.setPositiveButton("OK",null);
        builder.show();
    }

    private void setUpGridViewView() {
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String value =(String) parent.getItemAtPosition(position);
                switch (value.toLowerCase()) {
                    case "customer details":
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(CUSTOMER_POJO_SERIAL_KEY,customerPOJO);
                        Intent customerIntent = new Intent(getApplicationContext(),CustomerActivity.class);
                        customerIntent.putExtras(bundle);
                        startActivityForResult(customerIntent,CUSTOMERS_LIST_REQUEST_CODE);
                        break;
                    case "cod areas":
                        showCODAreas();
                        break;
                    case "cod shipping rates":
                        showCODShippingRates();
                        break;
                    case "customer order":
                        navItemIndex = 3;
                        break;
                    case "orders":
                        showOrderHistory();
                        break;
                    case "lbc/jrs shipping rates":
                        showJRSLBCShippingRates();
                        navItemIndex = 5;
                        break;
                    default:
                        navItemIndex = 0;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            myDb.updateLoggedInStatus(getIntent().getStringExtra("userId"),"false");
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class CallRestAPICaller extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try{
                String jsonParam=null;
                String url = strings[0];
                if(strings.length>1)
                    jsonParam=strings[1];

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
            switch (methodForReflection){
                case "showCODAreas" :
                    showCODAreasAfterAPI(s);
                    break;
                case "showCODShippingRates" :
                    showCODShippingRatesAfterAPI(s);
                    break;
                case "showJRSLBCShippingRates" :
                    showJRSLBCShippingRatesAfterAPI(s);
                    break;
            }
        }

        /*private void callAfterAsyncTask(String data){
            Log.i(null,data);
            try{
                Class<?> c = Class.forName("com.store.activities.MainScreenActivity");
                Object gvClass = c.newInstance();
                Class[] argTypes = new Class[] {String.class};

                //queryType = method to be called
                // argType = parameters of the method to be called with type e.g. "queryOne" below
                Method main = c.getDeclaredMethod(methodForReflection, argTypes);
                main.invoke(gvClass,new Object[] {data});
                Log.i(null,"finished reflection...");
            }catch(NoSuchMethodException e){
                e.printStackTrace();
            }catch(IllegalAccessException e){
                e.printStackTrace();
            }catch(InstantiationException e){
                e.printStackTrace();
            }catch(ClassNotFoundException e){
                e.printStackTrace();
            }catch(Exception e){
                e.printStackTrace();
            }
        }*/
    }
}
