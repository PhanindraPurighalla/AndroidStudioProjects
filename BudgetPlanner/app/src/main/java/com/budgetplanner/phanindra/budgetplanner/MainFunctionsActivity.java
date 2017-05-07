package com.budgetplanner.phanindra.budgetplanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainFunctionsActivity extends Activity {

    Button btnNewExpenseRecord;
    Button btnManageUserProfile;
    Button btnViewExpenseReport;
    Button btnPerformSMSOperations;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_functions);

        // Buttons
        btnNewExpenseRecord = (Button) findViewById(R.id.btnCreateExpenseRecord);
        btnManageUserProfile = (Button) findViewById(R.id.btnManageUserProfile);
        btnViewExpenseReport = (Button) findViewById(R.id.btnViewExpenseReport);
        btnPerformSMSOperations = (Button) findViewById(R.id.btnPerformSMSOperations);

        // add transaction type click event
        btnNewExpenseRecord.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Launching create new expense record activity
                Intent i = new Intent(getApplicationContext(), AddExpenseActivity.class);
                startActivity(i);
            }
        });

        // manage user profile click event
        btnManageUserProfile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Launching manage user profile activity
                Intent i = new Intent(getApplicationContext(), ManageUserProfileActivity.class);
                startActivity(i);
            }
        });

        // view expense report click event
        btnViewExpenseReport.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Launching expense report activity
                Intent i = new Intent(getApplicationContext(), ExpenseReportActivity.class);
                startActivity(i);
            }
        });

        // perform SMS operations click event
        btnPerformSMSOperations.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Launching perform SMS operations activity
                Intent i = new Intent(getApplicationContext(), SMSOperationsActivity.class);
                startActivity(i);
            }
        });
    }
}
