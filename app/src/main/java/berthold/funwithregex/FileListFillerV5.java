package berthold.funwithregex;

/**
 * Directory filler V5
 *
 * This is variation of 'FileListFillerV4'
 *
 * Motivation: First load and show file names, then add pictures and show them.
 *
 * The file names are loaded and shown faster. After that the progress of loading
 * the picture files - which can be slow - is started and while pictures are
 * added the user can already browse the files....
 *
 *  THIS VERSION WILL BE MAINTAINED! OTHER VERSIONS WILL NOT!!!!!
 *
 *  @author  Berthold Fritz 2017
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.File;
import java.util.Date;

public class FileListFillerV5 extends AsyncTask<String,FileListOneEntry,String> {

    public Bitmap fileSym;
    public Bitmap file;
    public FileListAdapter dir;
    public File[] fileObjects;
    public boolean readable;
    public String tag;

    private int i;

    //private Bitmap fileSym;
    private int thumbHeight;
    private int thumbWidth;

    public String search;
    public Context c;
    private ProgressBar p;
    private static final int JUST_FILENAMES=1;
    private static final int JUST_PICTURES=2;
    private int state;

    private ListView myListView;
    private int firstVissibleitem,lastVissibleItem;
    private Handler h;

    /**
     * Constructor
     *
     * Creates a new filler object
     */

    FileListFillerV5(FileListAdapter dir, File[] fileObjects,int firstVissibleitem,int lastVissibleItem,Context c, ProgressBar p) {
        this.c = c;
        this.dir = dir;
        this.fileObjects = fileObjects;
        this.p=p;
        this.firstVissibleitem=firstVissibleitem;
        this.lastVissibleItem=lastVissibleItem;
    }

    /**
     * Get directory contents and put them into the file list
     */

    @Override
    protected void onPreExecute(){
        p.setVisibility(View.VISIBLE);
        p.setMax(fileObjects.length-1);
    }

    /**
     *  Does all the work in the background
     *  Rule! => Never change view elements of the UI- thread from here! Do it in 'onPublish'!
     */

    @Override
    protected String doInBackground(String ... params){

        // Debug
        tag=FileListFillerV5.class.getSimpleName();

        Log.v (tag,"Getting file names and adding them to our list....");
        state=JUST_FILENAMES;
            for (i = firstVissibleitem; i <= lastVissibleItem - 1; i++) {

                if (isCancelled()) break;

                // Create row and add to custom list
                // Set file symbol accordingly
                // This is the default for files:
                fileSym = BitmapFactory.decodeResource(c.getResources(), R.drawable.document);

                // Check if file is a picture...
                if (isPictureFile.check(fileObjects[i].getName()))
                      fileSym = BitmapFactory.decodeResource(c.getResources(), R.drawable.camera);

                // Check if it is a directory....
                if (fileObjects[i].isDirectory())
                    fileSym = BitmapFactory.decodeResource(c.getResources(), R.drawable.openfolder);

                // Check if file or folder is readable
                if (!fileObjects[i].canRead() || !fileObjects[i].exists())
                    readable = false;
                else
                    readable = true;

                // Get File's last modificaton date
                String d=new Date(new File (fileObjects[i].getAbsolutePath()).lastModified()).toString();

                // Add file or folder name to list
                FileListOneEntry e = new FileListOneEntry(FileListOneEntry.IS_ACTIVE, fileSym, fileObjects[i].getName(), fileObjects[i].getAbsolutePath(),readable,d);
                publishProgress(e);
            }

        // Wait a few seconds
        // If I didn't the list was not build in the right order.....
        try{
            Thread.sleep(500);
        }catch (InterruptedException e){}

        Log.v (tag," Adding pictures.....");
        state=JUST_PICTURES;

        for (i=firstVissibleitem;i<=lastVissibleItem-1;i++){

            // This is important!
            // If you miss to do this here, the class which created
            // this object has no way to end the async task started!
            // => This means, no matter how many task.cancel(true)
            // you call, the async task would never stop! You have to take
            // care here to react and run the code that cancels!
            if (isCancelled()) break;

            // If not canceled, go on....
            FileListOneEntry e=dir.getItem(i);

            BitmapFactory.Options metaData = new BitmapFactory.Options();
            metaData.inJustDecodeBounds = false;
            metaData.inSampleSize = 1;

            file=BitmapFactory.decodeFile(fileObjects[i].getAbsolutePath());
            if (file!=null) { // Only if file contains image data

                // Uncommend whichever picture format you like....
                //fileSym=MyBitmapTools.toRoundedImage(MyBitmapTools.scaleBitmap(file, 200, 200),null);
                // todo: If I comment this and use "file" and not "fileSymbol" then an unusal error is displayed
                fileSym = MyBitmapTools.toRectangle(MyBitmapTools.scaleBitmap(file, 200, 200,e.fileName));

                // Add bitmap to list view and publish.....
                e.fileSymbol=fileSym;

                publishProgress(e);
            }
        }
        return "Done";
    }

    /**
     * Update UI- thread
     *
     * This runs on the UI thread. Not handler's and 'post'
     * needed here
     *
     * @param e     File list entry
     */

    @Override
    protected void onProgressUpdate (FileListOneEntry ... e){
        if (state==JUST_FILENAMES) {
            dir.add(e[0]);
            p.setProgress(i);
            Log.v (tag," Filname added.....");
        }
        if (state==JUST_PICTURES){
            dir.notifyDataSetChanged();
            p.setProgress(i);
        }
    }

    /**
     * All done..
     *
     * @param result
     */

    @Override
    protected void onPostExecute (String result){
        p.setVisibility(View.GONE);
        super.onPostExecute(result);
    }
}
