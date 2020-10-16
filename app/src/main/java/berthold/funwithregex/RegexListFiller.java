package berthold.funwithregex;

/*
 * RegexListFiller.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 10/14/18 8:36 PM
 */

/*
 * This fills the array list containing all the data...
 */

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegexListFiller extends AsyncTask<String, ListEntryRegexList, String> {

    private String tag;

    private Context c;

    private RegexListAdapter listAdapter;

    private String orderBy;
    private String searchQuery;
    private String sortingOrder;

    private int cg; // Number of found columns

    /**
     * Constructor
     * <p>
     * Creates a new filler object
     */

    RegexListFiller(RegexListAdapter listAdapter, Context c, String orderBy, String searchQuery, String sortingOrder) {
        this.listAdapter = listAdapter;
        this.c = c;
        this.orderBy = orderBy;
        this.searchQuery = searchQuery;
        this.sortingOrder = sortingOrder;
    }

    /**
     * Get database contents and put them into the file list
     */

    @Override
    protected void onPreExecute() {

        listAdapter.clear();
    }

    /**
     * Does all the work in the background
     * Rule! => Never change view elements of the UI- thread from here! Do it in 'onPublish'!
     */

    @Override
    protected String doInBackground(String... params) {

        // Debug
        tag = RegexListFiller.class.getSimpleName();

        // DB
        try {
            PreparedStatement selectPreparedStatement = null;
            Log.v(tag + " Sorting:", sortingOrder + " Search:" + searchQuery);
            selectPreparedStatement = MainActivity.conn.prepareStatement("select key1,regexstring,description,date,rating from regex where description like '%" + searchQuery + "%' order by " + orderBy + " " + sortingOrder);
            ResultSet rs = selectPreparedStatement.executeQuery();

            while (rs.next()) {

                // This is important!
                // If you miss to do this here, the class which created
                // this object has no way to end the async task started!
                // => This means, no matter how often you call task.cancel(true)
                // the async task will not stop! You have to take
                // care here to react and run the code that cancels!
                if (isCancelled()) break;

                cg = rs.getMetaData().getColumnCount();

                // Insert date from db into list view's data model
                int key1 = 0;
                String regexString = "-";
                String comment = "-";
                String date = "-";
                int rating = 0;

                if (cg >= 1) key1 = rs.getInt(1);
                if (cg >= 2) regexString = rs.getString(2);
                if (cg >= 3) comment = rs.getString(3);
                if (cg >= 4) date = rs.getString(4);

                String niceDate;
                if (date != null)
                    niceDate = FormatTimeStamp.german(date, FormatTimeStamp.WITH_TIME);
                else
                    niceDate="-";

                if (cg >= 5) {
                    rating = rs.getInt(5);
                    Log.v(tag, "RATING:" + rating);
                }

                // If search querry was passed, mark matching part of comment....
                Spannable markedCommend = new SpannableString(comment);
                if (!searchQuery.isEmpty()) {
                    int startIndex = comment.indexOf(searchQuery);
                    markedCommend.setSpan(new BackgroundColorSpan(Color.BLUE), startIndex, startIndex + searchQuery.length(), 0);
                    markedCommend.setSpan(new RelativeSizeSpan(2), startIndex, startIndex + searchQuery.length(), 0);
                }
                // Create list entry
                ListEntryRegexList e = new ListEntryRegexList(key1, regexString, markedCommend, niceDate, rating);
                publishProgress(e);
            }
            if (cg < 1) {

                // Nothing found.....
                ListEntryRegexList e = new ListEntryRegexList(0, "Nichts gefunden", new SpannableString("-"), "-", 0);
                publishProgress(e);
            }
        } catch (SQLException ee) {
            Log.i(tag, "-----ERROR:" + ee.toString());
        }
        return "Done";
    }


    /**
     * Update UI- thread
     * <p>
     * This runs on the UI thread. Not handler's and 'post'
     * needed here
     *
     * @param e File list entry
     */

    @Override
    protected void onProgressUpdate(ListEntryRegexList... e) {
        super.onProgressUpdate();
        listAdapter.add(e[0]);
        listAdapter.notifyDataSetChanged();
    }

    /**
     * All done..
     *
     * @param result
     */

    @Override
    protected void onPostExecute(String result) {
    }
}
