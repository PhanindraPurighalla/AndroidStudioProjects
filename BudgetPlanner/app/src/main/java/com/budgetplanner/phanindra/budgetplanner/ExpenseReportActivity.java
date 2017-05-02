package com.budgetplanner.phanindra.budgetplanner;

import java.io.IOException;
import java.util.ArrayList;

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

    // Progress Dialog
    private ProgressDialog pDialog;

    // url to get all expenses list
    private static String url_all_expenses = "http://budgetplanner.bxsv2nypnp.us-west-2.elasticbeanstalk.com/expenses.json";

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
                    .url(url_all_expenses)
                    .get()
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "8f37de53-1db3-e010-35ad-8a89d685ad6f")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                JSONObject jsonResponse = new JSONObject(response.body().string());
                JSONArray expenses = jsonResponse.getJSONArray("expenses");
                for (int i=0; i<expenses.length(); i++) {
                    JSONObject expense = expenses.getJSONObject(i);
                    String categoryCode = expense.getJSONObject("Category").getString("category_code");
                    if (BarEntryLabels.contains(categoryCode)) {
                        int categoryCodeIndex = BarEntryLabels.indexOf(categoryCode);
                        float currentCount = BARENTRY.get(categoryCodeIndex).getVal();
                        BARENTRY.remove(categoryCodeIndex);
                        BARENTRY.add(new BarEntry(currentCount+1, categoryCodeIndex));
                    }
                    else {
                        BarEntryLabels.add(categoryCode);
                        BARENTRY.add(new BarEntry(1f, BarEntryLabels.indexOf(categoryCode)));
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
