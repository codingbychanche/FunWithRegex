package berthold.funwithregex;

/*
 * FragmentCustomDialogYesNo.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 9/5/18 5:03 PM
 */

/**
 * Shows a dialog containing an bitmap, editText and two buttons (yes/ no)
 * Bitmap and buttons can be customized.
 */

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class FragmentCustomDialogYesNo extends DialogFragment {

    // Fragments UI components
    TextView    dialogText;
    EditText    dialogTextInput;
    ImageView   screenShoot;
    Button      okButton;
    Button      cancelButton;

    int reqCode;            // Identifies the fragment in callback method of calling activity
    getDataFromFragment gf; // The Interface delivering data from the fragment....

    // Display options for the dialog
    //
    // Standart is: Display no editText and a two buttons = 'SHOW_AS_YES_NO_DIALOG'
    // if you pass this parameter or '0'
    // If you need the editText pass 'SHOW_WITH_EDIT_TEXT'
    // If you need a only a confirm dialog pass 'SHOW_CONFIRM_DIALOG'
    // If you need a confirm dialog + editText pass 'SHOW_WITH_EDIT_TEXT+SHOW_CONFIRM_DIALOG'

    private int options;
    public static final int SHOW_AS_YES_NO_DIALOG=0;  // Show Text and two buttons (confirm/ cancel)
    public static final int SHOW_WITH_EDIT_TEXT=1; // Allow text input
    public static final int SHOW_CONFIRM_DIALOG=2; // Disply only one confirm button

    // Return parameters
    public static final String BUTTON_OK_PRESSED="OK";
    public static final String BUTTON_CANCEL_PRESSED="CANCEL";

    // Empty Constructor!
    public FragmentCustomDialogYesNo(){
        // Constructor must be empty....
    }

    public static FragmentCustomDialogYesNo newInstance (int reqCode,int options,Bitmap icon, String dialogText, String yesText, String noText){
        FragmentCustomDialogYesNo frag=new FragmentCustomDialogYesNo();
        Bundle args=new Bundle();
        args.putInt("reqCode",reqCode);
        args.putInt("options",options);
        args.putByteArray("icon",null);
        args.putString("dialogText",dialogText);
        args.putString("yesText",yesText);
        args.putString("noText",noText);
        frag.setArguments(args);
        return frag;
    }

    // THE INTERFACE
    //
    // This is the interface used to pass data from the
    // this fragment to it's activity
    //
    public interface getDataFromFragment{
        void getDialogInput(int reqCode,String dialogText, String buttonPressed);
    }

    // get interface Object...
    // You may use any method defined in the interface through the
    // object 'gf'

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        gf=(getDataFromFragment) activity;
    }

    // Inflate fragment layout
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_custom_dialog_yes_no,container);
    }

    // This fills the layout with data
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get request code
        reqCode=getArguments().getInt("reqCode");

        // These are the fragments UI components
        // Gets all objects (Buttons, EditText etc..) and set's them on
        // their listeners.....
        dialogText=(TextView) view.findViewById(R.id.dialog_text);
        dialogTextInput=(EditText)view.findViewById(R.id.dialog_text_input);
        screenShoot=(ImageView) view.findViewById(R.id.screen_shot);
        //screenShoot.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.gure));
        okButton=(Button)view.findViewById(R.id.ok_button);
        cancelButton=(Button)view.findViewById(R.id.cancel_button);

        // Set UI- components contents (text etc....)
        // According to passed parameters
        options=SHOW_AS_YES_NO_DIALOG;
        options=getArguments().getInt("options");
        Log.v("Fragement:","Options:"+options);

            // Views are removed depending on 'options'

            // Show editText?
            dialogTextInput.setVisibility(View.GONE);
            if ((options & SHOW_WITH_EDIT_TEXT)==SHOW_WITH_EDIT_TEXT)
                dialogTextInput.setVisibility(View.VISIBLE);

            // Show onlay as confirm dialog? If so, remove cancel button
            if ((options & SHOW_CONFIRM_DIALOG)==SHOW_CONFIRM_DIALOG)
                cancelButton.setVisibility(View.GONE);

        dialogText.setText(getArguments().getString("dialogText"));
        okButton.setText(getArguments().getString("okText"));
        cancelButton.setText(getArguments().getString("noText"));

        // When Ok Button is pressed, finish fragment and return text....
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // When the button is pressed, pass entered text via the interface
                // to the activity which started this fragment.
                //
                // The caling activity must implement this fragments interface!

                gf.getDialogInput(reqCode,dialogTextInput.getText().toString(),BUTTON_OK_PRESSED);
                dismiss();
            }
        });

        // Cancel button was pressed
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gf.getDialogInput(reqCode,"Cancel Button was pressed.....",BUTTON_CANCEL_PRESSED);
                dismiss();
            }
        });
    }
}