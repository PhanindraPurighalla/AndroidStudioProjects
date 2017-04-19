package com.budgetplanner.phanindra.budgetplanner;


import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditTypeActivity extends Activity {

    EditText txtTypeCode;
    EditText txtTypeDesc;
    Button btnSave;
    Button btnDelete;

    String typeId;

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    // single transaction type url
    private static final String url_type_details = "http://10.0.2.2/BudgetPlanner/types";

    // url to update transaction type
    private static final String url_update_type = "http://10.0.2.2/BudgetPlanner/types";

    // url to delete transaction type
    private static final String url_delete_type = "http://10.0.2.2/BudgetPlanner/types";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_TYPE_OBJ = "type";
    private static final String TAG_TYPE = "Type";
    private static final String TAG_TYPE_ID = "id";
    private static final String TAG_TYPE_CODE = "type_code";
    private static final String TAG_TYPE_DESC = "type_desc";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_type);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // save button
        btnSave = (Button) findViewById(R.id.btnSave);
        btnDelete = (Button) findViewById(R.id.btnDelete);

        // getting transaction type details from intent
        Intent i = getIntent();

        // getting transaction type id (typeId) from intent
        typeId = i.getStringExtra(TAG_TYPE_ID);

        // Getting complete transaction type details in background thread
        new GetTypeDetails().execute();

        // save button click event
        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // starting background task to update transaction type
                new SaveTypeDetails().execute();
            }
        });

        // Delete button click event
        btnDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // deleting transaction type in background thread
                new DeleteType().execute();
            }
        });

    }

    /**
     * Background Async Task to Get complete transaction type details
     */
    class GetTypeDetails extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EditTypeActivity.this);
            pDialog.setMessage("Loading transaction type details. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Getting transaction type details in background thread
         */
        protected String doInBackground(String... params) {

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // Check for success tag
                    int success;
                    try {
                        // Building Parameters
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("typeId", typeId));

                        // getting transaction type details by making HTTP request
                        // Note that transaction type details url will use GET request
                        JSONObject json = jsonParser.makeHttpRequest(
                                url_type_details, "GET", params);

                        // check your log for json response
                        Log.d("One Trans Type Details", json.toString());

                        JSONObject typeObj = json
                                .getJSONObject(TAG_TYPE_OBJ); // JSON Array
                        if (typeObj != null) {
                            // successfully received transaction type details
                            // get the transaction type object from JSON Object

                            // transaction type with this typeId found
                            // Edit Text
                            txtTypeCode = (EditText) findViewById(R.id.inputTypeCode);
                            txtTypeDesc = (EditText) findViewById(R.id.inputTypeDesc);

                            // display transaction type data in EditText
                            txtTypeCode.setText(typeObj.getJSONObject(TAG_TYPE).getString(TAG_TYPE_CODE));
                            txtTypeDesc.setText(typeObj.getJSONObject(TAG_TYPE).getString(TAG_TYPE_DESC));

                        } else {
                            // transaction type with typeId not found
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once got all details
            pDialog.dismiss();
        }
    }

    /**
     * Background Async Task to Save transaction type Details
     */
    class SaveTypeDetails extends AsyncTask<String, String, String> {

        // getting updated data from EditTexts
        String typeCode = txtTypeCode.getText().toString();
        String typeDesc = txtTypeDesc.getText().toString();

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EditTypeActivity.this);
            pDialog.setMessage("Saving transaction type ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Saving transaction type
         */
        protected String doInBackground(String... args) {

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_TYPE_ID, typeId));
            params.add(new BasicNameValuePair(TAG_TYPE_CODE, typeCode));
            params.add(new BasicNameValuePair(TAG_TYPE_DESC, typeDesc));

            // sending modified data through http request
            // Notice that update transaction type url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_update_type,
                    "POST", params);

            // check json typeId tag
            try {
                String typeId = json.getJSONObject(TAG_TYPE_OBJ).getJSONObject(TAG_TYPE).getString(TAG_TYPE_ID);

                if (typeId != null) {
                    // successfully updated
                    Intent i = getIntent();
                    // send result code 100 to notify about transaction type update
                    setResult(100, i);
                    finish();
                } else {
                    // failed to update transaction type
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once transaction type updated
            pDialog.dismiss();
        }
    }

    /*****************************************************************
     * Background Async Task to Delete Transaction type
     * */
    class DeleteType extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EditTypeActivity.this);
            pDialog.setMessage("Deleting Transaction type...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Deleting transaction type
         */
        protected String doInBackground(String... args) {

            // Check for success tag
            int success;
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("typeId", typeId));

            // getting transaction type details by making HTTP request
            JSONObject json = jsonParser.makeHttpRequest(
                    url_delete_type, "DELETE", params);

            // check your log for json response
            Log.d("Delete Transaction Type", json.toString());
            // transaction type successfully deleted
            // notify previous activity by sending code 100
            Intent i = getIntent();
            // send result code 100 to notify about product deletion
            setResult(100, i);
            finish();

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once transaction type deleted
            pDialog.dismiss();

        }

    }
}