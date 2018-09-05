package berthold.funwithregex;
/**
 * Save Dialog
 *
 * Input filename to save, pick dir to save at.
 */

import android.content.DialogInterface;

import android.content.Intent;
import android.media.Image;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
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

import java.io.BufferedReader;
import java.io.FileReader;


public class SaveDialog extends AppCompatActivity {

    // UI
    private EditText fileName;
    private ImageButton saveToFileLocally;
    private CheckBox attachRegex;
    private CheckBox attachResultText;
    private TextView sampleText;
    private ProgressBar progressBar;

    // Data to save...
    private String theRegex;
    private String testText;
    private String justTheResult;

    private String savePath;

    private saveText saver;

    // Debug
    private String tag;

    // Request codes
    private static final int SAVE_FILE=1;

    // File Sys
    private String workingDir="/";

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

        if(extras.get("path")!=null) workingDir=extras.get("path").toString();

        // UI
        //fileName=(EditText) findViewById(R.id.file_name);
        saveToFileLocally=(ImageButton)findViewById(R.id.save_text_localy);
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

                if (!fileName.getText().toString().isEmpty()) {
                    Intent i = new Intent(SaveDialog.this, FileChooserDeluxe.class);
                    i.putExtra(FileChooserDeluxe.MY_TASK_FOR_TODAY_IS, FileChooserDeluxe.SAVE_FILE);
                    i.putExtra("path",workingDir);
                    startActivityForResult(i, SAVE_FILE);
                }else{
                    Toast.makeText(getApplicationContext(),"Bitte einen Dateinamen eingeben...", Toast.LENGTH_LONG).show();
                }
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
                String returnStatus=data.getExtras().getString(FileChooserDeluxe.RETURN_STATUS);
                Log.v(tag+" Return Status is:"+returnStatus+"---","Saving.."+savePath);

                // Confirm Dialog
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) dialogReturn(true);
                        if (which == DialogInterface.BUTTON_NEGATIVE) dialogReturn(false);
                    }
                };

                // Build Dialog
                final AlertDialog.Builder chooseYesNo = new AlertDialog.Builder(this);

                if (returnStatus.equals("folderAndFilePicked")) {
                    chooseYesNo.setMessage("Möchten Sie die Datei:"+ savePath+ "wirklich ersetzen?").setPositiveButton(R.string.yes, dialogClickListener).
                            setNegativeButton(R.string.no, dialogClickListener);
                }

                if (returnStatus.equals("justFolder")){
                    chooseYesNo.setMessage("Datei:"+fileName.getText()+" im Ordner "+savePath+" speichern?").setPositiveButton(R.string.yes, dialogClickListener).
                            setNegativeButton(R.string.no, dialogClickListener);
                }
                chooseYesNo.show();
            }
        }
    }

    /**
     * Callback for yesNoDialog.
     *
     * @param confirmed
     */

    public void dialogReturn(boolean confirmed){

        if (confirmed) {

            // Save file
            saver = new saveText(sampleText.getText().toString(),getApplicationContext(),progressBar, savePath + "/" + fileName.getText().toString());
            saver.execute();
        }
    }

    /**
     * Init UI components
     *
     */

    private void initUI() {

        sampleText.setText(" ");

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

}
