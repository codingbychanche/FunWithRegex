package berthold.funwithregex;

/*
 * MainActivity.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 11/1/18 10:20 AM
 */

/**
 * Test your favourite regex...
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.sql.Connection;


public class MainActivity extends AppCompatActivity implements FragmentCustomDialogYesNo.getDataFromFragment {

    // UI
    ProgressBar     loadProgress;
    ImageButton     run;
    ImageButton     delteText;
    ImageButton     magnifyText;
    ImageButton     shrinkText;
    EditText        theRegex;
    ImageButton     delRegex;
    EditText        testText;
    EditText        justResult;
    TextView        messageText;
    ImageButton     removeHighlights;
    View            lineBelowMessage;

    Switch          textViewSwitcher;

    ImageButton     insertRegexFromDB;
    ImageButton     insertRegexIntoDB;

    Button          insertPara;
    Button          insertCurlys;
    Button          insertComma;
    Button          insertBracket;
    Button          insertMinus;
    Button          insertCarret;
    Button          insertBackSlash;
    Button          insertFwdSlash;

    // For your convenience
    private boolean regexWasSaved;      // If true, regex was saved and can be deleted without bothering the user by asking...
    private boolean regexInsertFromDB;  // If true, regex was just taken from db.
                                        // When save- button is pressed, app asks if you want to update the
                                        // existing entry or if you wan't to create a new one
                                        // If false, a new entry will be created....


    private boolean textWasSaved;       // If true, text was saved and can be deleted...... same as with regex

    private int     key1;               // primary key for the regex picked from DB

    private int timesBackPressed;       // Prevent user from accidentially leave the app

    private float   textSize;           // Text size

    // DB
    String                         path;
    static public Connection       conn; // Database holding our regex'es.....

    // Req- codes
    public static final int LOAD_TEXT=1; // Request Code for file picker
    public static final int SAVE_TEXT=2; // Save text to file
    public static final int GET_REGEX=3; // Get a regex from DB

    // Req- codes for 'FragmentCustomDialogYesNo'
    public static final int DELETE_REGEX=3;
    public static final int DELETE_TEXT=4;
    public static final int UPDATE_REGEX=5;

    public static final int NOT_REQUIERED=-1;

    // Async tasks
    LoadText loader;
    RunRegex regexRunner;

    // Debug
    String          tag;

    // Shared preferences
    SharedPreferences sharedPreferences;

    // If this is true, regex is restored from shared preferences.
    // If it is false, regex was picked from list....
    boolean regexWasNotPicked;

    // File system
    public static File workingDir;
    public String appDir="/";       // App's working dir..

    /**
     * On Create
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Debug
        tag=MainActivity.class.getSimpleName();

        Log.v(tag,"On Create...");

        // Init...
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Init...
        // Those two will be set to 'false' when changed.....
        // Will be set to true, when saved.
        regexWasSaved=true;
        textWasSaved=true;
        regexWasNotPicked=true;

        // Current regex was not insert from DB to edit. It is a new one and a new
        // entry will be created when the user presses the save- button.
        regexInsertFromDB=false;
        key1=-1;

        // If back- buttom was pressed once, warn the user that he meight loose data if he presses again....
        // If back- button was pressed more than once, leave the app!
        timesBackPressed=0;

        // Text size
        textSize=20;

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

        // File system
        //
        // @rem:Filesystem, creates public folder in the devices externalStorage dir...@@
        //
        // This seems to be the best practice. It creates a public folder.
        // This folder will not be deleted when the app is de- installed
        workingDir= Environment.getExternalStoragePublicDirectory(appDir);
        Log.v("---Working dir",workingDir.getAbsolutePath());
        workingDir.mkdirs(); // Create dir, if it does not already exist

        // UI
        theRegex=(EditText)findViewById(R.id.the_regex);

        testText=(EditText)findViewById(R.id.test_text);
        loadProgress=(ProgressBar)findViewById(R.id.load_progress);

        delteText=(ImageButton)findViewById(R.id.delete_text);
        textViewSwitcher=(Switch)findViewById(R.id.switchresult);
        textViewSwitcher.setChecked(true);
        removeHighlights=(ImageButton)findViewById(R.id.remove_result);
        magnifyText=(ImageButton)findViewById(R.id.magnify_text);
        shrinkText=(ImageButton)findViewById(R.id.shrink_text);

        justResult=(EditText)findViewById(R.id.just_the_result);
        justResult.setVisibility(View.GONE);  // Vissible or not depents on 'textSwitcher'

        messageText=(TextView) findViewById(R.id.messages);
        lineBelowMessage=(View) findViewById(R.id.line_below_message);
        run=(ImageButton)findViewById(R.id.run);

        insertRegexFromDB=(ImageButton)findViewById(R.id.insert_regex_from_db);
        insertRegexIntoDB=(ImageButton) findViewById(R.id.insert_regex_into_db);

        delRegex=(ImageButton)findViewById(R.id.deleteRegexInput);
        insertCurlys=(Button)findViewById(R.id.curly);
        insertMinus=(Button)findViewById(R.id.minus);
        insertPara=(Button)findViewById(R.id.para);
        insertComma=(Button)findViewById(R.id.comma);
        insertBracket=(Button)findViewById(R.id.bracket);
        insertBackSlash=(Button)findViewById(R.id.backslash);
        insertFwdSlash=(Button)findViewById(R.id.forwardslash);
        insertCarret=(Button)findViewById(R.id.caret);
    }

    /**
     * OnResume
     *
     */

    @Override
    protected void onResume(){
        super.onResume();
        Log.v(tag,"On Resume.....");

        // Get last state....
        restoreFromSharedPreferences();

        // Hide message window as there is no message to be shown yet...
        // Vissibility will be set when there is something to output...
        hideMessageWindow();

        testText.setTextSize(textSize);

        // Remove highlight's from text
        removeHighlights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeHighlightFromTestText();
            }
        });

        // Switch, show result marked in text or just the result
        textViewSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    justResult.setVisibility(View.GONE);
                    testText.setVisibility(View.VISIBLE);
                } else {
                    justResult.setVisibility(View.VISIBLE);
                    testText.setVisibility(View.GONE);
                }
            }
        });

        // Text size
        magnifyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testText.setTextSize(textSize++);
            }
        });

        shrinkText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textSize>5) testText.setTextSize(textSize--);
            }
        });

        // Run regex.......
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAllTasks();
                regexRunner=new RunRegex(theRegex,testText,justResult,messageText,lineBelowMessage,loadProgress);
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
            }
        });

        // Insert regex into DB?
        insertRegexIntoDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAllTasks();
                Intent in = new Intent(v.getContext(), InsertRegexInDB.class);

                // If regex is empty, do nothing!
                if (!theRegex.getText().toString().isEmpty()) {

                    // If current regex string was not changed (just taken from DB) then do nothing. Else:
                    // If current entry was taken from DB, then ask the user if he wishes to update it.
                    // If not, create a new entry.
                    if (!regexWasSaved) {
                        if (regexInsertFromDB) {
                            FragmentManager fm = getSupportFragmentManager();
                            FragmentCustomDialogYesNo fragmentDeleteRegex =
                                    FragmentCustomDialogYesNo.newInstance(UPDATE_REGEX,
                                            FragmentCustomDialogYesNo.SHOW_AS_YES_NO_DIALOG,NOT_REQUIERED,
                                            null, getResources().getString(R.string.new_entry_or_update),
                                            getResources().getString(R.string.update_button),
                                            getResources().getString(R.string.new_entry_button));
                            fragmentDeleteRegex.show(fm, "fragment_dialog");
                        } else {
                            in.putExtra("myTaskIs", InsertRegexInDB.CREATE_NEW_ENTRY);
                            in.putExtra("regexString", theRegex.getText().toString());
                            regexWasSaved = true;
                            startActivity(in);
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Nichts geändert, nichts zum speichern...", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Keine Regex, nichts zum speichern da.....", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Delete regex?
        delRegex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!regexWasSaved && !theRegex.getText().toString().isEmpty()) {
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentCustomDialogYesNo fragmentDeleteRegex =
                            FragmentCustomDialogYesNo.newInstance(DELETE_REGEX,
                                    FragmentCustomDialogYesNo.SHOW_AS_YES_NO_DIALOG,NOT_REQUIERED,
                                    null, getResources().getString(R.string.delete_regex_dialog),
                                    getResources().getString(R.string.yes_button),
                                    getResources().getString(R.string.no_button));
                    fragmentDeleteRegex.show(fm, "fragment_dialog");
                } else{
                    deleteRegex();
                }
            }
        });

        // Delete text
        delteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.v(tag,"text was saved:"+textWasSaved);
                Log.v(tag,"text is empty:"+testText.getText().toString().isEmpty());
                if(!textWasSaved && !testText.getText().toString().isEmpty()) {
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentCustomDialogYesNo fragmentDeleteRegex =
                            FragmentCustomDialogYesNo.newInstance(DELETE_TEXT,
                                    FragmentCustomDialogYesNo.SHOW_AS_YES_NO_DIALOG,NOT_REQUIERED,
                                    null, getResources().getString(R.string.delete_text_dialog),
                                    getResources().getString(R.string.yes_button),
                                    getResources().getString(R.string.no_button));
                    fragmentDeleteRegex.show(fm, "fragment_custom_dialog_yes_no");
                } else{
                    deleteText();
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

        // Insert backslash
        insertBackSlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theRegex.getText().insert(theRegex.getSelectionStart(),"\\");
            }
        });

        // Insert minus
        insertMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theRegex.getText().insert(theRegex.getSelectionStart(),"-");
            }
        });

        // Insert minus
        insertCarret.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theRegex.getText().insert(theRegex.getSelectionStart(), "^");
            }
        });

        // Insert forwardslash
        insertFwdSlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theRegex.getText().insert(theRegex.getSelectionStart(), "/");
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

        // TestText
        testText.addTextChangedListener(new TextWatcher() {

            // Android documentation advises: Do not change charSequence from here!
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            // Android documentation advises: Do not change charSequence from here!
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                textWasSaved=false; // Regex changed, allow to prevent user from deleting the text by mistake
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
     * With startActivityForResult started activity's,
     * will return to this callback method
     *
     */

    @Override
    protected void onActivityResult(int reqCode,int resCode,Intent data)
    {
        // Load text
        if (reqCode==LOAD_TEXT){
            if (data!=null && data.hasExtra("path")) {

                final String path=data.getExtras().getString("path");

                stopAllTasks();

                loader=new LoadText(testText,messageText,loadProgress,path);
                loader.execute();

                textWasSaved=true;
            }
        }

        // Save text
        if(reqCode==SAVE_TEXT){
            textWasSaved=true;
        }

        // Get regex from database
        if (resCode==RESULT_OK && reqCode==GET_REGEX){

            if (data.hasExtra("theRegex")) {
                final String regex = data.getExtras().getString("theRegex");
                key1=data.getExtras().getInt("key1");
                theRegex.setText(regex);
                hideMessageWindow();
                regexWasSaved=true;
                regexInsertFromDB=true;
                regexWasNotPicked=false;
            }
        }
    }

    /**
     * Get input from yes/ no fragment
     *
     * @see FragmentCustomDialogYesNo
     */

    @Override
    public void getDialogInput(int reqCode,int data,String text,String buttonPressed)
    {
        switch (reqCode){

            // Delete Regex?
            case DELETE_REGEX:
                if (buttonPressed.equals(FragmentCustomDialogYesNo.BUTTON_OK_PRESSED)) {
                   deleteRegex();
                }
                break;

            // Delte text?
            case DELETE_TEXT:
                if(buttonPressed.equals(FragmentCustomDialogYesNo.BUTTON_OK_PRESSED)){
                    deleteText();
                }
                break;

            // Insert or update regex entry in database
            case UPDATE_REGEX:
                Intent in = new Intent(this, InsertRegexInDB.class);
                if (buttonPressed.equals(FragmentCustomDialogYesNo.BUTTON_OK_PRESSED)) {
                    in.putExtra("myTaskIs", InsertRegexInDB.UPDATE_ENTRY);
                    in.putExtra("key1", key1);
                    in.putExtra("regexString", theRegex.getText().toString());
                } else {
                    in.putExtra("myTaskIs", InsertRegexInDB.CREATE_NEW_ENTRY);
                    in.putExtra("regexString", theRegex.getText().toString());
                }
                startActivity(in);
                break;
        }
    }

    /**
     * @remSaves instance state@@
     *
     * @rem:Is called when the user leaves this activity or the screen orientation@@
     * @rem:is changed. In other words:@@
     *
     * @rem:* ONLY IF THE ACTIVITY IS DESTROYED BY THE SYSTEM!@@
     *
     * Here the contents of all 'editText' fields are saved to out state
     *
     * In this case 'onPause' or 'onStop' is called as well.
     *
     */

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        saveSettings();
        Log.v(tag,"State saved.....");
    }

    /**
     * Save current settings in shared preferences
     */
    public void saveSettings()
    {
        sharedPreferences=getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("theRegex",theRegex.getText().toString());
        editor.putString("testText",testText.getText().toString());
        editor.putBoolean ("regexWasSaved",regexWasSaved);
        editor.putBoolean("regexInsertFromDB",regexInsertFromDB);
        editor.putFloat("textSize",textSize);
        editor.commit();
    }

    /**
     * Restore from shared preferences
     *
     */
    public void restoreFromSharedPreferences()
    {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        if (regexWasNotPicked)
            theRegex.setText(sharedPreferences.getString("theRegex",""));

        regexInsertFromDB=sharedPreferences.getBoolean("regexInsertFromDB",regexInsertFromDB);
        testText.setText(sharedPreferences.getString("testText",""));
        regexWasSaved=sharedPreferences.getBoolean("regexWasSaved",regexWasSaved);
        textSize=sharedPreferences.getFloat("textSize",textSize);
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
     * If Back button was pressed
     *
     * @rem: Override "Back Button Pressed". Shows how to check if this button was pressed@@
     * @rem: Here it is checked to prevent the user from accidendily leave the app@@
     */

    @Override
    public void onBackPressed()
    {
        timesBackPressed++;
        if(timesBackPressed>1){
            saveSettings();
            finish();
        }
        else Toast.makeText(getApplicationContext(),getResources().getString(R.string.leave_warning),Toast.LENGTH_LONG).show();

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

                // Save text?
                case R.id.save:
                    Intent saveFile=new Intent (this,SaveDialog.class);
                    saveFile.putExtra("testText",testText.getText().toString());
                    saveFile.putExtra("justTheResult",justResult.getText().toString());
                    saveFile.putExtra("theRegex",theRegex.getText().toString());
                    saveFile.putExtra("path",workingDir);
                    this.startActivityForResult(saveFile,SAVE_TEXT);
                    break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Delete regex
     *
     */

    private void deleteRegex() {
        stopAllTasks();
        theRegex.setText("");
        messageText.setText("");
        regexInsertFromDB=false;
        hideMessageWindow();
        removeHighlightFromTestText();
        saveSettings();
    }

    /**
     * Delete text
     */

    private void deleteText(){

        stopAllTasks();
        testText.setText("");
        justResult.setText("");
        textWasSaved=true;
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
