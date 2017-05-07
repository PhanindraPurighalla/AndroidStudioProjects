package com.budgetplanner.phanindra.budgetplanner;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SMSOperationsActivity extends Activity implements OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 1234;
    //  GUI Widget
    Button btnSent, btnInbox, btnDraft;
    TextView lblMsg, lblNo;
    ListView lvMsg;

    // Cursor Adapter
    SimpleCursorAdapter adapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_operations);

        // Init GUI Widget
        btnInbox = (Button) findViewById(R.id.btnInbox);
        btnInbox.setOnClickListener(this);

        btnSent = (Button)findViewById(R.id.btnSentBox);
        btnSent.setOnClickListener(this);

        btnDraft = (Button)findViewById(R.id.btnDraft);
        btnDraft.setOnClickListener(this);

        lvMsg = (ListView) findViewById(R.id.lvMsg);

    }

    @Override
    public void onClick(View v) {

        if (v == btnInbox) {

            if (ContextCompat.checkSelfPermission(SMSOperationsActivity.this,
                    Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(SMSOperationsActivity.this,
                        Manifest.permission.READ_SMS)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(SMSOperationsActivity.this,
                            new String[]{Manifest.permission.READ_SMS},
                            MY_PERMISSIONS_REQUEST_READ_SMS);

                    // MY_PERMISSIONS_REQUEST_READ_SMS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
            else {
                // permission was granted, yay! Do the
                // SMS-related task you need to do.

                // Create Inbox box URI
                Uri inboxURI = Uri.parse("content://sms/inbox");

                // List required columns
                String[] reqCols = new String[] { "_id", "address", "body" };

                // Get Content Resolver object, which will deal with Content Provider
                ContentResolver cr = getContentResolver();

                // Fetch Inbox SMS Message from Built-in Content Provider
                Cursor c = cr.query(inboxURI, reqCols, null, null, null);

                // Attached Cursor with adapter and display in listview
                adapter = new SimpleCursorAdapter(this, R.layout.row, c,
                        new String[] { "body", "address" }, new int[] {
                        R.id.lblMsg, R.id.lblNumber });
                lvMsg.setAdapter(adapter);
            }
        }

        if(v==btnSent)
        {

            // Create Sent box URI
            Uri sentURI = Uri.parse("content://sms/sent");

            // List required columns
            String[] reqCols = new String[] { "_id", "address", "body" };

            // Get Content Resolver object, which will deal with Content Provider
            ContentResolver cr = getContentResolver();

            // Fetch Sent SMS Message from Built-in Content Provider
            Cursor c = cr.query(sentURI, reqCols, null, null, null);

            // Attached Cursor with adapter and display in listview
            adapter = new SimpleCursorAdapter(this, R.layout.row, c,
                    new String[] { "body", "address" }, new int[] {
                    R.id.lblMsg, R.id.lblNumber });
            lvMsg.setAdapter(adapter);

        }

        if(v==btnDraft)
        {
            // Create Draft box URI
            Uri draftURI = Uri.parse("content://sms/draft");

            // List required columns
            String[] reqCols = new String[] { "_id", "address", "body" };

            // Get Content Resolver object, which will deal with Content Provider
            ContentResolver cr = getContentResolver();

            // Fetch Sent SMS Message from Built-in Content Provider
            Cursor c = cr.query(draftURI, reqCols, null, null, null);

            // Attached Cursor with adapter and display in listview
            adapter = new SimpleCursorAdapter(this, R.layout.row, c,
                    new String[] { "body", "address" }, new int[] {
                    R.id.lblMsg, R.id.lblNumber });
            lvMsg.setAdapter(adapter);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // SMS-related task you need to do.

                    // Create Inbox box URI
                    Uri inboxURI = Uri.parse("content://sms/inbox");

                    // List required columns
                    String[] reqCols = new String[] { "_id", "address", "body" };

                    // Get Content Resolver object, which will deal with Content Provider
                    ContentResolver cr = getContentResolver();

                    // Fetch Inbox SMS Message from Built-in Content Provider
                    Cursor c = cr.query(inboxURI, reqCols, null, null, null);

                    // Attached Cursor with adapter and display in listview
                    adapter = new SimpleCursorAdapter(this, R.layout.row, c,
                            new String[] { "body", "address" }, new int[] {
                            R.id.lblMsg, R.id.lblNumber });
                    lvMsg.setAdapter(adapter);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
