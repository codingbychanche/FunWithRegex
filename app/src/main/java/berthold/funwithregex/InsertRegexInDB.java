package berthold.funwithregex;

/**
 * Insert regex in DB
 *
 *
 */

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class InsertRegexInDB extends AppCompatActivity {

    String theRegex;

    // UI
    private EditText regexInput;
    private EditText description;


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

        description=(EditText)findViewById(R.id.regex_description);

        // Get the regex string....
        Bundle r = getIntent().getExtras();
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
                // Empty regex?
                if (!regexInput.getText().toString().trim().equals("")){
                    // Does an DB entry for this regex already exist?
                    if (!DB.doesExist("regex","regexstring",regexInput.getText().toString().trim(),MainActivity.conn)) {
                        saveRegex(regexInput.getText().toString(), description.getText().toString());
                    } else
                        Toast.makeText(getApplicationContext(), "Diese Regex gibt es schon. Nichts gespeichert", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Keine Regex eingegeben. Nichts gespeichert...", Toast.LENGTH_LONG).show();
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

    private void saveRegex(String regex, String description) {
        DB.insert("insert into regex (regexstring,description,date) values " +
                "('" + regex + "','" + description + "',CURRENT_TIMESTAMP)", MainActivity.conn);

        Toast.makeText(getApplicationContext(), "Regex gespeichert", Toast.LENGTH_LONG).show();
        finish();
    }
}