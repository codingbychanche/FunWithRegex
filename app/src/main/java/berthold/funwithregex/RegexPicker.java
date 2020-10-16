package berthold.funwithregex;

/*
 * RegexPicker.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 9/24/18 8:27 PM
 */

/*
 * Regex Picker
 *
 * Shows a list of regex'es and lets the user pick one of them
 * to test....
 *
 *
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;

public class RegexPicker    extends     AppCompatActivity implements  FragmentCustomDialogYesNo.getDataFromFragment, EditButtonInsideRegexListWasPressed {

    // Toolbar
    private Toolbar toolbar;

    // UI
    private TextView showSorting;
    private SearchView searchView;

    // Debug
    private String tag;

    // Custom List adapter for our list of regexe's
    private RegexListAdapter regexListAdapter;

    // Async task, filling the list of regexe's...
    private RegexListFiller filler;

    // Shared preferences
    SharedPreferences sharedPreferences;

    // Sorting
    // These strings must contain the aprobiate field in the database. They are
    // used by the sql statement in 'RegexListFiller'
    private String orderBy;
    private String searchQuery;
    private String sortingOrder;

    /**
     * On Create
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regex_picker);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // UI
        showSorting=(TextView)findViewById(R.id.info_text);

        // Debug
        tag=getClass().getSimpleName();

        // Create custom list adapter
        ArrayList<ListEntryRegexList> entry = new ArrayList<>();
        regexListAdapter = new RegexListAdapter(this, entry);      // Custom list adapter
        final ListView listView = (ListView) findViewById(R.id.regex_list);
        listView.setAdapter(regexListAdapter);

        // Fill list
        sortingOrder="DESC";

        // Get saved instance state
        searchQuery="";     // Empty shows=> show all DB entry's
        orderBy="date";
    }

    /**
     * On Resume
     *
     */

    @Override
    public void onResume()
    {
        super.onResume();

        // @rem:Get shared preferences@@
        // @rem:Should be done in 'onResume'@@
        // @rem:Reason, this way this is executed when the activity was left and restarted@@
        // @rem:In 'onCreate()' it would only executed after the activity was destroyed by the@@
        // @rem:system and then restarted@@
        // @rem:Semms to be best practice to do it this way.@@
        // Shared preferences
        restoreFromSharedPreferences();

        // Update list
        updateRegexList(regexListAdapter,orderBy,sortingOrder);

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
        getMenuInflater().inflate(R.menu.menu_regex_picker, menu);

        // Get Search field
        // @rem:Get search view located at toolbar@@
        // @rem:Shows how to handle inputs and update a list accordingly@@
        searchView=(SearchView) menu.findItem(R.id.action_search).getActionView();
        
        SearchView.OnQueryTextListener querryTextListener=new SearchView.OnQueryTextListener(){
            public boolean onQueryTextChange(String sq){
                Log.v(tag+"-----Query changed",sq);
                searchQuery=sq; // Set global variable
                updateRegexList(regexListAdapter,orderBy,sortingOrder);
                return true;
            }

            //Get search field input......
            public boolean onQueryTextSubmit (String sq) {
                searchQuery=sq;
                Log.v(tag + "-----Query Submitted", searchQuery);
                updateRegexList(regexListAdapter,orderBy,sortingOrder);
                return true;
            }
        };
        searchView.setOnQueryTextListener(querryTextListener);
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

        switch (item.getItemId()) {

            // Change sorting order?
            case R.id.sorting_order:
                if (sortingOrder=="ASC") sortingOrder="DESC";
                else sortingOrder="ASC";
                updateRegexList(regexListAdapter,orderBy,sortingOrder);

                break;

            // Change order by?
            case R.id.order_by_date:
                orderBy="date";
                updateRegexList(regexListAdapter,orderBy,sortingOrder);
                break;

            case R.id.order_by_rating:
            orderBy="rating";
            updateRegexList(regexListAdapter,orderBy,sortingOrder);
            break;

            case R.id.export_csv:
                Intent in = new Intent(this,ActivityExportDBAsCsv.class);
                startActivity(in);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * If Back button was pressed
     *
     * Override "Back Button Pressed". Shows how to check if this button was pressed
     *
     * If user leaves this activitys, all settings (search querry, sorting order....)
     * will be saved to Android's shared preferences. They will be restored when
     * this activity is restarted.
     */

    @Override
    public void onBackPressed()
    {
        saveSettings();
        finish();
    }


    /**
     * Saves instance state
     *
     * Is called when the user leaves this activity or the screen orientation
     * is changed.
     *
     * In this case 'onPause' or 'onStop' is called as well.
     *
     */

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        saveSettings();
    }

    /**
     * Save current settings in shared preferences
     */

    public void saveSettings()
    {
        sharedPreferences=getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("searchQuerry",searchQuery);
        editor.putString("sortingOrder",sortingOrder);
        editor.putString("orderBy",orderBy);
        Log.v(tag,"SEARCH"+searchQuery);
        editor.commit();
    }

    /**
     * Restore from shared preferences
     *
     */
    public void restoreFromSharedPreferences()
    {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        searchQuery=sharedPreferences.getString("searchQuerry",searchQuery);
        sortingOrder=sharedPreferences.getString("sortingOrder",sortingOrder);
        orderBy=sharedPreferences.getString("orderBy",orderBy);
    }

    /**
     * Update list of regexe's
     *
     */

   public void updateRegexList(RegexListAdapter regexListAdapter, String orderBy, String sortingOrder){
        Log.v(tag," Updating list");
        if (filler!=null) filler.cancel(true);
        filler=new RegexListFiller(regexListAdapter,getApplicationContext(),orderBy,searchQuery,sortingOrder);
        filler.execute();
        updateToolbar(orderBy,sortingOrder);

        return;
    }

    /**
     * Update toolbar subtitle
     *
     *
     */

    private void updateToolbar (String sortBy,String sortingOrder){

        StringBuffer infoText=new StringBuffer();

        if(!searchQuery.isEmpty()) infoText.append("Alle Einträge die '"+searchQuery+"' enthalten.");
        else infoText.append("Alle Einträge.");

        if(sortingOrder.equals("ASC")) infoText.append("Aufsteigend sortiert nach ");
        else infoText.append("Absteigend sortiert nach ");

        if(sortBy.equals("date")) infoText.append("Datum");
        if (sortBy.equals("rating"))infoText.append("Bewertung");

        showSorting.setText(infoText.toString());

        return;
    }

    /**
     * If 'Edit Button' inside list view was pressed...
     */

    @Override
    public void editButtonInsideRegexListWasPressed()
    {
        Log.v(tag,"Edit Button was pressed. Saving state......");
        saveSettings();
    }

    /**
     * Get input from yes/ no fragment
     *
     * @see FragmentCustomDialogYesNo
     */

    @Override
    public void getDialogInput(int reqCode,int key1,String text,String buttonPressed)
    {
        Log.v("RegexPicker button:",buttonPressed+" KEY1:"+key1);

        switch (reqCode) {

            case RegexListAdapter.DELETE_REGEX:
                if (buttonPressed.equals(FragmentCustomDialogYesNo.BUTTON_OK_PRESSED)) {
                    try {
                        MainActivity.conn.createStatement().executeUpdate("delete from regex where key1=" + key1);
                        updateRegexList(regexListAdapter,orderBy,sortingOrder);
                    } catch (SQLException e) {}
                }
            break;
        }
    }
}
