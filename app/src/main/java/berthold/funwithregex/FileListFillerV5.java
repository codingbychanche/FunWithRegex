/*
 * First load and show file names, then add pictures and show them.
 *
 * The file names are loaded and shown faster. After that the progress of loading
 * the picture files - which can be slow - is started and while pictures are
 * added the user can already browse the files....
 */

package berthold.funwithregex;

/*
 * FileListFillerV5.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 12/18/18 11:32 PM
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.io.File;
import java.util.Date;

public class FileListFillerV5 extends AsyncTask<String,FileListOneEntry, String> {

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

    private Handler h;

    /**
     * Constructor
     *
     * Creates a new filler object
     */

    FileListFillerV5(FileListAdapter dir, File[] fileObjects, Context c, ProgressBar p) {
        this.c = c;
        this.dir = dir;
        this.fileObjects = fileObjects;
        this.p=p;
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
    protected String doInBackground(String... params){

        // Debug
        tag=FileListFillerV5.class.getSimpleName();

        state=JUST_FILENAMES;
        for (i = 0; i <=fileObjects.length-1; i++) {

            if (isCancelled()) break;

            // Create row and add to custom list
            // Set file symbol accordingly
            // This is the default for files:
            fileSym = BitmapFactory.decodeResource(c.getResources(), R.drawable.document);

            if (fileObjects[i]!=null) {

                // Check if file is a picture...
                Log.v(tag, "File Object is:" + i + " contains:" + fileObjects[i]);
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
                String d = new Date(new File(fileObjects[i].getAbsolutePath()).lastModified()).toString();

                // Add file or folder name to list
                FileListOneEntry e = new FileListOneEntry(FileListOneEntry.IS_ACTIVE, fileSym, fileObjects[i].getName(), fileObjects[i].getAbsolutePath(), readable, d);
                publishProgress(e);
            }
        }

        // Wait a few seconds
        // If I didn't the list was not build in the right order.....
        try{
            Thread.sleep(250);
        }catch (InterruptedException e){}

        state=JUST_PICTURES;
        for (i=0;i<=fileObjects.length-2;i++){

            // This is important!
            // If you miss to do this here, the class which created
            // this object has no way to end the async task started!
            // => This means, no matter how many task.cancel(true)
            // you call, the async task would never stop! You have to take
            // care here to react and run the code that cancels!
            if (isCancelled()) break;

            // If not canceled, go on....
            if (fileObjects[i]!=null){

                FileListOneEntry e=dir.getItem(i);

                BitmapFactory.Options metaData = new BitmapFactory.Options();

                // Calc sample size of image according to it's size
                // We do this in order to reduce the images memory footprint
                int sampleSize;
                metaData.inJustDecodeBounds = true;
                file= BitmapFactory.decodeFile(fileObjects[i].getAbsolutePath(),metaData);
                sampleSize=MyBitmapTools.calcSampleSize(metaData.outWidth,metaData.outHeight,200,200);
                metaData.inSampleSize = sampleSize;

                // Now set sample size and get images data from file
                metaData.inJustDecodeBounds=false;
                file= BitmapFactory.decodeFile(fileObjects[i].getAbsolutePath(),metaData);
                if (file!=null) { // Only if file contains image data
                    // Add bitmap to list view and publish.....
                    e.fileSymbol = file;
                    publishProgress(e);
                }
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