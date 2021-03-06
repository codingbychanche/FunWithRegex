package berthold.funwithregex;

/*
 * ActivityExportDBAsCsv.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 10/6/18 5:59 PM
 */

/**
 * Exports the regex db as a csv- table
 *
 * @todo: Saving is done on UI- Thread. Exporting large files could block the App......
 */

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ActivityExportDBAsCsv extends AppCompatActivity implements FragmentCustomDialogYesNo.getDataFromFragment{

    // UI
    ImageButton save;
    ImageButton sendByMail;
    EditText    csvSepparator;
    ProgressBar pBar;

    // Req codes
    private final static int SAVE_CSV=1;
    private final static int CONFIRM_CSV_SAVE_LOC=2;
    private final static int CONFIRM_CSV_OVERWRITE=3;

    private boolean SHOW_CONFIRM_SAVE_AT=false;
    private boolean SHOW_CONFIRM_OVERWRITE=false;

    private static int NOT_REQUIERED=-1;

    // Filesystem
    private String path;

    // Async tasks
    private SaveText saveText;

    /**
     * On Create
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_csvdialog);

        // UI
        save=(ImageButton)findViewById(R.id.save_csv_localy);
        sendByMail=(ImageButton)findViewById(R.id.export_csv_by_mail);
        csvSepparator=(EditText)findViewById(R.id.csv_separator);
        pBar=(ProgressBar)findViewById(R.id.csv_pbar);

        // Actions
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), FileChooserDeluxe.class);
                i.putExtra(FileChooserDeluxe.MY_TASK_FOR_TODAY_IS,FileChooserDeluxe.SAVE_FILE);
                i.putExtra("path",MainActivity.workingDir);
                startActivityForResult(i,SAVE_CSV);
            }
        });

        // Send text by mail
        sendByMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String csvTable=tableToCsv(csvSepparator.getText().toString());
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "FunWithRegex: Take this :-)");
                emailIntent.putExtra(Intent.EXTRA_TEXT, csvTable);
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });
    }

    /*
     * Callbacks
     */

    /**
     * Callback for file picker
     *
     */

    @Override
    protected void onActivityResult(int reqCode,int resCode,Intent data)
    {
        if (resCode==RESULT_OK && reqCode==SAVE_CSV ){
            if (data.hasExtra("path")) {

                path=data.getStringExtra("path");

                // When saving a file:
                // Check if just the folder was picked, in that case you have to take care
                // that a filename will be assigned or:
                // if a folder and an existing file was selected, in that case you may want to
                // check if the user wants the selected file to be overwritten...
                String returnStatus = data.getExtras().getString(FileChooserDeluxe.RETURN_STATUS);

                if (returnStatus!=null) {

                    // File was picked, ask user if he want's to overwrite it
                    if (returnStatus.equals(FileChooserDeluxe.FOLDER_AND_FILE_PICKED)) {
                        SHOW_CONFIRM_SAVE_AT=false;
                        SHOW_CONFIRM_OVERWRITE=true;
                    }

                    // Just the folder, ask user for filename
                    if (returnStatus.equals(FileChooserDeluxe.JUST_THE_FOLDER_PICKED)) {
                        SHOW_CONFIRM_OVERWRITE=false;
                        SHOW_CONFIRM_SAVE_AT=true;
                    }
                }
            }
        }
    }

    /**
     * On Resume
     *
     */

    @Override
    public void onResume()
    {
        super.onResume();

        // @rem:Bug api level >11. When starting fragment before 'onResume' was called:@@
        // @rem:java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState@@
        // @rem:This is a workaround for this bug!@@
        // @rem:toDo Insert link for see: ... stack overflow..@@
        if (SHOW_CONFIRM_SAVE_AT){
           showConfirmDialog(CONFIRM_CSV_SAVE_LOC,
                   FragmentCustomDialogYesNo.SHOW_WITH_EDIT_TEXT,
                   getResources().getString(R.string.confirm_dialog_save_at),
                   getResources().getString(R.string.confirm_save_at),
                   getResources().getString(R.string.cancel_save));
        }

        if (SHOW_CONFIRM_OVERWRITE){
            showConfirmDialog(CONFIRM_CSV_OVERWRITE,
                    FragmentCustomDialogYesNo.SHOW_AS_YES_NO_DIALOG,
                    (getResources().getString(R.string.confirm_dialog_overwrite)+" \n"+path),
                    getResources().getString(R.string.confirm_save_at),
                    getResources().getString(R.string.cancel_save));
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

    /**
     * Callback for yesNoDialog
     *
     * @see FragmentCustomDialogYesNo
     *
     */

    @Override
    public void getDialogInput(int reqCode,int data,String filename,String buttonPressed) {
        // Callback from confirm dialog
        // Existing '*.csv' file will be overwritten or a new file will be created
        // depending on the users choice in previously shown confirm dialog.
        File f = new File(path + "/" + filename);

        if (reqCode == CONFIRM_CSV_SAVE_LOC) {
            if (buttonPressed.equals(FragmentCustomDialogYesNo.BUTTON_OK_PRESSED)) {

                // If filename does not exist, save file....
                if (!f.exists())
                    saveCsv(path + "/" + filename);
                else {
                    // ... If filename exists, append '_NEW' to filename. Do not overwrite
                    // existing file.
                    String newFilename = filename + "_NEW";
                    saveCsv(path + "/" + newFilename);
                    Toast.makeText(getApplicationContext(), "Die Datei " + filename + " gibt es schon. Neue Datei unter " + newFilename + " abgelegt.", Toast.LENGTH_LONG).show();
                }

            } else
                // Cancel button pressed, do nothing.....
                Toast.makeText(getApplicationContext(), "Nichts gespeichert....", Toast.LENGTH_LONG).show();
        }
        // If file was picked, overwrite
        if (reqCode == CONFIRM_CSV_OVERWRITE) {
            saveCsv(path);
        }
    }

    /*
     * Save CSV
     */

    private void saveCsv(String path)
    {
        String csvTable=tableToCsv(csvSepparator.getText().toString());
        if (saveText!=null) saveText.cancel(true);
        saveText=new SaveText(csvTable,getApplicationContext(),pBar,path);
        saveText.execute();
    }

    /*
     * Create CSV
     */

    private String tableToCsv(String sepparator)
    {
        StringBuilder csvTable=new StringBuilder();

        try {
            PreparedStatement selectPreparedStatement = null;
            selectPreparedStatement = MainActivity.conn.prepareStatement("select date,regexstring,description,rating from regex");
            ResultSet rs = selectPreparedStatement.executeQuery();

            while(rs.next()){
                String dateTime=FormatTimeStamp.german (rs.getString(1).toString(),FormatTimeStamp.WITH_TIME);
                csvTable.append (dateTime+sepparator);
                csvTable.append (rs.getString(2)+sepparator);
                csvTable.append (rs.getString(3)+sepparator);
                csvTable.append (rs.getInt(4)+"\n");
            }
            System.out.println(csvTable.toString());
        }catch (SQLException e){
            Log.v("CSV export error:",e.toString());
        }
        return csvTable.toString();
    }
}
