package berthold.funwithregex;

/**
 * Load Text
 *
 *  @author  Berthold Fritz 2017
 */

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoadText extends AsyncTask<String,StringBuffer,String> {

    private int i;
    private String tag;

    private Context c;
    private ProgressBar p;
    private TextView text;
    private TextView message;
    private String path;
    private  int linesRead=0;


    /**
     * Constructor
     *
     * Creates a new filler object
     */

    LoadText (TextView text, TextView message, ProgressBar p, String path){
        this.text=text;
        this.message=message;
        this.p=p;
        this.path=path;
    }

    /**
     * Load text file
     */

    @Override
    protected void onPreExecute(){
        //p.setVisibility(View.VISIBLE);
        text.setText(" ");
    }

    /**
     *  Does all the work in the background
     *  Rule! => Never change view elements of the UI- thread from here! Do it in 'onPublish'!
     */

    @Override
    protected String doInBackground(String ... params){

        // Debug
        tag=LoadText.class.getSimpleName();

        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            StringBuffer loaded=new StringBuffer();


            while((line=br.readLine())!=null){

                // This is important!
                // If you miss to do this here, the class which created
                // this object has no way to end the async task started!
                // => This means, no matter how often you call task.cancel(true)
                // the async task will not stop! You have to take
                // care here to react and run the code that cancels!
                if (isCancelled()) break;


                loaded.append(line);
                loaded.append("\n");
                linesRead++;

                publishProgress(loaded);

                if (linesRead>=50) break;
                // Wait a few seconds
                // If I didn't the list was not build in the right order.....
                try{
                    Thread.sleep(5);
                }catch (InterruptedException e){}
            }

        }catch (Exception e){
            Log.v(tag,"Fehler "+ e);
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
        text.append(s[0]);
        message.setText(linesRead+" Zeile gelesen");
    }

    /**
     * All done..
     *
     * @param result
     */

    @Override
    protected void onPostExecute (String result){
        //p.setVisibility(View.GONE);

    }
}
