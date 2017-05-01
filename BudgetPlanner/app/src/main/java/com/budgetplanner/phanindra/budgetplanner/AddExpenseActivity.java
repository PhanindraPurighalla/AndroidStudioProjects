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

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
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
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AddExpenseActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener {

    // Progress Dialog
    private ProgressDialog pDialog;

    ArrayList<String> categoriesList=new ArrayList<String>();

    private String gender = "Male";
    private String datePart = "01-Jan-1970";
    private String timePart = "00:00:00";

    private View btnPickDate;
    private View btnPickTime;
    private TextView textView;

    @InjectView(R.id.input_expense_desc) EditText _expenseDescText;
    @InjectView(R.id.input_expense_amount) EditText _expenseAmountText;
    @InjectView(R.id.categoryspinner) EditText _categorySpinner;
    @InjectView(R.id.expense_datetime_text) EditText _dateOfExpense;
    @InjectView(R.id.btn_add_expense) Button _addExpenseButton;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();
    private String message = "";

    // url to add expense record to the BudgetPlanner application
    private static String url_add_expense = "http://budgetplanner.bxsv2nypnp.us-west-2.elasticbeanstalk.com/expenses/add.json";

    // url to get configured categories
    private static String url_get_categories = "http://budgetplanner.bxsv2nypnp.us-west-2.elasticbeanstalk.com/categories.json";

    MaterialBetterSpinner materialDesignSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_expense);
        ButterKnife.inject(this);

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
                //new AddExpenseRecord().execute();
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
                double expenseAmount = Double.valueOf(_expenseAmountText.getText().toString());
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
                    String category_code = category.getJSONObject("Category").getString("category_code");
                    categoriesList.add(category_code);
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

    class AddExpenseRecord extends AsyncTask<String, String, String> {

        String expense_desc = _expenseDescText.getText().toString();
        String expense_amount = _expenseAmountText.getText().toString();
        String category = _categorySpinner.getText().toString();
        String expense_date = _dateOfExpense.getText().toString();

        final ProgressDialog progressDialog = new ProgressDialog(AddExpenseActivity.this,
                R.style.AppTheme_Dark);

        /**
         * Before starting background thread Show Progress Dialog
         *
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Creating a new expense record. Please wait...");
            progressDialog.show();

        }

        /**
         * Signup new user via url
         *
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username", name));
            params.add(new BasicNameValuePair("email_id", email));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("profession", profession));
            params.add(new BasicNameValuePair("gender", gender));
            params.add(new BasicNameValuePair("date_of_birth", dob));

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_add_expense, "POST", params);

            // check log cat for response
            Log.d("Signup Response", json.toString());

            try {
                if (json.getJSONArray("response").getJSONObject(0).getString("result").equals("false")) {
                    message = json.getJSONArray("response").getJSONObject(0).getString("message");
                }
                else {
                    // successfully signed up
                    message = json.getJSONArray("response").getJSONObject(0).getString("message");
                    Intent i = new Intent(getApplicationContext(), BudgetPlannerActivity.class);
                    startActivity(i);

                    // closing this screen
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * *
        protected void onPostExecute(String file_url) {

            // dismiss the dialog after successful login
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

    } */
}