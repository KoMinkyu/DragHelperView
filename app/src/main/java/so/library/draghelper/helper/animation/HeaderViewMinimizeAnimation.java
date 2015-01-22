package so.library.draghelper.helper.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by risedrag on 15. 1. 16..
 */
public class HeaderViewMinimizeAnimation extends Animation {
	final int startWidth;
	final int targetWidth;
	final float targetTranslationX;
	final float targetTranslationY;

	View view;


	public HeaderViewMinimizeAnimation(View view, int targetWidth, float targetTranslationX, float targetTranslationY) {
		this.view = view;
		this.targetWidth = targetWidth;
		this.targetTranslationX = targetTranslationX;
		this.targetTranslationY = targetTranslationY;

		startWidth = view.getWidth();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		int newWidth = (int) (startWidth + (targetWidth - startWidth) * interpolatedTime);

		view.setTranslationX(targetTranslationX * interpolatedTime);
		view.setTranslationY(targetTranslationY * interpolatedTime);
		view.getLayoutParams().width = newWidth;
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
