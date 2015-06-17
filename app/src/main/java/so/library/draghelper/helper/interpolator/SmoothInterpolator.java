package so.library.draghelper.helper.interpolator;

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by minkyu on 2015. 1. 20..
 */
public class SmoothInterpolator extends LinearInterpolator {
//    @Override
//    public float getInterpolation(float input){
//        return (float) Math.pow(input - 1, 5) + 1;
//    }

  @Override
  public float getInterpolation(float t){
    float x;
    if (t<0.5f)
    {
      x = t*2.0f;
      return 0.5f*x*x*x*x*x;
    }
    x = (t-0.5f)*2-1;
    return 0.5f*x*x*x*x*x+1;
  }
}
