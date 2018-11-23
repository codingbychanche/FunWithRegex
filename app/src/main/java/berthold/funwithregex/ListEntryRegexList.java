package berthold.funwithregex;

/*
 * ListEntryRegexList.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 3/23/18 9:24 PM
 */

/**
 *  Data model for each row in our list
 */

import android.text.Spannable;
import android.widget.RatingBar;

public class ListEntryRegexList {

    int key1;
    public String theRegexString;
    public Spannable description;
    public String date;
    public int rating;

    /**
     * Constructor, assign properties
     */

    ListEntryRegexList(int key1, String theRegexString, Spannable description, String date, int rating){
        this.key1=key1;
        this.theRegexString=theRegexString;
        this.description=description;
        this.date=date;
        this.rating=rating;
    }

}
