/**
 * Adapter class for File- list
 *
 * This code creates each row of our list, each time a new entry
 * is added.
 *
 * @author  Berthold Fritz 2016
 */


package berthold.funwithregex;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class FileListAdapter extends ArrayAdapter <FileListOneEntry> {

    DecimalFormat df=new DecimalFormat("#,###,###");

    /**
     * Constructor
     */
    
    public FileListAdapter(Context context, ArrayList <FileListOneEntry> FileListOneEntry) {
        super (context,0, FileListOneEntry);
    }

    /*
     * Inflate layout for one row of the list
     */

    @Override
    public  View getView (int position, View convertView, ViewGroup parent) {

            final FileListOneEntry item = getItem(position);

            // Check status of entry and inflate matching layout
            // Active item. Inflate layout and fill row with data
            if (item.entryType == FileListOneEntry.IS_ACTIVE) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.file_list_active_entry, parent, false);

                // Set file name
                final TextView tvName = (TextView) convertView.findViewById(R.id.fileNname);
                tvName.setText(item.fileName);

                // Set file symbol and set ImageButton on click listener
                final ImageView fs = (ImageView) convertView.findViewById(R.id.fileSymbol);
                fs.setImageBitmap(item.fileSymbol);
                fs.setTag(position);
                fs.setOnClickListener(new View.OnClickListener() {

                    // Set imageView on click listener
                    @Override
                    public void onClick(View v) {

                        if (isPictureFile.check(item.fileName)) {
                            FragmentManager f = ((AppCompatActivity) getContext()).getSupportFragmentManager();
                            FragmentShowFileInfo fragmentShowFileInfo = FragmentShowFileInfo.newInstance(item.filePath);
                            fragmentShowFileInfo.show(f, "fragment_dialog_show_file_info");
                        }
                    }
                });

                // If folder/ file is !readable, then add lock- sym to file sym...
                if (!item.isReadable) {
                    final ImageView lock = (ImageView) convertView.findViewById(R.id.locksymbol);
                    Bitmap lockSym = BitmapFactory.decodeResource(convertView.getResources(), android.R.drawable.ic_delete);
                    lock.setImageBitmap(lockSym);
                }

                // Set last modified date
                final TextView d = (TextView) convertView.findViewById(R.id.lastmodified);
                d.setText(item.lastModified);

            }

        return convertView;
    }
}
