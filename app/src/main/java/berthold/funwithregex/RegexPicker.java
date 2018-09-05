package berthold.funwithregex;

/**
 * Regex Picker
 *
 * Shows a list of regex'es and lets the user pick one of them
 * to test....
 *
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class RegexPicker extends AppCompatActivity {

    // Toolbar
    private Toolbar toolbar;

    // UI
    private TextView showSorting;
    // Debug
    private String tag;

    // Custom List adapter for our list of regexe's
    private AdapterRegexList regexListAdapter;

    // Async task, filling the list of regexe's...
    private RegexListFiller filler;

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
        regexListAdapter = new AdapterRegexList(this, entry);      // Custom list adapter
        final ListView listView = (ListView) findViewById(R.id.regex_list);
        listView.setAdapter(regexListAdapter);

        // Fill list
        sortingOrder="DESC";

        // Get saved instance state
        searchQuery="";     // Empty shows=> show all DB entry's
        if (savedInstanceState != null) searchQuery=savedInstanceState.getString("searchTerm");

        orderBy="date";
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
        SearchView searchView=(SearchView) menu.findItem(R.id.action_search).getActionView();

        SearchView.OnQueryTextListener querryTextListener=new SearchView.OnQueryTextListener(){
            public boolean onQueryTextChange(String sq){
                Log.v(tag+"-----Query changed",sq);
                searchQuery=sq; // Set global variable
                updateRegexList(regexListAdapter,orderBy,sortingOrder);
                return true;
            }

            //Get search field input......
            public boolean onQueryTextSubmit (String searchQuery) {
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
        }
        return super.onOptionsItemSelected(item);
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
        outState.putString("searchTerm",searchQuery);
    }

    /**
     * Update list of regexe's
     *
     */

    private void updateRegexList(AdapterRegexList regexListAdapter,String orderBy,String sortingOrder){
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
}
