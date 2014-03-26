package com.lixtracking.lt.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.lixtracking.lt.MainActivity;
import com.lixtracking.lt.R;
import com.lixtracking.lt.common.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kbushko on 03/22/14.
 */

public class LoginActivity extends Activity implements View.OnClickListener{

    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PASSWORD = "password";

    EditText eUserName = null;
    EditText ePassword = null;

    private String message = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login);

        eUserName = (EditText)findViewById(R.id.eUsername);
        ePassword = (EditText)findViewById(R.id.ePassword);

        Button SigIn = (Button)findViewById(R.id.bSigIn);
        Button Registered = (Button)findViewById(R.id.bRegistration);

        SigIn.setOnClickListener(this);
        Registered.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bSigIn:
                if(eUserName.length() == 0 || ePassword.length() == 0){
                    message = "Please complate fields";
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle("Error");
                    builder.setMessage(message);
                    builder.setCancelable(true);
                    builder.setPositiveButton("cancel",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else{
                    new LoginTask().execute(URL.host + "VerifyUserLogin");
                }
                break;
            case R.id.bRegistration:
                //Intent intent = new Intent(this, RegisteredActivity.class);
                //startActivity(intent);
                break;
            default: break;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        //overridePendingTransition(R.anim.in_a,R.anim.out_a);
    }

    /**********************************************************************************************/
    /* Login async task */
    /**********************************************************************************************/
    class LoginTask extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog progressDialog = null;
        boolean result = false;
        JSONObject User;
        int userId;
        HttpResponse httpResponse;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(LoginActivity.this,"Is running"," please wait...",true);
        }

        @Override
        protected Boolean doInBackground(String... url) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 2000);
            HttpConnectionParams.setSoTimeout(httpParams, 2000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpPost httpPost = new HttpPost(url[0]);
            Log.i("info",url[0]);
            Log.i("info","user_id : " + eUserName.getText());
            Log.i("info","password : " + ePassword.getText());

            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            List<NameValuePair> nameValuePairsList = new ArrayList<NameValuePair>(2);
            nameValuePairsList.add(new BasicNameValuePair("user_id", eUserName.getText().toString()));
            nameValuePairsList.add(new BasicNameValuePair("password",ePassword.getText().toString()));

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairsList));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                message = EntityUtils.toString(httpEntity);
                Log.i("info", message);
                result = VerifyUserLogin(message);

            } catch (IOException e) {
                message = "Server doesn't response";
                result = false;
                e.printStackTrace();
            }
            return true;//result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            progressDialog.cancel();


            if (result == true) {
                // User success login
                Intent intent = new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }else{
                // Error login
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Error");
                builder.setMessage(message);
                builder.setCancelable(true);
                builder.setPositiveButton("cancel",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }
    /**********************************************************************************************/
    private boolean VerifyUserLogin(String xml) {
        boolean result = false;
        String tmp = "";
        try {
            XmlPullParser xpp = prepareXpp(xml);
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.START_DOCUMENT:
                        Log.i("info", "START_DOCUMENT");
                        break;
                    case XmlPullParser.START_TAG:
                        Log.i("info", "START_TAG: " + xpp.getName() + ", depth = " + xpp.getDepth() + ", attrCount = " + xpp.getAttributeCount());
                        tmp = "";
                        for (int i = 0; i < xpp.getAttributeCount(); i++) {
                            tmp = tmp + xpp.getAttributeName(i) + " = " + xpp.getAttributeValue(i) + ", ";
                        }
                        if (!TextUtils.isEmpty(tmp))
                            Log.i("info", "Attributes: " + tmp);
                        break;
                    case XmlPullParser.END_TAG:
                        Log.i("info", "END_TAG: " + xpp.getName());
                        break;
                    case XmlPullParser.TEXT:
                        Log.i("info", "text = " + xpp.getText());
                        result = Boolean.parseBoolean(xpp.getText());
                        Log.i("info"," result : " + Boolean.toString(result));
                        break;
                    default:
                        break;
                }
                xpp.next();
            }
            Log.d("info", "END_DOCUMENT");
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    XmlPullParser prepareXpp(String data) throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader(data));
        return xpp;
    }
}
