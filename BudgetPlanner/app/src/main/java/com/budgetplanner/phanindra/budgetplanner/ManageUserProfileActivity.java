package com.budgetplanner.phanindra.budgetplanner;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ManageUserProfileActivity extends AppCompatActivity {

    EditText txtUsername;
    EditText txtEmailId;
    EditText txtContactNo;
    Button btnSave;
    Button btnDelete;

    String username;
    String email_id;
    String contact_no;

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    // url to update user profile
    private static final String url_update_user = "http://10.0.2.2/BudgetPlanner/users/edit";

    // url to unsubscribe user
    private static final String url_delete_user = "http://10.0.2.2/BudgetPlanner/users/delete";

    // JSON Node names
    private static final String TAG_USER_OBJ = "loggedInUser";
    private static final String TAG_USER = "User";
    private static final String TAG_USER_NAME = "username";
    private static final String TAG_EMAIL_ID = "email_id";
    private static final String TAG_CONTACT_NO = "contact_no";

    // Session Manager Class
    SessionManager session;

    // Button Logout
    Button btnLogout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_user_profile);

        // Session class instance
        session = new SessionManager(getApplicationContext());

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // save button
        btnSave = (Button) findViewById(R.id.btnSave);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        // Button logout
        btnLogout = (Button) findViewById(R.id.btnLogout);

        Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn() + "\n User name: " + session.getUserDetails().get(session.KEY_NAME), Toast.LENGTH_LONG).show();

        /**
         * Call this function whenever you want to check user login
         * This will redirect user to LoginActivity is he is not
         * logged in
         * */
        session.checkLogin();

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();

        // username
        username = user.get(SessionManager.KEY_NAME);

        // email
        email_id = user.get(SessionManager.KEY_EMAIL);

        // contact no
        contact_no = user.get(SessionManager.KEY_CONTACT_NO);


        // Getting complete user details in background thread
        new GetUserDetails().execute();

        // save button click event
        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // starting background task to update user profile
                new SaveUserDetails().execute();
            }
        });

        // Delete button click event
        btnDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // deleting user account in background thread
                new DeleteUser().execute();
            }
        });

        /**
         * Logout button click event
         * */
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Clear the session data
                // This will clear all session data and
                // redirect user to BudgetPlannerActivity
                session.logoutUser();
            }
        });

    }

    /**
     * Background Async Task to Get complete user details
     */
    class GetUserDetails extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ManageUserProfileActivity.this);
            pDialog.setMessage("Loading user details. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Getting user details in background thread
         */
        protected String doInBackground(String... params) {

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // Edit Text
                    txtUsername = (EditText) findViewById(R.id.inputUsername);
                    txtEmailId = (EditText) findViewById(R.id.inputEmailId);
                    txtContactNo = (EditText) findViewById(R.id.inputContactNo);

                    // display logged in user data in EditText
                    txtUsername.setText(username);
                    txtEmailId.setText(email_id);
                    txtContactNo.setText(contact_no);
                }
            });

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once we get all details
            pDialog.dismiss();
        }
    }

    /**
     * Background Async Task to Save user Details
     */
    class SaveUserDetails extends AsyncTask<String, String, String> {

        // getting updated data from EditTexts
        String username = txtUsername.getText().toString();
        String emailId = txtEmailId.getText().toString();
        String contactNo = txtContactNo.getText().toString();

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ManageUserProfileActivity.this);
            pDialog.setMessage("Saving user profile ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Saving user profile
         */
        protected String doInBackground(String... args) {

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_USER_NAME, username));
            params.add(new BasicNameValuePair(TAG_EMAIL_ID, emailId));
            params.add(new BasicNameValuePair(TAG_CONTACT_NO, contactNo));

            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/json");
            String jsonBody = "{\"username\": \"" + username + "\"," +
                    "    \"email_id\" : \"" + emailId + "\"," +
                    "\"contact_no\" : \"" + contactNo + "\"" +
                    "}";

            RequestBody body = RequestBody.create(mediaType, jsonBody);
            Request request = new Request.Builder()
                    .url(url_update_user + "/" + email_id + ".json")
                    .put(body)
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "90a2f8da-2715-16c0-91f3-026771c06132")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                // successfully updated

                // Update session parameters
                JSONObject jsonResponse = new JSONObject(response.body().string());
                session.createLoginSession(jsonResponse.getJSONObject("updatedUser").getJSONObject("User"));

                Intent i = getIntent();
                // send result code 100 to notify about user profile update
                setResult(100, i);
                finish();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once user profile is updated
            pDialog.dismiss();
        }
    }

    /*****************************************************************
     * Background Async Task to Delete user profile
     * */
    class DeleteUser extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ManageUserProfileActivity.this);
            pDialog.setMessage("Unsubscribing user...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Deleting user account
         */
        protected String doInBackground(String... args) {

            // Check for success tag
            int success;

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url_delete_user + "/" + email_id + ".json")
                    .delete(null)
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "827c36c2-7587-7112-6277-0dd86e60d18b")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                JSONObject jsonResponse = new JSONObject(response.body().string());

                if (jsonResponse.getJSONArray("response").getJSONObject(0).getString("result").equals("true")) {
                    session.logoutUser();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once user profile is deleted
            pDialog.dismiss();

        }

    }
}