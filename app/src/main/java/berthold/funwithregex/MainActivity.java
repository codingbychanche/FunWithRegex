package berthold.funwithregex;

/*
 * MainActivity.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 3/23/18 10:12 PM
 */

/**
 * Test your favourite regex...
 */

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

public class MainActivity extends AppCompatActivity implements FragmentCustomDialogYesNo.getDataFromFragment {

    // UI
    ProgressBar     searchProgress;
    ImageButton     run;
    ImageButton     delteText;
    EditText        theRegex;
    ImageButton     delRegex;
    EditText        testText;
    EditText        justResult;
    TextView        messageText;
    View            lineBelowMessage;

    Switch          textViewSwitcher;

    ImageButton     insertRegexFromDB;
    ImageButton     insertRegexIntoDB;

    Button          insertPara;
    Button          insertCurlys;
    Button          insertComma;
    Button          insertBracket;

    // For your convenience
    private boolean regexWasSaved;      // If true, regex was saved and can be deleted without bothering the user by asking...
    private boolean textWasSaved;       // If true, text was saved and can be deleted...... same as with regex

    // Settings
    private String workingDir="/";

    // DB
    String                         path;
    static public Connection       conn; // Database holding our regex'es.....

    // Req- codes
    public static final int LOAD_TEXT=1;    // Request Code for file picker
    public static final int GET_REGEX=2;    // Get a regex from DB

    // Req- codes for 'FragmentCustomDialogYesNo'
    public static final int DELETE_REGEX=3;

    // Async tasks
    LoadText loader;
    RunRegex regexRunner;

    // Debug
    String          tag;

    /**
     * On Create
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.v(tag,"On Create...");

        // Init...
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Debug
        tag="Debug: Main";

        // Init...
        // Those two will be set to 'false' when changed.....
        // Will be set to true, when saved.
        regexWasSaved=true;
        textWasSaved=true;

        // Get instance state
        if (savedInstanceState!=null) {
            regexWasSaved = savedInstanceState.getBoolean("regexWasSaved");
            textWasSaved = savedInstanceState.getBoolean("textWasSaved");
        }

        // Create DB
        // This is our regex library.....
        String dbName = "/regexlist";

        File f = getFilesDir();
        path = (f.getAbsolutePath() + dbName);

        try {
            CreateDB.make(path);
            Log.i(tag, "DB Created on:\n");
            Log.i(tag, path);
        } catch (Exception e) {
            Log.i(tag,"Error creating DB:"+e.toString());
        }

        // Read DB
        String DB_DRIVER = "org.h2.Driver";
        String DB_CONNECTION = "jdbc:h2:" + path;
        String DB_USER = "";
        String DB_PASSWORD = "";

        try {
            Log.d(tag, "Reading:" + DB_CONNECTION + "\n");
            conn = DB.read(DB_DRIVER, DB_CONNECTION, DB_USER, DB_PASSWORD);

        } catch (Exception e) {
            Log.d(tag, "Error opening DB\n");
            Log.d(tag, e.toString());
        }

        // UI
        theRegex=(EditText)findViewById(R.id.the_regex);

        testText=(EditText)findViewById(R.id.test_text);

        delteText=(ImageButton)findViewById(R.id.delete_text);

        textViewSwitcher=(Switch)findViewById(R.id.switchresult);
        textViewSwitcher.setChecked(true);

        justResult=(EditText)findViewById(R.id.just_the_result);
        justResult.setVisibility(View.GONE);  // Vissible or not depents on 'textSwitcher'

        messageText=(TextView) findViewById(R.id.messages);
        lineBelowMessage=(View) findViewById(R.id.line_below_message);
        run=(ImageButton)findViewById(R.id.run);
        // searchProgress=(ProgressBar)findViewById(R.id.progress);

        insertRegexFromDB=(ImageButton)findViewById(R.id.insert_regex_from_db);
        insertRegexIntoDB=(ImageButton) findViewById(R.id.insert_regex_into_db);

        delRegex=(ImageButton)findViewById(R.id.deleteRegexInput);
        insertCurlys=(Button)findViewById(R.id.curly);
        insertPara=(Button)findViewById(R.id.para);
        insertComma=(Button)findViewById(R.id.comma);
        insertBracket=(Button)findViewById(R.id.bracket);
    }

    /**
     * OnResume
     *
     */

    @Override
    protected void onResume(){
        super.onResume();
        Log.v(tag,"On Resume.....");


        // Hide message window as there is no message to be shown yet...
        // Vissibility will be set when there is something to output...
        hideMessageWindow();

        // Buttons and Actions
        final String switcherTrue="Ganzer Text, markiere Treffer";
        final String switcherFalse="Nur die Ergebnisse";
        if (textViewSwitcher.isChecked()) textViewSwitcher.setText(switcherTrue);
        else textViewSwitcher.setText(switcherFalse);

        // Delete text
        delteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAllTasks();
                testText.setText(" ");
                justResult.setText(" ");
            }
        });

        // Switch, show result marked in text or just the result
        textViewSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    textViewSwitcher.setText(switcherTrue);
                    justResult.setVisibility(View.GONE);
                    testText.setVisibility(View.VISIBLE);
                } else {
                    textViewSwitcher.setText(switcherFalse);
                    justResult.setVisibility(View.VISIBLE);
                    testText.setVisibility(View.GONE);
                }
            }
        });

        // Run regex.......
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAllTasks();
                regexRunner=new RunRegex(theRegex,testText,justResult,messageText,lineBelowMessage,searchProgress);
                regexRunner.execute();
            }
        });

        // Insert regex from DB?
        insertRegexFromDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAllTasks();
                Intent in = new Intent(v.getContext(), RegexPicker.class);
                startActivityForResult(in,GET_REGEX);
                regexWasSaved=true;
            }
        });

        // Insert regex into DB?
        insertRegexIntoDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAllTasks();
                Intent in = new Intent(v.getContext(), InsertRegexInDB.class);
                in.putExtra("regexString",theRegex.getText().toString());
                regexWasSaved=true;
                startActivity(in);
            }
        });

        // Delete regex?
        delRegex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!regexWasSaved) {
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentCustomDialogYesNo fragmentDeleteRegex =
                            FragmentCustomDialogYesNo.newInstance(DELETE_REGEX,
                                    FragmentCustomDialogYesNo.SHOW_AS_YES_NO_DIALOG,
                                    null, getResources().getString(R.string.delete_regex_dialog),
                                    getResources().getString(R.string.yes_button),
                                    getResources().getString(R.string.no_button));
                    fragmentDeleteRegex.show(fm, "fragment_dialog");
                } else{
                    deleteRegex();
                }
            }
        });

        // Convenience buttons.....
        // Insert curly brackets
        insertCurlys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theRegex.getText().insert(theRegex.getSelectionStart(),"{}");
                theRegex.setSelection(theRegex.getSelectionStart()-1);
            }
        });

        // Insert parathensis
        insertPara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theRegex.getText().insert(theRegex.getSelectionStart(),"()");
                theRegex.setSelection(theRegex.getSelectionStart()-1);
            }
        });

        // Insert brackets
        insertBracket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theRegex.getText().insert(theRegex.getSelectionStart(),"[]");
                theRegex.setSelection(theRegex.getSelectionStart()-1);
            }
        });

        // Insert comma
        insertComma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theRegex.getText().insert(theRegex.getSelectionStart(),",");
            }
        });

        // Text watchers
        // Callbacks for 'editText's......

        // Regex
        theRegex.addTextChangedListener(new TextWatcher() {

            // Android documentation advises: Do not change charSequence from here!
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            // Android documentation advises: Do not change charSequence from here!
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                regexWasSaved=false; // Regex changed, allow to prevent user from deleting the text by mistake
            }

            // Android documentation tells, that you may change the editable from here!
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    /**
     * Callbacks
     */

    /**
     * Get input from yes/ no fragment
     *
     * @see FragmentCustomDialogYesNo
     */

    @Override
    public void getDialogInput(int reqCode,String text,String buttonPressed)
    {
        switch (reqCode){

            // Delete Regex?
            case DELETE_REGEX:
                if (buttonPressed.equals(FragmentCustomDialogYesNo.BUTTON_OK_PRESSED)) {
                   deleteRegex();
                }
                break;
        }
    }

    /**
     * Saves instance state
     *
     * Is called when the user leaves this activity or the screen orientation
     * is changed. In other words:
     *
     * ONLY IF THE ACTIVITY IS DESTROYED BY THE SYSTEM!
     *
     * Here the contents of all 'editText' fields are saved to out state
     *
     * In this case 'onPause' or 'onStop' is called as well.
     *
     */

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("theRegex",theRegex.getText().toString());
        outState.putString("testText",testText.getText().toString());
        outState.putBoolean("regexWasSaved",regexWasSaved);
        outState.putBoolean("textWasSaved",textWasSaved);
        Log.v(tag,"State saved.....");
    }

    /**
     * The menu, located in action bar...
     *
     * @param menu
     * @return
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * If menu item was selected...
     *
     * @param item
     * @return
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        stopAllTasks();

        switch (item.getItemId()) {

                // Show info?
                case R.id.about:
                    Intent in = new Intent(this, About.class);
                    this.startActivity(in);
                    break;

                // Load ?
                case R.id.load:
                    Intent i = new Intent(this, FileChooserDeluxe.class);
                    i.putExtra(FileChooserDeluxe.MY_TASK_FOR_TODAY_IS,FileChooserDeluxe.GET_FILE_NAME_AND_PATH);
                    i.putExtra("path",workingDir);
                    this.startActivityForResult(i,LOAD_TEXT);
                    break;

                // Save ?
                case R.id.save:
                    Intent saveFile=new Intent (this,SaveDialog.class);
                    saveFile.putExtra("testText",testText.getText().toString());
                    saveFile.putExtra("justTheResult",justResult.getText().toString());
                    saveFile.putExtra("theRegex",theRegex.getText().toString());
                    saveFile.putExtra("path",workingDir);
                    this.startActivity(saveFile);
                    break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * With startActivityForResult started activity's,
     * will return to this callback method
     *
     */

    @Override
    protected void onActivityResult(int reqCode,int resCode,Intent data)
    {
        // Load text
        if (resCode==RESULT_OK && reqCode==LOAD_TEXT){
            if (data.hasExtra("path")) {

                final String path=data.getExtras().getString("path");

                stopAllTasks();
                justResult.setText(" ");

                loader=new LoadText(testText,messageText,searchProgress,path);
                loader.execute();
            }
        }
        if (resCode==RESULT_OK && reqCode==GET_REGEX){

            if (data.hasExtra("theRegex")) {

                final String regex = data.getExtras().getString("theRegex");
                theRegex.setText(regex);
                hideMessageWindow();
            }
        }
    }

    /**
     * Delete regex
     *
     */

    private void deleteRegex() {
        stopAllTasks();
        theRegex.setText("");
        messageText.setText("");
        hideMessageWindow();
        removeHighlightFromTestText();
    }

    /**
     * Show message window
     *
     */

    private void showMessageWindow(){
        messageText.setVisibility(View.VISIBLE);
        lineBelowMessage.setVisibility(View.VISIBLE);
    }

    /**
     * Hide message window
     *
     */

    private void hideMessageWindow(){
        messageText.setVisibility(View.GONE);
        lineBelowMessage.setVisibility(View.GONE);
    }

    /**
     * Stopp async tasks
     *
     * Should be called before a new task is started
     */

    public void stopAllTasks(){
        //searchProgress.setVisibility (View.GONE);
        if (loader!=null) loader.cancel(true);
        if (regexRunner!=null) regexRunner.cancel(true);
        hideMessageWindow();
        return;
    }

    /**
     * Remove highlighting from text in 'testText' and
     * delete result
     *
     */

    private void removeHighlightFromTestText(){
        testText.setText(testText.getText().toString());
        justResult.setText(" ");
        hideMessageWindow();
    }
}
