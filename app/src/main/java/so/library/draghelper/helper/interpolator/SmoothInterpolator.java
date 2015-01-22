package so.library.draghelper.helper.interpolator;

import android.view.animation.LinearInterpolator;

/**
 * Created by minkyu on 2015. 1. 20..
 */
public class SmoothInterpolator extends LinearInterpolator {
    @Override
    public float getInterpolation(float input){
        return (float) Math.pow(input - 1, 5) + 1;
    }
}
