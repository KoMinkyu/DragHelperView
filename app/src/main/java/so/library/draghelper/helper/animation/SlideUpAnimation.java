package so.library.draghelper.helper.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by minkyu on 2015. 1. 22..
 */
public class SlideUpAnimation extends Animation {
    final float startTranslationY;

    View view;

    public SlideUpAnimation(View view){
        this.view = view;

        this.startTranslationY = view.getTranslationY();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float targetTranslationY = startTranslationY - (startTranslationY * interpolatedTime);

        view.setTranslationY(targetTranslationY);
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
