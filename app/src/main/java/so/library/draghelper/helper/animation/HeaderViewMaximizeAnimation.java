package so.library.draghelper.helper.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by risedrag on 15. 1. 19..
 */
public class HeaderViewMaximizeAnimation extends Animation {
	final int startWidth;
	final int targetWidth;

	final float startTranslationX;
	final float startTranslationY;

	View view;

	public HeaderViewMaximizeAnimation(View view, int targetWidth) {
		this.view = view;
		this.targetWidth = targetWidth;

		startWidth = view.getWidth();
		startTranslationX = view.getTranslationX();
		startTranslationY = view.getTranslationY();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		int newWidth = (int) (startWidth + (targetWidth - startWidth) * interpolatedTime);

		//targetTranslation (0,0)

		float targetTranslationX = startTranslationX - (startTranslationX * interpolatedTime);
		float targetTranslationY = startTranslationY - (startTranslationY * interpolatedTime);

		view.setTranslationX(targetTranslationX);
		view.setTranslationY(targetTranslationY);
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
