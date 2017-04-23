package com.budgetplanner.phanindra.budgetplanner;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import org.json.JSONException;
import org.json.JSONObject;

public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "BudgetPlannerPref";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "username";

    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "email_id";

    // Contact no (make variable public to access from outside)
    public static final String KEY_CONTACT_NO = "contact_no";

    // Contact no (make variable public to access from outside)
    public static final String KEY_PROFESSION = "profession";

    // Contact no (make variable public to access from outside)
    public static final String KEY_GENDER = "gender";

    // Contact no (make variable public to access from outside)
    public static final String KEY_DATE_OF_BIRTH = "date_of_birth";

    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void createLoginSession(JSONObject userObject){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        try {
            editor.putString(KEY_NAME, userObject.getString("username"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Storing email in pref
        try {
            editor.putString(KEY_EMAIL, userObject.getString("email_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Storing contact no in pref
        try {
            editor.putString(KEY_CONTACT_NO, userObject.getString("contact_no"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Storing profession in pref
        try {
            editor.putString(KEY_PROFESSION, userObject.getString("profession"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Storing gender in pref
        try {
            editor.putString(KEY_GENDER, userObject.getString("gender"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Storing date of birth in pref
        try {
            editor.putString(KEY_DATE_OF_BIRTH, userObject.getString("date_of_birth"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // commit changes
        editor.commit();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, BudgetPlannerActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }


    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));

        // user email id
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

        // user contact no
        user.put(KEY_CONTACT_NO, pref.getString(KEY_CONTACT_NO, null));

        // user profession
        user.put(KEY_PROFESSION, pref.getString(KEY_PROFESSION, null));

        // user gender
        user.put(KEY_GENDER, pref.getString(KEY_GENDER, null));

        // user date of birth
        user.put(KEY_DATE_OF_BIRTH, pref.getString(KEY_DATE_OF_BIRTH, null));

        // return user
        return user;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Login Activity
        Intent i = new Intent(_context, BudgetPlannerActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Starting Login Activity
        _context.startActivity(i);
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }
}