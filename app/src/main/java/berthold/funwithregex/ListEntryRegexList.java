/**
 *  Data model for each row in our list
 *
 *  @author  Berthold Fritz 2017
 */

package berthold.funwithregex;

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
