package com.store.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.store.R;
import com.store.dbs.DBHelper;
import com.store.pojos.CustomerPOJO;
import com.store.pojos.builders.CustomerPOJOBuilder;
import com.store.util.RESTAPICaller;

import static com.store.util.Constants.*;
import static com.store.util.Strings.*;

public class LoginActivity extends AppCompatActivity {

    private DBHelper mydb;
    Button btnSignUp, btnSignIn, btnExit;
    TextView txtUsername, txtPassword;
    Intent mainIntent;

    private String userName,password,userId;
    private boolean isFirstSession;
    private Cursor sessionCursor;
    private CustomerPOJO customerPOJO;
    private Bundle bundle;
    private String userGroupId;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PRODUCT_RETRIEVAL_REQUEST_CODE){
            String jsonString=data.getStringExtra("products") != null ? data.getStringExtra("products") : "";
           // initializeDB(true,jsonString);//true for testing only
        } else if(requestCode == MAIN_DRAWER_ACTIVITY_REQUEST_CODE){
            Intent intent= new Intent(LoginActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else if(requestCode == DB_CREATE_REQUEST_CODE){
            if (isFirstSession)
                insertSession(1);
            else
                getInsertLastSession();
        } else super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        Intent intent = new Intent(getApplicationContext(), MainScreenActivity.class);
        mainIntent= intent;
        bundle = new Bundle();

        initializeDB();
        initializeObjects();
        setListeners();
        checkSessions();
    }

    private CustomerPOJO setCustomerPOJO(Cursor userCursor){
        userGroupId=userCursor.getString(userCursor.getColumnIndex("USER_GROUP_ID"));

        CustomerPOJO customer = new CustomerPOJOBuilder()
                .fName(userCursor.getString(userCursor.getColumnIndex("FIRSTNAME")))
                .lName(userCursor.getString(userCursor.getColumnIndex("LASTNAME")))
                .mobile(userCursor.getString(userCursor.getColumnIndex("CONTACT")))
                .email(userCursor.getString(userCursor.getColumnIndex("EMAIL")))
                .fb(userCursor.getString(userCursor.getColumnIndex("FACEBOOK")))
                .address(userCursor.getString(userCursor.getColumnIndex("ADDRESS")))
                .cityTown(userCursor.getString(userCursor.getColumnIndex("CITY")))
                .brgy(userCursor.getString(userCursor.getColumnIndex("BRGY")))
                .landmark(userCursor.getString(userCursor.getColumnIndex("LANDMARKS")))
                .note(userCursor.getString(userCursor.getColumnIndex("NOTES")))
                .build();
        return customer;
    }

    private void insertSession(int sessionId){
        try{
            mydb.insetSession(sessionId);
            checkUserLogStatus();
        }catch (Exception e){
            Log.i(null,e.getMessage());
        }
    }

    public void checkUserLogStatus(){
        Cursor userCursor=mydb.getLoggedInUser();
        if(userCursor.getCount()==1){
            userCursor.moveToFirst();
            String fName=userCursor.getString(userCursor.getColumnIndex("FIRSTNAME"));
            String lName=userCursor.getString(userCursor.getColumnIndex("LASTNAME"));
            String userId=userCursor.getString(userCursor.getColumnIndex("USER_ID"));
            bundle.putSerializable(CUSTOMER_POJO_SERIAL_KEY,setCustomerPOJO(userCursor));

            mainIntent.putExtra("fName",fName);
            mainIntent.putExtra("lName",lName);
            mainIntent.putExtra("userId",userId);
            mainIntent.putExtra("userGroupId",userGroupId);
            mainIntent.putExtras(bundle);
            startActivityForResult(mainIntent,MAIN_DRAWER_ACTIVITY_REQUEST_CODE);
        }
    }

    private void getInsertLastSession(){
        sessionCursor.moveToPosition(sessionCursor.getCount() - 1);
        insertSession((sessionCursor.getInt(sessionCursor.getColumnIndex("SESSION_ID")))+1);
    }

    private void checkSessions(){
        boolean isConnected=haveNetworkConnection();
        Cursor sessionCursor = mydb.getAllData(mydb.APP_SESSION_HISTORY);
        this.sessionCursor=sessionCursor;
        int count = sessionCursor.getCount();
        if(count==0){
            if(!isConnected)
                showConstantDialog(LoginActivity.this,NOTICE.name(),NOTICE_FIRST_SESSION,mainIntent,"",false);
            else{
                Intent dbCreateIntent = new Intent(getApplicationContext(),DBCreateActivity.class);
                startActivityForResult(dbCreateIntent,DB_CREATE_REQUEST_CODE);
                isFirstSession=true;
            }
        }else{
            if(isConnected){
                Intent dbCreateIntent = new Intent(getApplicationContext(),DBCreateActivity.class);
                startActivityForResult(dbCreateIntent,DB_CREATE_REQUEST_CODE);
            }
        }
    }

    private void initializeObjects(){
        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnExit = (Button) findViewById(R.id.btnExit);
        txtUsername = (TextView) findViewById(R.id.txtUsername);
        txtPassword = (TextView) findViewById(R.id.txtPassword);

        txtUsername.setText("rzbanal");
        txtPassword.setText("12345");
    }

    private void initializeDB(){
        DBHelper mydb=new DBHelper(this);
        this.mydb = mydb;
    }

    private boolean haveNetworkConnection() {
        boolean isWifiConnected = false;
        boolean isMobileDataConnected = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase(WIFI.name()))
                if (ni.isConnected())
                    isWifiConnected = true;
            if (ni.getTypeName().equalsIgnoreCase(MOBILE.name()))
                if (ni.isConnected())
                    isMobileDataConnected = true;
        }
        return isWifiConnected || isMobileDataConnected;
    }

    private void setListeners(){
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    performLoginCheck(txtUsername.getText().toString(),txtPassword.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
                startActivityForResult(intent, REGISTRATION_REQUEST_CODE);
            }
        });
    }

    private void performLoginCheck(String username,String password) throws JSONException {
        if(haveNetworkConnection()) {
            JSONObject postDataParams = new JSONObject();
            postDataParams.put("username", username);
            postDataParams.put("password", password);

            JSONObject userObj = new JSONObject();
            userObj.put("user",postDataParams);

            new LoginAPI().execute(LOGIN_URL,userObj.toString());
        }else{
            Cursor userCursor=mydb.checkUser(username,password);
            if(userCursor==null)
                showConstantDialog(LoginActivity.this,"USER AUTHENTICATION",USER_NAME_PASSWORD_NOT_EXISTING,mainIntent,"",false);
            else{
                userCursor.moveToFirst();
                bundle.putSerializable(CUSTOMER_POJO_SERIAL_KEY,setCustomerPOJO(userCursor));

                String fName=userCursor.getString(userCursor.getColumnIndex("FIRSTNAME"));
                String lName=userCursor.getString(userCursor.getColumnIndex("LASTNAME"));
                String userId=userCursor.getString(userCursor.getColumnIndex("USER_ID"));
                mydb.updateLoggedInStatus(userId,"true");
                startMainActivity("",USER_AUTHENTICATED,fName,lName,userId);
            }
        }
    }

    public void startMainActivity(String status,String statusMsg,String fName,String lName,String userId){
        showConstantDialog(LoginActivity.this,"USER AUTHENTICATION",statusMsg,mainIntent,status,false);
        if(!status.equalsIgnoreCase("FAILED")){
            mainIntent.putExtra("fName",fName);
            mainIntent.putExtra("lName",lName);
            mainIntent.putExtra("userId",userId);
            mainIntent.putExtra("userGroupId",userGroupId);
            mainIntent.putExtras(bundle);
            startActivityForResult(mainIntent,MAIN_DRAWER_ACTIVITY_REQUEST_CODE);
        }
    }

    public void callAfterLoginAPI(String message){
        try{
            JsonParser parser = new JsonParser();
            JsonElement tradeElement = parser.parse(message);
            JsonObject userObj = tradeElement.getAsJsonObject().getAsJsonObject("user");

            String status = userObj.get("status").toString().replaceAll("\"","");
            String statusMsg = userObj.get("status_msg").toString().replaceAll("\"","");

            showConstantDialog(LoginActivity.this,"USER AUTHENTICATION",statusMsg,mainIntent,status,false);
            if(!status.equalsIgnoreCase("FAILED")){
                String fName = userObj.get("firstname").toString().replaceAll("\"","");
                String lName = userObj.get("lastname").toString().replaceAll("\"","");

                String userName = userObj.get("username").toString().replaceAll("\"","");
                String userId = userObj.get("user_id").toString().replaceAll("\"","");
                String password = userObj.get("password").toString().replaceAll("\"","");
                mydb.saveUpdateUser(userId,userName,password,userObj);
                mydb.updateLoggedInStatus(userId,"true");

                Cursor userCursor=mydb.getLoggedInUser();
                userCursor.moveToFirst();
                bundle.putSerializable(CUSTOMER_POJO_SERIAL_KEY,setCustomerPOJO(userCursor));

                startMainActivity(status,statusMsg,fName,lName,userId);
            }
        }catch (Exception e){
            Log.i(null,e.getMessage());
        }
    }

    public class LoginAPI extends AsyncTask<String, Void, String>{

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
            callAfterLoginAPI(s);
        }
    }
}
