package com.budgetplanner.phanindra.budgetplanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainFunctionsActivity extends Activity {


    Button btnViewTypes;
    Button btnNewType;
    Button btnManageUserProfile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_functions);

        // Buttons
        btnViewTypes = (Button) findViewById(R.id.btnViewTypes);
        btnNewType = (Button) findViewById(R.id.btnCreateType);
        btnManageUserProfile = (Button) findViewById(R.id.btnManageUserProfile);

        // view transaction types click event
        btnViewTypes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Launching All transaction types Activity
                Intent i = new Intent(getApplicationContext(), AllTypesActivity.class);
                startActivity(i);
            }
        });

        // add transaction type click event
        btnNewType.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Launching create new transaction type activity
                Intent i = new Intent(getApplicationContext(), NewTypeActivity.class);
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
    }
}
