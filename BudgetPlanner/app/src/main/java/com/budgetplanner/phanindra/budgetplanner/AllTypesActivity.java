package com.budgetplanner.phanindra.budgetplanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class AllTypesActivity extends ListActivity {

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> typesList;

    // url to get all transaction types list
    private static String url_all_types = "http://10.0.2.2/BudgetPlanner/types.json";

    // JSON Node names
    private static final String TAG_TYPES = "types";
    private static final String TAG_TYPE = "Type";
    private static final String TAG_TYPE_ID = "id";
    private static final String TAG_TYPE_CODE = "type_code";

    // Transaction Types JSONArray
    JSONArray types = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_types);

        // Hashmap for ListView
        typesList = new ArrayList<HashMap<String, String>>();

        // Loading transaction types in Background Thread
        new LoadAllTypes().execute();

        // Get listview
        ListView lv = getListView();

        // on selecting single transaction type
        // launching Edit Transaction Type Screen
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String typeId = ((TextView) view.findViewById(R.id.typeId)).getText()
                        .toString();

                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        EditTypeActivity.class);
                // sending typeId to next activity
                in.putExtra(TAG_TYPE_ID, typeId);

                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
            }
        });

    }

    // Response from Edit Transaction Type Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted transaction type
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

    }

    /**
     * Background Async Task to Load all transaction types by making HTTP Request
     * */
    class LoadAllTypes extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AllTypesActivity.this);
            pDialog.setMessage("Loading transaction types. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All transaction types from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_types, "GET", params);

            // Check your log cat for JSON response
            Log.d("All Transaction types: ", json.toString());

            try {
                // Checking if we have obtained the transaction types
                types = json.getJSONArray(TAG_TYPES);

                if (types.length() > 0) {
                    // transaction types found
                    // looping through All Transaction types
                    for (int i = 0; i < types.length(); i++) {
                        JSONObject c = types.getJSONObject(i);

                        // Storing each json item in variable
                        String typeId = c.getJSONObject(TAG_TYPE).getString(TAG_TYPE_ID);
                        String typeCode = c.getJSONObject(TAG_TYPE).getString(TAG_TYPE_CODE);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_TYPE_ID, typeId);
                        map.put(TAG_TYPE_CODE, typeCode);

                        // adding HashList to ArrayList
                        typesList.add(map);
                    }
                } else {
                    // no transaction types found
                    // Launch Add New transaction type Activity
                    Intent i = new Intent(getApplicationContext(),
                            NewTypeActivity.class);
                    // Closing all previous activities
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            AllTypesActivity.this, typesList,
                            R.layout.list_type, new String[] { TAG_TYPE_ID,
                            TAG_TYPE_CODE},
                            new int[] { R.id.typeId, R.id.typeCode });
                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }

    }
}