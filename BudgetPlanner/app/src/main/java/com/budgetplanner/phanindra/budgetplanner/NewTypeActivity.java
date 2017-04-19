package com.budgetplanner.phanindra.budgetplanner;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NewTypeActivity extends Activity {

    // Progress Dialog
    private ProgressDialog pDialog;

    JSONParser jsonParser = new JSONParser();
    EditText inputTypeCode;
    EditText inputTypeDesc;

    // url to create new transaction type
    private static String url_create_type = "http://10.0.2.2/BudgetPlanner/types.json";

    // JSON Node names
    //private static final String TAG_SUCCESS = "success";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_type);

        // Edit Text
        inputTypeCode = (EditText) findViewById(R.id.inputTypeCode);
        inputTypeDesc = (EditText) findViewById(R.id.inputTypeDesc);

        // Create button
        Button btnCreateType = (Button) findViewById(R.id.btnCreateType);

        // button click event
        btnCreateType.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // creating new transaction type in background thread
                new CreateNewType().execute();
            }
        });
    }

    /**
     * Background Async Task to Create new transaction type
     */
    class CreateNewType extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(NewTypeActivity.this);
            pDialog.setMessage("Creating Transaction Type..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating transaction type
         */
        protected String doInBackground(String... args) {
            String typeCode = inputTypeCode.getText().toString();
            String typeDesc = inputTypeDesc.getText().toString();

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("type_code", typeCode));
            params.add(new BasicNameValuePair("type_desc", typeDesc));

            // getting JSON Object
            // Note that create transaction type url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_create_type,
                    "POST", params);

            // check log cat for response
            Log.d("Create Response", json.toString());

            // successfully created transaction type
            Intent i = new Intent(getApplicationContext(), AllTypesActivity.class);
            startActivity(i);

            // closing this screen
            finish();

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
        }

    }
}