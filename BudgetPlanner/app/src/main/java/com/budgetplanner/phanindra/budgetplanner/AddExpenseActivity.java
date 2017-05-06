package com.budgetplanner.phanindra.budgetplanner;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

public class AddExpenseActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener {

    // Session Manager Class
    SessionManager session;

    // Progress Dialog
    private ProgressDialog pDialog;

    ArrayList<String> categoriesList=new ArrayList<String>();
    HashMap<String, String> categoriesHash = new HashMap<String, String>();

    private int userId;
    private double expenseAmount;
    private String datePart = "1970-01-01";
    private String timePart = "00:00:00";

    private View btnPickDate;
    private View btnPickTime;
    private TextView textView;

    @InjectView(R.id.input_expense_desc) EditText _expenseDescText;
    @InjectView(R.id.input_expense_amount) EditText _expenseAmountText;
    @InjectView(R.id.categoryspinner) EditText _categorySpinner;
    @InjectView(R.id.expense_datetime_text) EditText _dateOfExpense;
    @InjectView(R.id.btn_add_expense) Button _addExpenseButton;

    private String message = "";

    // url to add expense record to the BudgetPlanner application
    private static String url_add_expense = "http://Sample-env.bxsv2nypnp.us-west-2.elasticbeanstalk.com/expenses/add.json";

    // url to get configured categories
    private static String url_get_categories = "http://Sample-env.bxsv2nypnp.us-west-2.elasticbeanstalk.com/categories.json";

    MaterialBetterSpinner materialDesignSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_expense);
        ButterKnife.inject(this);

        // Session class instance
        session = new SessionManager(getApplicationContext());
        /**
         * Call this function whenever you want to check user login
         * This will redirect user to LoginActivity if he is not
         * logged in
         * */
        session.checkLogin();

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();

        // userid
        userId = Integer.valueOf(user.get(SessionManager.KEY_USERID));

        materialDesignSpinner = (MaterialBetterSpinner)
                findViewById(R.id.categoryspinner);
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
                Toast.makeText(getBaseContext(), "Category selected: " + materialDesignSpinner.getText(), Toast.LENGTH_LONG).show();

            }
        } );


        new LoadAllCategories().execute();

        textView = (TextView) findViewById(R.id.expense_datetime_text);
        btnPickDate = findViewById(R.id.btn_expense_date);
        btnPickTime = findViewById(R.id.btn_expense_time);

        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog datepickerdialog = DatePickerDialog.newInstance(
                        AddExpenseActivity.this,
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
                TimePickerDialog timepickerdialog = TimePickerDialog.newInstance(AddExpenseActivity.this,
                        now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
                timepickerdialog.setThemeDark(true); //Dark Theme?
                timepickerdialog.vibrate(true); //vibrate on choosing time?
                timepickerdialog.dismissOnPause(false); //dismiss the dialog onPause() called?
                timepickerdialog.enableSeconds(true); //show seconds?
                timepickerdialog.setAccentColor(Color.parseColor("#9C27A0")); // custom accent color

                //Handling cancel event
                timepickerdialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Toast.makeText(AddExpenseActivity.this, "Cancel choosing time", Toast.LENGTH_SHORT).show();
                    }
                });
                timepickerdialog.show(getFragmentManager(), "Timepickerdialog"); //show time picker dialog
            }
        });

        _addExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform user creation in background thread
                if (!validate()) {
                    return;
                }
                new AddExpenseRecord().execute();
            }
        });

    }

    public boolean validate() {
        boolean valid = true;

        String expense_desc = _expenseDescText.getText().toString();
        String expense_amount = _expenseAmountText.getText().toString();

        if (expense_desc.isEmpty() || expense_desc.length() < 10) {
            _expenseDescText.setError("at least 10 characters");
            valid = false;
        } else {
            _expenseDescText.setError(null);
        }

        if (expense_amount.isEmpty()) {
            _expenseAmountText.setError("expense amount cannot be null");
            valid = false;
        } else {
            try {
                expenseAmount = Double.valueOf(_expenseAmountText.getText().toString());
                _expenseAmountText.setError(null);
            }
            catch (NumberFormatException nfe) {
                valid = false;
            }
        }
        return valid;
    }


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String dayPart = dayOfMonth < 10 ? "0" + dayOfMonth : "" + dayOfMonth;
        int updatedMonth = ++monthOfYear;
        String monthPart = updatedMonth < 10 ? "0" + updatedMonth : "" + updatedMonth;
        datePart = year + "-" + (monthPart) + "-" + dayPart;
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
     * Background Async Task to Load all categories by making HTTP Request
     * */
    class LoadAllCategories extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AddExpenseActivity.this);
            pDialog.setMessage("Filling categories drop-down list. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All users from url
         * */
        protected String doInBackground(String... args) {

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url_get_categories)
                    .get()
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "8f37de53-1db3-e010-35ad-8a89d685ad6f")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                JSONObject jsonResponse = new JSONObject(response.body().string());
                JSONArray categories = jsonResponse.getJSONArray("categories");
                for (int i=0; i<categories.length(); i++) {
                    JSONObject category = categories.getJSONObject(i);
                    String category_id = category.getJSONObject("Category").getString("id");
                    String category_code = category.getJSONObject("Category").getString("category_code");
                    categoriesList.add(category_code);
                    categoriesHash.put(category_code, category_id);
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
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all users
            pDialog.dismiss();

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(AddExpenseActivity.this,
                            android.R.layout.simple_dropdown_item_1line, categoriesList);

                    materialDesignSpinner.setAdapter(arrayAdapter);

                }
            });
        }

    }

    /**
     * Background Async Task to create a new expense record by making HTTP Request
     *
     **/

    class AddExpenseRecord extends AsyncTask<String, String, String> {

        String expense_desc = _expenseDescText.getText().toString();
        String category_id = categoriesHash.get(_categorySpinner.getText().toString());
        String expense_date = _dateOfExpense.getText().toString();

        final ProgressDialog progressDialog = new ProgressDialog(AddExpenseActivity.this,
                R.style.AppTheme_Dark);

        /**
         * Before starting background thread Show Progress Dialog
         **/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Creating a new expense record. Please wait...");
            progressDialog.show();

        }

        /**
         * Add expense record via url
         **/
        protected String doInBackground(String... args) {
            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\r\n\t\"user_id\": \"" + userId + "\"," +
                    "\r\n    \"expense_date\": \"" + expense_date + "\"," +
                    "\r\n    \"category_id\": \"" + category_id + "\"," +
                    "\r\n    \"expense_amount\": \"" + expenseAmount + "\"," +
                    "\r\n    \"expense_desc\": \"" + expense_desc + "\"" +
                    "\r\n}");
            Request request = new Request.Builder()
                    .url(url_add_expense)
                    .post(body)
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "3ffa193b-9b33-f622-59db-ba8b220c9a26")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                JSONObject jsonResponse = new JSONObject(response.body().string());
                message = jsonResponse.getJSONArray("response").getJSONObject(0).getString("message");
                if (jsonResponse.getJSONArray("response").getJSONObject(0).getString("result").equals("true")) {
                    finish();
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
         * */
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after processing expense record
            progressDialog.dismiss();

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {

                    if (message != null) {
                        Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                    }

                }
            });

        }
    }
}