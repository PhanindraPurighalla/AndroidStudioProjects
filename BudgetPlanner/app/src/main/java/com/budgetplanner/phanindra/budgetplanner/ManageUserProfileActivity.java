package com.budgetplanner.phanindra.budgetplanner;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ManageUserProfileActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener {

    EditText txtUsername;
    EditText txtEmailId;
    EditText txtContactNo;

    private String profession = "";
    private String gender = "";
    private String dateOfBirth = "";
    private String datePart = "01-Jan-1970";
    private String timePart = "00:00:00";

    private View btnPickDate;
    private View btnPickTime;
    private TextView textView;

    Button btnSave;
    Button btnDelete;

    String username;
    String email_id;
    String contact_no;

    @InjectView(R.id.edit_professionspinner) MaterialBetterSpinner _professionSpinner;
    @InjectView(R.id.edit_gender_radio_group) RadioGroup _genderRadioGroup;
    @InjectView(R.id.edit_male_radio_btn) RadioButton _maleRadioButton;
    @InjectView(R.id.edit_female_radio_btn) RadioButton _femaleRadioButton;
    @InjectView(R.id.edit_unspecified_radio_btn) RadioButton _unspecifiedRadioButton;
    @InjectView(R.id.edit_dob_datetime_text) EditText _dateOfBirth;

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
        ButterKnife.inject(this);

        ArrayList<String> professions =getProfessions();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, professions);
        final MaterialBetterSpinner materialDesignSpinner = (MaterialBetterSpinner)
                findViewById(R.id.edit_professionspinner);
        materialDesignSpinner.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // Showing selected spinner item
                Toast.makeText(getBaseContext(), "Profession selected: " + materialDesignSpinner.getText(), Toast.LENGTH_LONG).show();

            }
        } );
        materialDesignSpinner.setAdapter(arrayAdapter);

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

        // profession
        profession = user.get(SessionManager.KEY_PROFESSION);

        // gender
        gender = user.get(SessionManager.KEY_GENDER);

        // date of birth
        dateOfBirth = user.get(SessionManager.KEY_DATE_OF_BIRTH);

        _genderRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                RadioButton rb = (RadioButton) findViewById(checkedId);
                gender = rb.getText().toString();
                Toast.makeText(getBaseContext(), "Gender selected: " + gender, Toast.LENGTH_LONG).show();
            }
        });

        textView = (TextView) findViewById(R.id.edit_dob_datetime_text);
        btnPickDate = findViewById(R.id.edit_btn_dob_date);
        btnPickTime = findViewById(R.id.edit_btn_dob_time);

        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog datepickerdialog = DatePickerDialog.newInstance(
                        ManageUserProfileActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                datepickerdialog.setThemeDark(true); //set dark them for dialog?
                datepickerdialog.vibrate(true); //vibrate on choosing date?
                datepickerdialog.dismissOnPause(true); //dismiss dialog when onPause() called?
                datepickerdialog.showYearPickerFirst(false); //choose year first?
                datepickerdialog.setAccentColor(Color.parseColor("#9C27A0")); // custom accent color
                datepickerdialog.setTitle("Please select a date"); //dialog title
                datepickerdialog.show(getFragmentManager(), "Datepickerdialog"); //show dialog
            }
        });

        btnPickTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();
                TimePickerDialog timepickerdialog = TimePickerDialog.newInstance(ManageUserProfileActivity.this,
                        now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
                timepickerdialog.setThemeDark(true); //Dark Theme?
                timepickerdialog.vibrate(true); //vibrate on choosing time?
                timepickerdialog.dismissOnPause(false); //dismiss the dialog onPause() called?
                timepickerdialog.setAccentColor(Color.parseColor("#9C27A0")); // custom accent color
                timepickerdialog.enableSeconds(true); //show seconds?

                //Handling cancel event
                timepickerdialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Toast.makeText(ManageUserProfileActivity.this, "Cancel choosing time", Toast.LENGTH_SHORT).show();
                    }
                });
                timepickerdialog.show(getFragmentManager(), "Timepickerdialog"); //show time picker dialog
            }
        });

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

    private ArrayList<String> getProfessions(){
        JSONObject jsonObject=null;
        ArrayList<String> professionList=new ArrayList<String>();
        try {
            String professions = "{\n" +
                    "  \"professions\": [\n" +
                    "    {\n" +
                    "      \"Profession\": {\n" +
                    "        \"id\": \"1\",\n" +
                    "        \"desc\": \"student\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"Profession\": {\n" +
                    "        \"id\": \"2\",\n" +
                    "        \"desc\": \"home maker\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"Profession\": {\n" +
                    "        \"id\": \"3\",\n" +
                    "        \"desc\": \"software professional\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"Profession\": {\n" +
                    "        \"id\": \"4\",\n" +
                    "        \"desc\": \"doctor\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"Profession\": {\n" +
                    "        \"id\": \"5\",\n" +
                    "        \"desc\": \"teacher\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
            jsonObject=new JSONObject(professions);
            JSONArray jsonArray = jsonObject.getJSONArray("professions");
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    professionList.add(jsonArray.getJSONObject(i).getJSONObject("Profession").getString("desc"));
                }
            }
        } catch (JSONException je){
            je.printStackTrace();
        }
        return professionList;
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        datePart = dayOfMonth + "-" + (++monthOfYear) + "-" + year;
        textView.setText(datePart + " " + timePart);
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        String hourString = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
        String minuteString = minute < 10 ? "0" + minute : "" + minute;
        String secondString = second < 10 ? "0" + second : "" + second;
        timePart = hourString + ":" + minuteString + ":" + secondString;
        textView.setText(datePart + " " + timePart);
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

                    // display logged in user data in appropriate fields
                    txtUsername.setText(username);
                    txtEmailId.setText(email_id);
                    txtContactNo.setText(contact_no);
                    _professionSpinner.setText(profession);// ((ArrayAdapter<String>)_professionSpinner.getAdapter()).getPosition(profession));

                    switch (gender) {
                        case "Male":
                            _maleRadioButton.setChecked(true);
                            break;
                        case "Female":
                            _femaleRadioButton.setChecked(true);
                            break;
                        case "Unspecified":
                            _unspecifiedRadioButton.setChecked(true);
                            break;
                    }

                    _dateOfBirth.setText(dateOfBirth);

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
        String profession = _professionSpinner.getText().toString();
        String dob = _dateOfBirth.getText().toString();

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

            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/json");
            String jsonBody = "{\"username\": \"" + username + "\"," +
                    "    \"email_id\" : \"" + emailId + "\"," +
                    "\"contact_no\" : \"" + contactNo + "\"," +
                    "\"profession\" : \"" + profession + "\"," +
                    "\"gender\" : \"" + gender + "\"," +
                    "\"date_of_birth\" : \"" + dob + "\"" +
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