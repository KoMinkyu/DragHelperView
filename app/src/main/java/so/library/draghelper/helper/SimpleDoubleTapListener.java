package so.library.draghelper.helper;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by minkyu on 2015. 1. 20..
 */
public class SimpleDoubleTapListener extends GestureDetector.SimpleOnGestureListener{

    Runnable callback;

    public SimpleDoubleTapListener(Runnable callback){
        this.callback = callback;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event){
        callback.run();
        return true;
    }
}
