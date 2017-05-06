package com.budgetplanner.phanindra.budgetplanner;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class BudgetPlannerActivity extends AppCompatActivity {
    private static final String TAG = "BudgetPlannerActivity";
    private static final int REQUEST_SIGNUP = 0;

    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_login) Button _loginButton;
    @InjectView(R.id.link_signup) TextView _signupLink;

    // Session Manager Class
    SessionManager session;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();
    private String message = "";

    // url to login to the BudgetPlanner application
    private static String url_login = "http://Sample-env.bxsv2nypnp.us-west-2.elasticbeanstalk.com/users/rest_login.json";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budget_planner);
        ButterKnife.inject(this);

        // Session Manager
        session = new SessionManager(getApplicationContext());
        session.editor.clear();
        session.editor.commit();

        Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();

        _loginButton.setOnClickListener(new View.OnClickListener() {

            /*@Override
            public void onClick(View v) {
                login();
            }
            */

            @Override
            public void onClick(View view) {
                // Performing authentication in background thread
                if (!validate()) {
                    return;
                }
                new AppLogin().execute();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }


    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("Password should be between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    /**
     * Background Async Task to Login to the BudgetPlanner application by making HTTP Request
     * */
    class AppLogin extends AsyncTask<String, String, String> {

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        final ProgressDialog progressDialog = new ProgressDialog(BudgetPlannerActivity.this,
                R.style.AppTheme_Dark);

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Authenticating. Please wait...");
            progressDialog.show();

        }

        /**
         * Authenticate credentials via url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("email_id", email));
            params.add(new BasicNameValuePair("password", password));

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_login, "POST", params);

            // check log cat for response
            Log.d("Login Response", json.toString());

            try {
                if (json.getJSONArray("response").getJSONObject(0).getString("result").equals("false")) {
                    message = json.getJSONArray("response").getJSONObject(0).getString("message");
                }
                else {
                    // successfully logged in
                    message = json.getJSONArray("response").getJSONObject(0).getString("message");

                    JSONObject loggedInUser = json.getJSONObject("loggedInUser").getJSONObject("User");

                    session.createLoginSession(loggedInUser);

                    Intent i = new Intent(getApplicationContext(), MainFunctionsActivity.class);
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