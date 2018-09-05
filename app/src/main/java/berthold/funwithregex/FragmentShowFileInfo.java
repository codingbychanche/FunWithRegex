package berthold.funwithregex;

/**
 * Show file info
 *
 * Show's preview of the selected file's thumpnail
 *
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
import android.widget.ImageView;
import android.widget.ProgressBar;

/**
 * Created by Berthold on 1/13/18.
 */

public class FragmentShowFileInfo extends DialogFragment {

    ImageView   screenShoot;
    Bitmap      pic;
    String      filePath;
    ProgressBar progress;
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
        void onFinishEditDialog(String text);
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

        screenShoot=(ImageView) view.findViewById(R.id.screen_shot);
        progress=(ProgressBar) view.findViewById(R.id.progress);
        final Handler h=new Handler();

        //filePath=getArguments().getString("filePath");
        filePath=getArguments().getString("filePath");
        System.out.println("------Fragment Path:"+filePath);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                pic=null;
                do{
                    pic=BitmapFactory.decodeFile(filePath);
                    //pic=BitmapFactory.decodeResource(getResources(),R.drawable.camera,null);
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            progress.setVisibility(View.VISIBLE);
                        }
                    });

                    // Wait a vew millisec's to enable the main UI thread
                    // to react.
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {}

                } while (pic==null);

                // All done...
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        screenShoot.setImageBitmap(pic);
                        progress.setVisibility(View.GONE);
                        frag.getView().setBackgroundColor(MyBitmapTools.getDominantColorAtBottom(pic));


                    }
                });
            }
        });
        t.start();
    }
}
