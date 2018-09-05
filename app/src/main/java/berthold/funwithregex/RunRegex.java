package berthold.funwithregex;

/**
 * Run Regex
 *
 * Executes the regex
 *
 * @author  Berthold Fritz 2017
 */

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RunRegex extends AsyncTask<String,Integer,RunRegexResult> {

    private String tag;

    private ProgressBar p;
    private EditText theRegex;
    private TextView testText;
    private TextView justResult;
    private TextView message;
    private View lineBelowMessage;

    String          textToTest;         // Test to test with our regex...
    String          regex;
    Spannable       markedText;         // Market part of the tested text
    StringBuffer    justTheResult=new StringBuffer();      // Contains the part of the test text, matching the regex pattern

    RunRegexResult result=new RunRegexResult(); // The result...

    int found = 0;

    // These are evaluated in 'PostExecute' to decide which message
    // has to be displayed. Error is an error, noRegexString means that nothing was done
    // because there was no input....

    boolean error;          // There was an error....
    boolean noRegexString;  // No regex- string was given....

    /**
     * Constructor
     */

    RunRegex(EditText theRegex, TextView testText, TextView justResult,TextView message,View lineBelowMessage, ProgressBar p){
        this.theRegex=theRegex;
        this.testText=testText;
        this.justResult=justResult;
        this.message=message;
        this.lineBelowMessage=lineBelowMessage;
        this.p=p;
    }

    /**
     * Run regex
     */

    @Override
    protected void onPreExecute(){

        // Get search pattern and text to test
        regex=theRegex.getText().toString();
        textToTest=testText.getText().toString();

        message.setVisibility(View.VISIBLE);
        message.bringToFront();
        lineBelowMessage.setVisibility(View.VISIBLE);
        lineBelowMessage.bringToFront();

        //p.setVisibility(View.VISIBLE);
        //p.bringToFront();
    }

    /**
     *  Does all the work in the background
     *  Rule! => Never change view elements of the UI- thread from here! Do it in 'onPublish'!
     */

    @Override
    protected RunRegexResult doInBackground(String ... params){

        // Debug
        tag=RunRegex.class.getSimpleName();
        error=false;

        try {
                markedText=new SpannableString(textToTest);


                if (!regex.isEmpty()){
                    Pattern mPattern;

                    try {
                        mPattern = Pattern.compile(regex);
                        Log.v("----","Pattern compiled");
                        Matcher matcher = mPattern.matcher(textToTest);

                        ArrayList matching = new ArrayList();
                        justTheResult.delete(0,justTheResult.length());

                        while (matcher.find()) {

                            // This is important!
                            // If you miss to do this here, the class which created
                            // this object has no way to end the async task started!
                            // => This means, no matter how often you call task.cancel(true)
                            // the async task will not stop! You have to take
                            // care here to react and run the code that cancels!
                            if (isCancelled()) break;

                            // Parse....
                            matching.add(matcher.group(0));
                            markedText.setSpan(new BackgroundColorSpan(Color.YELLOW), matcher.start(), matcher.end(), 0);
                            publishProgress(found);
                            found++;
                        }
                        if (found == 0){
                            //messageText.append("Nichts gefunden...");
                            //justTheResult.append("Nichts gefunden....");
                        } else {
                            //messageText.append(found + " Treffer.");

                            // Get the matching part of the text....
                            for (int i = 0; i <= matching.size() - 1; i++)
                                justTheResult.append(matching.get(i) + "\n");

                            result.markedText=markedText;
                            result.justTheResult=justTheResult;
                        }
                    } catch (Exception e) {
                        error=true;
                        markedText=new SpannableString("Fehler:"+e.toString());
                        result.markedText=markedText;
                        result.justTheResult.append("-");
                        return result;
                    }
                } else {
                    noRegexString=true;
                    markedText=new SpannableString("Keine regex eingegeben, kein Spass mit Regex");
                    result.markedText=markedText;
                    return result;
                }

        }catch (Exception e){
            Log.v(tag,"Fehler "+ e);
        }

        return result;
    }


    /**
     * Update UI- thread
     *
     * This runs on the UI thread. Not handler's and 'post'
     * needed here
     *
     * @param treffer
     */

    @Override
    protected void onProgressUpdate (Integer ... treffer){
        super.onProgressUpdate();
        message.setText("Treffer:"+treffer[0]);
    }

    /**
     * All done..
     *
     * @param result
     */

    @Override
    protected void onPostExecute (RunRegexResult result){
        //p.setVisibility(View.GONE);
        message.setVisibility(View.VISIBLE);
        lineBelowMessage.setVisibility(View.VISIBLE);

        // If something was found, display result, if not, tell so...
        if (found == 0){
            message.setText("Nichts gefunden...");
            // Remove highlights from testText and last result
            testText.setText(testText.getText().toString());
            justResult.setText(" ");
        }else{
            message.setText(found + " Treffer.");
            testText.setText(result.markedText);
            justResult.setText(justTheResult);
        }

        // If error or no regex string was given, inform the user....
        if (error || noRegexString) message.setText(markedText);
    }
}
