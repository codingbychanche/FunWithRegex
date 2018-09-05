package berthold.funwithregex;

/**
 * About
 *
 *
 *
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Show database info
        // You can run all sorts of statistics here and display them

        TextView dataBaseInfoO=(TextView)findViewById(R.id.dataBaseInfo);
        dataBaseInfoO.setText("Regexe insgesammt gespeichert:"+DB.getNumberOfRows("regex",MainActivity.conn)+"\n");

    }
}
