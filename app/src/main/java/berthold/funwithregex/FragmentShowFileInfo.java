/*
 * Show file info
 *
 * Show's preview of the selected file's thumbnail
 */

package berthold.funwithregex;

/*
 * FragmentShowFileInfo.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 12/18/18 11:32 PM
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class FragmentShowFileInfo extends DialogFragment {

    // UI
    ImageView screenShoot;
    View colorBelowPic;
    Bitmap pic;
    String filePath;
    ProgressBar progress;

    ImageButton quit;
    ImageButton nextPic;
    ImageButton lastPic;

    static FragmentShowFileInfo frag;

    public FragmentShowFileInfo(){
        // Constructor must be empty....
    }

    // This passes paramenters to the fragment beeing created...
    public static FragmentShowFileInfo newInstance (String filePath){
        frag=new FragmentShowFileInfo();
        Bundle args=new Bundle();
        args.putString("filePath",filePath);
        frag.setArguments(args);

        return frag;
    }

    // Listener Interface
    public interface FragmentEditNameListener{
        void onFinishEditDialog(String BottonPressed);
    }

    // Inflate fragment layout
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_dialog_show_file_info,container);
    }

    // This fills the layout with data
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ui
        screenShoot=(ImageView) view.findViewById(R.id.screen_shot);
        colorBelowPic=(View)view.findViewById(R.id.color_below_pic);
        progress=(ProgressBar) view.findViewById(R.id.progress);
        quit=(ImageButton) view.findViewById(R.id.quit_preview);
        nextPic=(ImageButton)view.findViewById(R.id.next_pic);
        lastPic=(ImageButton)view.findViewById(R.id.last_pic);
        final Handler h=new Handler();

        // Buttons
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               dismiss();
            }
        });

        //filePath=getArguments().getString("filePath");
        filePath=getArguments().getString("filePath");
        System.out.println("------Fragment Path:"+filePath);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                pic=null;
                    do{
                    pic= BitmapFactory.decodeFile(filePath);
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            progress.setVisibility(View.VISIBLE);
                        }
                    });

                    // Wait a vew millisec's to enable the main UI thread
                    // to react.
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {}

                } while (pic==null);

                // All done...
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        screenShoot.setImageBitmap(pic);
                        progress.setVisibility(View.GONE);

                        int color=MyBitmapTools.getDominatColorAtTop(pic);
                        int colorBottom=MyBitmapTools.getDominantColorAtBottom(pic);
                        frag.getView().setBackgroundColor(color);
                        quit.setBackgroundColor(color);
                        screenShoot.setBackgroundColor(color);

                        colorBelowPic.setBackgroundColor(colorBottom);
                        nextPic.setBackgroundColor(colorBottom);
                        lastPic.setBackgroundColor(colorBottom);
                    }
                });
            }
        });
        t.start();
    }
}
