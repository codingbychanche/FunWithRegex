/**
 * Adapter
 *
 * This code creates each row of our list, each time a new entry
 * is made.
 *
 * This uses the view holder pattern to speed things up.
 *
 * @author  Berthold Fritz 2018
 */


package berthold.funwithregex;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import java.util.ArrayList;

public class AdapterRegexList extends ArrayAdapter <ListEntryRegexList>{

    ArrayList <ListEntryRegexList> listEntry;
    Context context;

    /**
     * View holder
     */

    private class Holder{
        int key1;
        TextView theRegex;
        TextView description;
        ImageButton deleteEntry;
        TextView date;
        RatingBar rating;
    }

    /**
     * Constructor
     */

    public AdapterRegexList(Context context, ArrayList <ListEntryRegexList> listEntry) {
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
           //listEntry=getItem(position);
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.regex_list, parent, false);
            final View v=convertView;

            holder = new Holder();
            holder.theRegex = (TextView) convertView.findViewById(R.id.the_regex);
            holder.description = (TextView) convertView.findViewById(R.id.description);
            holder.deleteEntry=(ImageButton) convertView.findViewById(R.id.delete_this_entry);
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
        holder.rating.setProgress(item.rating);

        // Buttons in each row
        // Delete this entry????
        holder.deleteEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MainActivity.conn.createStatement().executeUpdate("delete from regex where key1=" + holder.key1);
                    listEntry.remove(position);
                    notifyDataSetChanged();
                } catch (Exception e){}
            }
        });

        // Rating bar changed....
        holder.rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
             Log.v ("---", "key:"+holder.key1+"  Progress:"+ratingBar.getProgress());

                // Insert new rating into db entry
                try {
                    MainActivity.conn.createStatement().executeUpdate ("update regex set rating="+(int)rating+" where key1="+holder.key1);
                    //notifyDataSetChanged();
                } catch (Exception e){
                    Log.v("---"," uuuuups "+e.toString());
                }

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
                ((Activity)getContext()).setResult(Activity.RESULT_OK,i);
                ((Activity)getContext()).finish();
            }
        });
        return convertView;
    }


}
