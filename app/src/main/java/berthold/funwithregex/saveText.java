package berthold.funwithregex;

/**
 * Save Text
 *
 *  @author  Berthold Fritz 2017
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;

public class saveText extends AsyncTask<String,StringBuffer,String> {

    private String tag;

    private Context c;
    private ProgressBar p;
    private String text;
    private String path;

    private String finalMessage="";

    /**
     * Constructor
     *
     * Creates a new filler object
     */

    saveText(String text, Context c,ProgressBar p, String path){
        this.text=text;
        this.c=c;
        this.p=p;
        this.path=path;
    }

    /**
     * Load text file
     */

    @Override
    protected void onPreExecute(){
        p.setVisibility(View.VISIBLE);
    }

    /**
     *  Does all the work in the background
     *  Rule! => Never change view elements of the UI- thread from here! Do it in 'onPublish'!
     */

    @Override
    protected String doInBackground(String ... params){

        // Debug
        tag=saveText.class.getSimpleName();

        Log.v (tag,text);

        finalMessage="Gespeichert...."; // Will be overwritte with error message if something gone wrong....

        try {
            FileOutputStream fo=c.openFileOutput(path,Context.MODE_PRIVATE);
            fo.write(text.getBytes());
            fo.close();

            Log.v(tag," Saving to:"+path);

            // Wait a few seconds
            // If I didn't the list was not build in the right order.....
            try{
                Thread.sleep(150);
            }catch (InterruptedException e){}

        }catch (Exception e){
            Log.v(tag,"Fehler "+ e);
            finalMessage="Fehler, konnte nach "+path+" nicht schreiben. Grund:"+e.toString();
        }

        return "Done";
    }


    /**
     * Update UI- thread
     *
     * This runs on the UI thread. Not handler's and 'post'
     * needed here
     *
     * @param s     Line by line of the file loading....
     */

    @Override
    protected void onProgressUpdate (StringBuffer ... s){
        super.onProgressUpdate();
    }

    /**
     * All done..
     *
     * @param result
     */

    @Override
    protected void onPostExecute (String result){
        p.setVisibility(View.GONE);
        Toast.makeText(c,finalMessage, Toast.LENGTH_LONG).show();

    }
}
