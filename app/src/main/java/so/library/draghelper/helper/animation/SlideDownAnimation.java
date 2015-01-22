package so.library.draghelper.helper.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by minkyu on 2015. 1. 22..
 */
public class SlideDownAnimation extends Animation {
    final float targetTranslationY;

    View view;

    public SlideDownAnimation(View view, float targetTranslationY){
        this.view = view;
        this.targetTranslationY = targetTranslationY;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        view.setTranslationY(targetTranslationY * interpolatedTime);
        view.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}
