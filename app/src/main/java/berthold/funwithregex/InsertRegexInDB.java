package berthold.funwithregex;

/*
 * InsertRegexInDB.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 10/26/18 9:04 PM
 */

/**
 * Insert regex in DB
 *
 *
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InsertRegexInDB extends AppCompatActivity {

    // Passed parameters
    Bundle r;
    private String theRegex;
    private String myTaskIs;
    public static final String CREATE_NEW_ENTRY="createNew";
    public static final String UPDATE_ENTRY="update";

    // UI
    private EditText regexInput;
    private EditText description;
    private RatingBar rbar;

    // DB
    private int key1;

    /**
     * On Create
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_regex_in_db);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // UI
        regexInput = (EditText) findViewById(R.id.the_regex);
        regexInput.setText(theRegex);
        rbar=(RatingBar) findViewById(R.id.evaluation);
        description=(EditText)findViewById(R.id.regex_description);

        // Get the regex string....
        r = getIntent().getExtras();

        // What is my task?
        // This activity can be called to add a new entry or to edit an existig
        // entry.
        myTaskIs=r.getString("myTaskIs");

        // Preset
        if (myTaskIs.equals(UPDATE_ENTRY)) {
            key1 = r.getInt("key1");
            // Gets entry and updates editText's
            getEntry(key1);
        }

        final String theRegex = r.getString("regexString");
        regexInput.setText(theRegex);

        /**
         * Buttons and actions
         *
         */

        // Save regex?
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Create a new entry
                if (myTaskIs.equals(CREATE_NEW_ENTRY)) {
                    // Empty regex?
                    if (!regexInput.getText().toString().trim().equals("")) {
                        // Does an DB entry for this regex already exist?
                        if (!DB.doesExist("regex", "regexstring", regexInput.getText().toString().trim(), MainActivity.conn)) {
                            saveRegex(regexInput.getText().toString(), description.getText().toString(),(int)rbar.getRating());
                        } else
                            Toast.makeText(getApplicationContext(), "Diese Regex gibt es schon. Nichts gespeichert", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Keine Regex eingegeben. Nichts gespeichert...", Toast.LENGTH_LONG).show();
                    }
                }

                // Update existing entry
                if (myTaskIs.equals(UPDATE_ENTRY)){
                    String theRegex=regexInput.getText().toString();
                    String descr=description.getText().toString();
                    float rat=rbar.getRating();
                    Log.v("RATING:",""+rat);
                    updateEntry(key1,theRegex,descr,(int)rat);
                }
            }
        });
    }

    /**
     * Save regex
     *
     * @param regex
     * @param description
     */

    private void saveRegex(String regex, String description,int rating) {
        // @rem:SQL, shows how today's date (current_timestamp) can be inserted into DB@@
        DB.insert("insert into regex (regexstring,description,date,rating) values " +
                "('" + regex + "','" + description + "',CURRENT_TIMESTAMP,"+rating+")", MainActivity.conn);

        Toast.makeText(getApplicationContext(), "Regex gespeichert", Toast.LENGTH_LONG).show();
        finish();
    }

    /**
     * Update regex
     *
     */

    private void updateEntry(int key1,String regex,String coment,int rating)
    {
        try {
            MainActivity.conn.createStatement().executeUpdate("update regex set regexstring='"+regex+"',description='"+coment+"',rating=" +rating + ",date=CURRENT_TIMESTAMP where key1=" + key1);
        }catch(SQLException e){
            Log.v("Sql Error:",e.toString());
        }
        Toast.makeText(getApplicationContext(), "Eintrag geändert", Toast.LENGTH_LONG).show();

        Intent in = new Intent(this, RegexPicker.class);
        startActivity(in);
        finish();
    }

    /**
     * Get existing entry
     *
     * If this activity is called in order to update an existing entry
     * this method gets this entry from the DB and writes it to the matching
     * editText's
     *
     * Only 'description' and 'rating' are needed. The 'regexString' is always passed
     * from the calling activity.
     */

    public void getEntry (int key1)
    {
        try {
            PreparedStatement selectPreparedStatement = MainActivity.conn.prepareStatement("select description,rating from regex where key1="+key1);
            ResultSet rs = selectPreparedStatement.executeQuery();
           if (rs.next()) {
               description.setText(rs.getString(1));
               rbar.setRating(rs.getInt(2));
           }else{
               Log.v ("GETENTRY:","No result");
           }

        }catch(SQLException e){
            Log.v("SQL ERROR",e.toString());
        }
    }
}