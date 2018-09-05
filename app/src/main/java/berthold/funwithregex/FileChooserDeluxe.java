/**
 * <b>File Picker Deluxe</b>
 *
 * Lets the user browse the devices file system and returns the selected files path.
 *
 * This version shows how 'FilePicker' can be used from another activity and how
 * it communicates with this activity. Check out 'finishIt()' method to see how
 * it works.
 *
 * This is version uses a custom- list view to display the file system in a neat way
 *
 * @author  Berthold Fritz
 * @date    3/2018
 */

package berthold.funwithregex;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.File;
import java.util.ArrayList;

public class FileChooserDeluxe extends AppCompatActivity {

    Bundle savedState;

    // GUI
    private ListView myListView;
    private ProgressBar progress;
    private ActionBar ab;

    // Filesystem
    private File currentPath;                                       // The current path
    private String root="/";                                        // Initial path, root if not specified via extras
    private File [] fileObjects;                                    // Contains the current folder's files as file- objects in the same order as 'directory'

    // List view
    private FileListAdapter myListAdapter=null;

    // Async task
    private FileListFillerV5 task;

    // UI
    FloatingActionButton saveNow;

    // Return values
    // One or mor of these values may be written to the bundles 'Extras' when
    // the activity is finished. Which depends on the task this activity was given by
    // the caller.

    public String status;
    public static final String RETURN_STATUS="status";
    public static final String FOLDER_AND_FILE_PICKED="folderAndFilePicked";
    public static final String JUST_THE_FOLDER_PICKED="justFolder";

    // Activity control
    // You can configure this activity by setting the approbiate 'key' - 'value' in the Bundle's
    // extra's. Supported key/ value pairs are listed here.
    //
    // Alway's set the string "MY_TASK_FOR_TODAY_IS" as the key. Choose one of the listed
    // values to perform the dessiered action

    private String                  myTaskForTodayIs;       // Tasks are saved in this string upon 'onCreate'
    public static final String      MY_TASK_FOR_TODAY_IS="myTaskForTodayIs"; // Key...

        // This flag must be set!
        // It tells the activity to act like a simple dir viewer. The activity will return the
        // full path of the file selected.

        public static final String GET_FILE_NAME_AND_PATH="getFileAndPath";

        // If this flag is set upon creation, the calling requests a path
        // in order to write a file. If set, the folowing happens here:
        //
        //  1.  a save button is displayed. When pressed, the activity returns
        //      to the calling activity and provides the path which was displayed when
        //      the button was pressed. Now the caller can proceed and save it's data...
        //
        //  2.  When a file was picked, the full path is returned to the calling
        //      activity. A flag is returned, telling the caller, that a file was
        //      selected. The caller can ask the user of he want's to replace this file...
        //
        //  If this flag was not set, the activity act's as a directory disply tool which
        //  returns each readable file the user picked.

        public static final String      SAVE_FILE="saveFile";                    // Value


    /**
     * Activity starts here...
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_chooser);
        savedState=savedInstanceState;

        ab=getSupportActionBar();
        ab.setTitle("Dateiauswahl");

        // What do you wan't me to do???
        // The string 'myTaskForTodayIs' contains the task to be performed (e.g. 'SAVE_FILE')
        Bundle extra=getIntent().getExtras();
        myTaskForTodayIs = extra.getString(MY_TASK_FOR_TODAY_IS);
        Log.v("---","--My task is:"+myTaskForTodayIs);

        // UI

        // Display the save button only, if this activity was called in order to
        // save a file....
        saveNow=(FloatingActionButton)findViewById(R.id.returnAndSave);
        if(!myTaskForTodayIs.equals(SAVE_FILE))
            saveNow.setVisibility(View.GONE);

        // Create list view
        myListView=(ListView)findViewById(R.id.mylist);

        ArrayList<FileListOneEntry> dir=new ArrayList<>();       // Array containing the current path's dir
        myListAdapter=new FileListAdapter(this,dir);
        myListView.setAdapter(myListAdapter);

        // Get current path, directory as string array and as file object- list
        if(extra.get("path")!=null) root=extra.get("path").toString();
        Log.v("--- Working Dir",root);
        currentPath=new File (root);
        refreshFiles(currentPath,myListAdapter);

        // List view
        myListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    // If a file was selected, get it's full path name and return to
                    // the calling activity
                    if (fileObjects[position].isFile() && fileObjects[position].canRead()){
                        currentPath=fileObjects[position].getAbsoluteFile();
                        status=FOLDER_AND_FILE_PICKED;
                        finishIt();
                    }

                    // If list item is a directory, show it's files
                    if (fileObjects[position].isDirectory()) {

                        // File should be readable and it should exist (e.g. folder 'sdcard')
                        // it might be shown but it could not be mounted yet. In that case
                        // it does not exist :-)
                        if(fileObjects[position].canRead() && fileObjects[position].exists()) {

                            // Get path and display it
                            currentPath = fileObjects[position].getAbsoluteFile();
                            ActionBar ab=getSupportActionBar();
                            ab.setSubtitle(currentPath.toString());

                            // Refresh file object list and file ListArray
                            // Update listView => show changes
                            refreshFiles(currentPath,myListAdapter);
                            myListAdapter.notifyDataSetChanged();

                        }
                    }
                }
        });

        //Set list on onnScrollListener
        //Here you can check which the first visible item is and how many
        //items are visible
        myListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            int first,last;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // ToDo: How can we update just the items, that are visible....?
                System.out.println("----updating from:"+first+"   to:"+last);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                System.out.println("OnScroll:"+firstVisibleItem+"    #ofItems"+visibleItemCount);
                first=firstVisibleItem;
                last=firstVisibleItem+visibleItemCount;
            }

        });

        // Save button
        // Will not be displayed if 'MY_TASK_FOR_TODAY_IS' is set to 'GET_FILE_NAME_AND_PATH'
        saveNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status=JUST_THE_FOLDER_PICKED;
                finishIt();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle s){
        Log.v("Main","Save state-------"+currentPath+"  "+task);
        s.putString("path",currentPath.toString());
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (task!=null) task.cancel(true);
        Log.v("Main","ONPAUSEEEEEEE--------"+currentPath+"  "+task);
    }

    @Override
    protected void onResume (){
        super.onResume();
        if(savedState!=null) currentPath=new File(savedState.getString("path"));
        // Display current path
        ab.setSubtitle(currentPath.toString());
        Log.v("Main","ONRESUME----------"+currentPath+"  "+task);
        refreshFiles(currentPath,myListAdapter);
    }

    /**
     * Options menu
     *
     * @param   menu
     * @since   7/2017
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_chooser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // If up button was pressed, move to parent dir or leave activity if
        // we are already at "/"
        if (id == R.id.goup) {
            if (currentPath.toString().equals("/")){
                // Cancel async task, if picture thumbnails for the
                // current dir are created....
                finishIt();
            } else {
                File parent = currentPath.getParentFile();
                currentPath = parent;
                ActionBar ab=getSupportActionBar();
                ab.setSubtitle(currentPath.toString());
                refreshFiles(parent,myListAdapter);
            }
        }
        return super.onOptionsItemSelected(item);
    }

        /**
         * Refresh array containing file- objects
         *
         */

        private void refreshFiles (File path,FileListAdapter a)
        {
            if (task!=null) task.cancel(true);
            // Get all file objects of the current path
            File files=new File(path.getPath());
            File []fo=files.listFiles();
            // Sort by kind, and do not show files that are not readable
            fileObjects=SortFiles.byKind(fo);

            // Clear dir array
            a.clear();
            progress=(ProgressBar)findViewById(R.id.pbar);
            task=new FileListFillerV5(a, fileObjects, 0,fileObjects.length-1,getApplicationContext(),progress);
            task.execute();

            return;
        }

        /**
         * Leave activity
         *
         */

        public void finishIt()
        {
            task.cancel(true);
            // Enter the caller class's name here (second parameter)
            Intent i=new Intent(FileChooserDeluxe.this,MainActivity.class);
            i.putExtra("path",currentPath.toString());

            // Set return status
            i.putExtra(RETURN_STATUS, status);
            Log.v("------", "Picked "+status);

            // That's it....
            setResult(RESULT_OK,i);
            super.finish();
        }
    }
