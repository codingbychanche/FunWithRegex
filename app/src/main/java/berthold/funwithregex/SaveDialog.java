package berthold.funwithregex;

/*
 * SaveDialog.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 11/27/18 9:44 AM
 */

/**
 * Save Dialog
 */

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


public class SaveDialog extends AppCompatActivity implements FragmentCustomDialogYesNo.getDataFromFragment{

    // UI
    private ImageButton saveToFileLocally;
    private ImageButton sendByMail;
    private CheckBox attachRegex;
    private CheckBox attachResultText;
    private TextView sampleText;
    private ProgressBar progressBar;

    // Data to save...
    private String theRegex;
    private String testText;
    private String justTheResult;

    private String savePath;

    private SaveText saveText;

    // Debug
    private String tag;

    // Request codes
    private static final int SAVE_FILE=1;
    private static final int CONFIRM_TEXT_SAVE_LOC=2;

    private boolean SHOW_CONFIRM_SAVE_AT;
    private boolean SHOW_CONFIRM_OVERWRITE;

    private static final int NOT_REQUIERED=-1;

    /**
     * On create
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_dialog);

        // Debug
        tag="Debug: Main";

        // Get data passed..
        Bundle extras=getIntent().getExtras();
        theRegex=extras.getString("theRegex");
        testText=extras.getString("testText");
        justTheResult=extras.getString("justTheResult");

        // UI
        saveToFileLocally=(ImageButton)findViewById(R.id.save_text_localy);
        sendByMail=(ImageButton)findViewById(R.id.send_by_mail);
        attachRegex=(CheckBox)findViewById(R.id.attach_regex);
        attachResultText=(CheckBox)findViewById(R.id.attach_result);
        sampleText=(TextView)findViewById(R.id.sample_of_saved_text);
        progressBar=(ProgressBar)findViewById(R.id.save_progress);

        // Init some UI-components
        initUI();

        // Save file, opens the 'fileChooser'
        saveToFileLocally.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), FileChooserDeluxe.class);
                i.putExtra(FileChooserDeluxe.MY_TASK_FOR_TODAY_IS,FileChooserDeluxe.SAVE_FILE);
                i.putExtra("path",MainActivity.workingDir);
                startActivityForResult(i,SAVE_FILE);
            }
        });

        // Send text by mail
        sendByMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "FunWithRegex: Take this :-)");
                emailIntent.putExtra(Intent.EXTRA_TEXT, sampleText.getText());
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });

        // Any Checkbox changed?
        // Change preview of text to be saved accroding to the selection.....
        attachRegex.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                initUI();
            }
        });

        attachResultText.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                initUI();
            }
        });
    }

    /**
     * Callback for fileChooser
     *
     * Save file to the selected path
     *
     */

    @Override
    protected void onActivityResult(int reqCode,int resCode,Intent data)
    {
        if (resCode==RESULT_OK && reqCode==SAVE_FILE ){
            if (data.hasExtra("path")) {

                savePath=data.getExtras().getString("path");

                // When saving a file:
                // Check if just the folder was picked, in that case you have to take care
                // that a filename will be assigned or:
                // if a folder and an existing file was selected, in that case you may want to
                // check if the user wants the selected file to be overwritten...
                String returnStatus = data.getExtras().getString(FileChooserDeluxe.RETURN_STATUS);

                if (returnStatus!=null) {

                    // File was picked, ask user if he want's to overwrite it
                    if (returnStatus.equals(FileChooserDeluxe.FOLDER_AND_FILE_PICKED)) {
                     Log.v(tag,": Folder and file was picked.... ");
                        SHOW_CONFIRM_OVERWRITE=true;
                        SHOW_CONFIRM_SAVE_AT=false;
                    }

                    // Just the folder, ask user for filename
                    if (returnStatus.equals(FileChooserDeluxe.JUST_THE_FOLDER_PICKED)) {
                        Log.v(tag,": Just folder picked....");
                        SHOW_CONFIRM_OVERWRITE=false;
                        SHOW_CONFIRM_SAVE_AT=true;
                    }
                }
            }
        }
    }

    /**
     * On Resume
     */

    @Override
    protected void onResume()
    {
        super.onResume();

        if (SHOW_CONFIRM_SAVE_AT){
            showConfirmDialog(CONFIRM_TEXT_SAVE_LOC,
                    FragmentCustomDialogYesNo.SHOW_WITH_EDIT_TEXT,
                    getResources().getString(R.string.confirm_save_text_at),
                    getResources().getString(R.string.confirm_save_at),
                    getResources().getString(R.string.cancel_save));
        }

        if (SHOW_CONFIRM_OVERWRITE){
            showConfirmDialog(CONFIRM_TEXT_SAVE_LOC,
                    FragmentCustomDialogYesNo.SHOW_AS_YES_NO_DIALOG,
                    (getResources().getString(R.string.confirm_dialog_overwrite)+" \n"+savePath),
                    getResources().getString(R.string.confirm_save_at),
                    getResources().getString(R.string.cancel_save));
        }
    }

    /**
     * Callback for yesNoDialog
     *
     * @see FragmentCustomDialogYesNo
     *
     */

    @Override
    public void getDialogInput(int reqCode,int data,String filename,String buttonPressed)
    {
        // Callback from confirm dialog
        // Existing file will be overwritten or a new file will be created
        // depending on the users choice in previously shown confirm dialog.
        if(reqCode==CONFIRM_TEXT_SAVE_LOC){
            File f=new  File(savePath+"/"+filename);

            if (buttonPressed.equals(FragmentCustomDialogYesNo.BUTTON_OK_PRESSED)) {
                saveText = null;

                // User choose an existing file and he want's it to be overwritten
                if (SHOW_CONFIRM_OVERWRITE)  // Folder and file was picked, so, overwrite file....
                    saveText = new SaveText(sampleText.getText().toString(), getApplicationContext(), null, savePath);

                // User choose location for file to save at, append file obtained from dialog fragment and
                // append it to path.
                if (SHOW_CONFIRM_SAVE_AT) {
                    if (!f.exists()) {
                        // Choosen filename does not exist, save file.
                        saveText = new SaveText(sampleText.getText().toString(), getApplicationContext(), null, savePath + "/" + filename);
                        System.out.println("Sample:" + sampleText.getText());
                    } else {
                        // If user coose a filename of an existing file, then change it, and do not overwrite
                        // existing file!
                        // @todo Improve: Open dialog, allow user to change filename or overwrite existing one
                        saveText = new SaveText(sampleText.getText().toString(), getApplicationContext(), null, savePath + "/" + filename + "_NEW");
                        Toast.makeText(getApplicationContext(), "Die Datei " + filename + " gibt es schon. Neuer Dateiname:" + filename + "_NEW. Alte Datei bleibt!", Toast.LENGTH_LONG).show();
                    }
                }
                saveText.execute();
            }
            if (buttonPressed.equals(FragmentCustomDialogYesNo.BUTTON_CANCEL_PRESSED))
                Log.v(tag," Button:"+buttonPressed);
        }
    }

    /**
     * Init UI components
     *
     */

    private void initUI() {

        SHOW_CONFIRM_OVERWRITE=false;
        SHOW_CONFIRM_SAVE_AT=false;

        sampleText.setText("");

        if (attachRegex.isChecked()){
            sampleText.setText("Angewendete Regex:\n");
            sampleText.append(theRegex+"\n\n");
            sampleText.append("Getesteter Text:");
            sampleText.append("\n\n");

        }
        sampleText.append(testText);

        if(attachResultText.isChecked()){
            sampleText.append("\n\n");
            sampleText.append("Ergebnisse:");
            sampleText.append("\n\n");
            sampleText.append(justTheResult);
        }
    }

    /*
     * Show confirm at dialog
     *
     * Asks the user to confirm the location to save his file to
     */

    private void showConfirmDialog(int reqCode,int kindOfDialog,String dialogText,String confirmButton,String cancelButton)
    {
        FragmentManager fm = getSupportFragmentManager();
        FragmentCustomDialogYesNo fragmentDeleteRegex =
                FragmentCustomDialogYesNo.newInstance(reqCode,kindOfDialog,NOT_REQUIERED,null,dialogText,confirmButton,cancelButton);
        fragmentDeleteRegex.show(fm, "fragment_dialog");
    }

}
