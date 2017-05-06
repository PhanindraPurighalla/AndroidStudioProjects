package com.budgetplanner.phanindra.budgetplanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExpenseReportActivity extends AppCompatActivity {

    // Session Manager Class
    SessionManager session;

    private String username;

    // Progress Dialog
    private ProgressDialog pDialog;

    // url to get user's expenses list
    private static String url_user_expenses = "http://Sample-env.bxsv2nypnp.us-west-2.elasticbeanstalk.com/expenses/get_user_expenses/";

    BarChart chart ;
    ArrayList<BarEntry> BARENTRY ;
    ArrayList<String> BarEntryLabels ;
    BarDataSet Bardataset ;
    BarData BARDATA ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expense_report);
        chart = (BarChart) findViewById(R.id.expenseByUserChart);

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

        // username
        username = user.get(SessionManager.KEY_NAME);

        BARENTRY = new ArrayList<>();

        BarEntryLabels = new ArrayList<String>();

        new LoadAllExpenses().execute();

    }

    /**
     * Background Async Task to Load all expense data by making HTTP Request
     * */
    class LoadAllExpenses extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ExpenseReportActivity.this);
            pDialog.setMessage("Loading expense report. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All expenses from url
         * */
        protected String doInBackground(String... args) {

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url_user_expenses + username + ".json")
                    .get()
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "8f37de53-1db3-e010-35ad-8a89d685ad6f")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                JSONObject jsonResponse = new JSONObject(response.body().string());
                JSONArray expenses = jsonResponse.getJSONArray("expense");
                for (int i=0; i<expenses.length(); i++) {
                    JSONObject expense = expenses.getJSONObject(i);
                    String categoryCode = expense.getJSONObject("Category").getString("category_code");
                    Float expenseAmount = Float.parseFloat(expense.getJSONObject("Expense").getString("expense_amount"));
                    if (BarEntryLabels.contains(categoryCode)) {
                        int categoryCodeIndex = BarEntryLabels.indexOf(categoryCode);
                        float currentExpenseAmount = BARENTRY.get(categoryCodeIndex).getVal();
                        BARENTRY.remove(categoryCodeIndex);
                        BARENTRY.add(new BarEntry(currentExpenseAmount+expenseAmount, categoryCodeIndex));
                    }
                    else {
                        BarEntryLabels.add(categoryCode);
                        BARENTRY.add(new BarEntry(expenseAmount, BarEntryLabels.indexOf(categoryCode)));
                    }
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
            // dismiss the dialog after getting all expenses
            pDialog.dismiss();

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    Bardataset = new BarDataSet(BARENTRY, "Categories");
                    BARDATA = new BarData(BarEntryLabels, Bardataset);
                    Bardataset.setColors(ColorTemplate.COLORFUL_COLORS);
                    chart.setData(BARDATA);
                    chart.animateY(3000);
                }
            });


        }

    }
}
