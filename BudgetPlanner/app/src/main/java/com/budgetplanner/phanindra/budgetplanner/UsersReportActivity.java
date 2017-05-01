package com.budgetplanner.phanindra.budgetplanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UsersReportActivity extends AppCompatActivity {

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    // url to get all users list
    private static String url_all_users = "http://budgetplanner.bxsv2nypnp.us-west-2.elasticbeanstalk.com/users.json";

    BarChart chart ;
    ArrayList<BarEntry> BARENTRY ;
    ArrayList<String> BarEntryLabels ;
    BarDataSet Bardataset ;
    BarData BARDATA ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_report);
        chart = (BarChart) findViewById(R.id.chart1);

        BARENTRY = new ArrayList<>();

        BarEntryLabels = new ArrayList<String>();

        //AddValuesToBARENTRY();

        //AddValuesToBarEntryLabels();

        new LoadAllUsers().execute();

        //fetchUserReportData();
    }

    public void AddValuesToBARENTRY(){

        BARENTRY.add(new BarEntry(2f, 0));
        BARENTRY.add(new BarEntry(4f, 1));
        BARENTRY.add(new BarEntry(6f, 2));
        BARENTRY.add(new BarEntry(15f, 3));
        BARENTRY.add(new BarEntry(7f, 4));
        BARENTRY.add(new BarEntry(3f, 5));

    }

    public void AddValuesToBarEntryLabels(){

        BarEntryLabels.add("January");
        BarEntryLabels.add("February");
        BarEntryLabels.add("March");
        BarEntryLabels.add("April");
        BarEntryLabels.add("May");
        BarEntryLabels.add("June");

    }

    /**
     * getting All users from url
     * */
    protected String fetchUserReportData() {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url_all_users)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache")
                .addHeader("postman-token", "8f37de53-1db3-e010-35ad-8a89d685ad6f")
                .build();

        try {
            Response response = client.newCall(request).execute();
            JSONObject jsonResponse = new JSONObject(response.body().string());
            JSONArray users = jsonResponse.getJSONArray("users");
            for (int i=0; i<users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                String profession = user.getJSONObject("User").getString("profession");
                if (BarEntryLabels.contains(profession)) {
                    int professionIndex = BarEntryLabels.indexOf(profession);
                    float currentCount = BARENTRY.get(professionIndex).getVal();
                    BARENTRY.remove(professionIndex);
                    BARENTRY.add(new BarEntry(currentCount+1, professionIndex));
                }
                else {
                    BarEntryLabels.add(profession);
                    BARENTRY.add(new BarEntry(1f, BarEntryLabels.indexOf(profession)));
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
     * Background Async Task to Load all user data by making HTTP Request
     * */
    class LoadAllUsers extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(UsersReportActivity.this);
            pDialog.setMessage("Loading users report. Please wait...");
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
                    .url(url_all_users)
                    .get()
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "8f37de53-1db3-e010-35ad-8a89d685ad6f")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                JSONObject jsonResponse = new JSONObject(response.body().string());
                JSONArray users = jsonResponse.getJSONArray("users");
                for (int i=0; i<users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);
                    String profession = user.getJSONObject("User").getString("profession");
                    if (BarEntryLabels.contains(profession)) {
                        int professionIndex = BarEntryLabels.indexOf(profession);
                        float currentCount = BARENTRY.get(professionIndex).getVal();
                        BARENTRY.remove(professionIndex);
                        BARENTRY.add(new BarEntry(currentCount+1, professionIndex));
                    }
                    else {
                        BarEntryLabels.add(profession);
                        BARENTRY.add(new BarEntry(1f, BarEntryLabels.indexOf(profession)));
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
            // dismiss the dialog after getting all users
            pDialog.dismiss();

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    Bardataset = new BarDataSet(BARENTRY, "Professions");
                    BARDATA = new BarData(BarEntryLabels, Bardataset);
                    Bardataset.setColors(ColorTemplate.COLORFUL_COLORS);
                    chart.setData(BARDATA);
                    chart.animateY(3000);
                }
            });


        }

    }
}
