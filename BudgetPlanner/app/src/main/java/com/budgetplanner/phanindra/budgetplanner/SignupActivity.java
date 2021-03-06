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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SignupActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener {
    private String gender = "Male";
    private String datePart = "01-Jan-1970";
    private String timePart = "00:00:00";

    private View btnPickDate;
    private View btnPickTime;
    private TextView textView;

    @InjectView(R.id.input_name) EditText _nameText;
    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.professionspinner) EditText _professionSpinner;
    @InjectView(R.id.gender_radio_group) RadioGroup _genderRadioGroup;
    @InjectView(R.id.dob_datetime_text) EditText _dateOfBirth;
    @InjectView(R.id.btn_signup) Button _signupButton;
    @InjectView(R.id.link_login) TextView _loginLink;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();
    private String message = "";

    // url to signup to the BudgetPlanner application
    private static String url_signup = "http://Sample-env.bxsv2nypnp.us-west-2.elasticbeanstalk.com/users/add.json";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.inject(this);

        ArrayList<String> professions =getProfessions();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, professions);
        final MaterialBetterSpinner materialDesignSpinner = (MaterialBetterSpinner)
                findViewById(R.id.professionspinner);
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

        _genderRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                RadioButton rb = (RadioButton) findViewById(checkedId);
                gender = rb.getText().toString();
                Toast.makeText(getBaseContext(), "Gender selected: " + gender, Toast.LENGTH_LONG).show();
            }
        });

        textView = (TextView) findViewById(R.id.dob_datetime_text);
        btnPickDate = findViewById(R.id.btn_dob_date);
        btnPickTime = findViewById(R.id.btn_dob_time);

        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog datepickerdialog = DatePickerDialog.newInstance(
                        SignupActivity.this,
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
                TimePickerDialog timepickerdialog = TimePickerDialog.newInstance(SignupActivity.this,
                        now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
                timepickerdialog.setThemeDark(true); //Dark Theme?
                timepickerdialog.vibrate(true); //vibrate on choosing time?
                timepickerdialog.dismissOnPause(false); //dismiss the dialog onPause() called?
                timepickerdialog.enableSeconds(true); //show seconds?

                //Handling cancel event
                timepickerdialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Toast.makeText(SignupActivity.this, "Cancel choosing time", Toast.LENGTH_SHORT).show();
                    }
                });
                timepickerdialog.show(getFragmentManager(), "Timepickerdialog"); //show time picker dialog
            }
        });

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform user creation in background thread
                if (!validate()) {
                    return;
                }
                new AppSignup().execute();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
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
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
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
     * Background Async Task to Signup a new user to the BudgetPlanner application by making HTTP Request
     * */
    class AppSignup extends AsyncTask<String, String, String> {

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String profession = _professionSpinner.getText().toString();
        String dob = _dateOfBirth.getText().toString();

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark);

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Creating a new user account. Please wait...");
            progressDialog.show();

        }

        /**
         * Signup new user via url
         * */
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
            JSONObject json = jParser.makeHttpRequest(url_signup, "POST", params);

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
         * **/
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

    }
}