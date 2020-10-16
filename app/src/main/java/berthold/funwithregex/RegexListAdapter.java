package berthold.funwithregex;

/*
 * RegexListAdapter.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 12/5/18 7:24 AM
 */

/**
 * Adapter
 *
 * This code creates each row of our list, each time a new entry
 * is made.
 *
 * This uses the view holder pattern to speed things up.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;

public class RegexListAdapter extends ArrayAdapter <ListEntryRegexList> {

    ArrayList <ListEntryRegexList> listEntry;
    Context context;

    // Constants
    public static final int DELETE_REGEX=1;

    RegexListAdapter ra;

    /**
     * View holder
     *
     * @rem:This is an example on how the 'view holder'- pattern works@@
     * @rem:Before inflating list rows layout, we check if it needs to be inflated or@@
     * @rem:if we just need to update the data (from the data model)@@
     * @rem:According to the official documaentation this should speed up list view's@@
     */

    private class Holder{
        int key1;
        TextView theRegex;
        TextView description;
        ImageButton deleteEntry;
        ImageButton editEntry;
        TextView date;
        RatingBar rating;
    }

    /**
     * Constructor
     */

    public RegexListAdapter(Context context, ArrayList <ListEntryRegexList> listEntry) {
        super (context,0, listEntry);
        this.listEntry=listEntry;
        this.context=context;
    }

    /*
     * Inflate layout for one row of the list
     */

    @Override
    public  View getView (final int position, View convertView, final ViewGroup parent) {

        final Holder holder;

        // If convertView is null, it was not inflated. Do it here...
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.regex_list, parent, false);

            holder = new Holder();
            holder.theRegex = (TextView) convertView.findViewById(R.id.the_regex);
            holder.description = (TextView) convertView.findViewById(R.id.description);
            holder.deleteEntry=(ImageButton) convertView.findViewById(R.id.delete_this_entry);
            holder.editEntry=(ImageButton)convertView.findViewById(R.id.edit_this_entry);
            holder.date=(TextView)convertView.findViewById(R.id.date);
            holder.rating=(RatingBar)convertView.findViewById(R.id.rating);

            convertView.setTag(holder);

            Log.v("---","Holder gesetzt");

            // ConvertView was already inflated, get holder from tag
        } else {
            holder = (Holder) convertView.getTag();
            Log.v("---","Holder geohlt");
        }

        // Initialize
        final ListEntryRegexList item = getItem(position);
        holder.key1=item.key1;
        holder.theRegex.setText(item.theRegexString);
        holder.description.setText(item.description);
        holder.date.setText(item.date);
        holder.rating.setRating(item.rating);

        // Buttons in each row
        // Delete this entry????
        holder.deleteEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FragmentManager fm = ((FragmentActivity)getContext()).getSupportFragmentManager();
                    FragmentCustomDialogYesNo fragmentDeleteRegex =
                            FragmentCustomDialogYesNo.newInstance(DELETE_REGEX,
                                    FragmentCustomDialogYesNo.SHOW_AS_YES_NO_DIALOG,holder.key1,
                                    null, (context).getResources().getString(R.string.delete_regex_dialog),
                                    (context).getResources().getString(R.string.yes_button),
                                    (context).getResources().getString(R.string.no_button));

                    System.out.println("Key1 just picked to delete:"+holder.key1);

                    fragmentDeleteRegex.show(fm, "fragment_dialog");

                } catch (Exception e){Log.v("Fragment",""+e.toString());}
            }
        });

        // Edit this entry?
        holder.editEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(view.getContext(), InsertRegexInDB.class);
                in.putExtra("myTaskIs",InsertRegexInDB.UPDATE_ENTRY);
                in.putExtra("key1",holder.key1);
                in.putExtra("regexString",holder.theRegex.getText().toString());
                context.startActivity(in);

                // @rem:Shows how a reference to an existing class can be constructed (interface pattern)@@
                ((RegexPicker)getContext()).editButtonInsideRegexListWasPressed();
                ((Activity)getContext()).finish();
            }
        });

        // Set entry on listener
        convertView.setOnClickListener(new View.OnClickListener() {

            // If list item was clicked....
            @Override
            public void onClick(View v) {
                Log.v ("----","Item's key1 Value:"+holder.key1+"  "+holder.theRegex.getText().toString());
                Intent i=new Intent(v.getContext(),MainActivity.class);
                i.putExtra("theRegex",holder.theRegex.getText().toString());
                i.putExtra("key1",holder.key1);
                ((Activity)getContext()).setResult(Activity.RESULT_OK,i);
                ((Activity)getContext()).finish();
            }
        });
        return convertView;
    }
}
