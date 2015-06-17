package so.library.draghelper.helper.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import so.library.draghelper.helper.SimpleDoubleTapListener;
import so.library.draghelper.helper.animation.HeaderViewScaleDownAnimation;
import so.library.draghelper.helper.animation.HeaderViewScaleUpAnimation;
import so.library.draghelper.helper.animation.SlideDownAnimation;
import so.library.draghelper.helper.animation.SlideUpAnimation;
import so.library.draghelper.helper.data.Offset;
import so.library.draghelper.helper.interpolator.SmoothInterpolator;

/**
 * Created by minkyu on 2015. 1. 20..
 */
public class DragHelperView extends LinearLayout {

    public enum State{Closed, Opened, FullyOpened};

    public static final String TAG = DragHelperView.class.getSimpleName();

    static final int DEFAULT_ANIMATION_DURATION = 600;
    static final float DEFAULT_SCALE_ANIMATION_RATIO = 0.4f;

    private static final int HEADER_VIEW_INDEX = 0;
    private static final int BODY_VIEW_INDEX = 1;

    private State state;
    private State stateBeforeTracking;

    private View headerView;
    private View bodyView;

    private Animation maximizeAnimation;
    private Animation minimizeAnimation;

    private Interpolator animationInterpolator;
    private OvershootInterpolator overshootInterpolator;

    private int layoutHeightFactor = -1;
    private int layoutWidthFactor = -1;

    private int headerViewLayoutHeightFactor = -1;
    private int headerViewLayoutWidthFactor = -1;

    private int animationDuration = DEFAULT_ANIMATION_DURATION;
    private float scaleAnimationRatio = DEFAULT_SCALE_ANIMATION_RATIO;

    private float headerViewScaleFactorX = -1.0f;
    private float headerViewScaleFactorY = -1.0f;

    private float startX;
    private float startY;
    private float oldX;
    private float oldY;

    private int contentOffsetY;

    private Offset headerViewOffset = new Offset();

    private int pagingTouchSlop;
    private int minFlingVelocity;
    private int maxFlingVelocity;

    private GestureDetector dragHelperGestureDetector;
    private VelocityTracker velocityTracker;

    private boolean animating = false;
    private boolean tracking = false;
    private boolean preTracking = false;
    private boolean headerViewDoubleTapOperatorEnabled = false;

    private boolean layoutInitialized = false;
    private boolean animationInitialized = false;
    private boolean useDefaultAnimationInterpolator = true;
    private boolean scaleAnimationEnabled = false;

    private Context context;

    public DragHelperView(Context context) {
        super(context);
        this.context = context;
    }

    public DragHelperView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public DragHelperView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public DragHelperView initialize(View headerView, View bodyView){
        this.removeAllViews();

        this.addView(headerView, HEADER_VIEW_INDEX);
        this.addView(bodyView, BODY_VIEW_INDEX);

        this.headerView = headerView;
        this.bodyView = bodyView;

        state = State.FullyOpened;

        overshootInterpolator = new OvershootInterpolator();
        initializeViewConfigurations();
        return this;
    }

    private void initializeLayoutScaleFactor(){
        this.layoutWidthFactor = getWidth();
        this.layoutHeightFactor = getHeight();

        this.headerViewLayoutHeightFactor = headerView.getHeight();
        this.headerViewLayoutWidthFactor = headerView.getWidth();
    }

    public DragHelperView setAnimationInterpolator(Interpolator animationInterpolator){
        this.animationInterpolator = animationInterpolator;
        this.useDefaultAnimationInterpolator = false;

        return this;
    }

    public DragHelperView setAnimationDuration(int duration){
        this.animationDuration = duration;

        return this;
    }

    public DragHelperView setScaleAnimationEnabled(boolean enabled){
        this.scaleAnimationEnabled = enabled;

        return this;
    }

    public DragHelperView setScaleAnimationRatio(float ratio){
        this.scaleAnimationRatio = ratio;

        return this;
    }

    public DragHelperView setDoubleTapAnimationEnabled(boolean enabled){
        this.headerViewDoubleTapOperatorEnabled = enabled;

        return this;
    }

    /**
     * Check if drag helper view animating.
     *
     * @return true if drag helper view animating.
     */
    public boolean isAnimating(){
        return this.animating;
    }

    /**
     * Check if drag helper view fully opened.
     *
     * @return true if drag helper view fully opened.
     */
    public boolean isFullyOpened(){
        return state == State.FullyOpened;
    }

    /**
     * Check if drag helper view opened.
     *
     * @return true if drag helper view open or fully opened.
     */
    public boolean isOpened(){
        return state == State.Opened;
    }

    public void setState(State newState, boolean animated){
        if(useDefaultAnimationInterpolator)
            this.animationInterpolator = new SmoothInterpolator();

        Interpolator interpolator = state == State.Closed && newState == State.Opened ? overshootInterpolator : animationInterpolator;

        this.state = newState;
        if (newState != State.Closed)
            this.setVisibility(VISIBLE);
        if (!animated){
            setNewOffset(getOffsetForState(newState));
            if(state == State.Closed)
                this.setVisibility(INVISIBLE);
        } else {
            animating = true;
            this.animate(this, getOffsetForState(newState), animationDuration, interpolator, new Runnable() {
                @Override
                public void run() {
                    animating = false;
                    if(state == State.Closed)
                        setVisibility(INVISIBLE);
                }
            });
        }
    }

    private void animate(View view, int translation, int duration, Interpolator interpolator, Runnable endAction){
        ViewPropertyAnimatorCompat animator = ViewCompat.animate(view);
        animator.setDuration(duration).translationY(translation);
        if(endAction != null)
            animator.withEndAction(endAction);
        if(interpolator != null)
            animator.setInterpolator(interpolator);

        animator.start();
    }

    private boolean isTouchInterceptableByChildViews(MotionEvent event){
        return isMotionEventOccurredOnHeaderView(event) && isMotionEventOccurredOnBodyView(event);
    }

    private boolean isMotionEventOccurredOnHeaderView(MotionEvent event){
        Rect viewHitRect = getHeaderHitRect();
        return viewHitRect.contains((int)event.getRawX(), (int)event.getRawY());
    }

    private boolean isMotionEventOccurredOnBodyView(MotionEvent event){
        Rect viewHitRect = new Rect();

        bodyView.getHitRect(viewHitRect);
        return viewHitRect.contains((int)event.getRawX(), (int)event.getRawY());
    }

    private Rect getHeaderHitRect(){
      Rect hitRect = new Rect();
      hitRect.left = headerView.getLeft();
      hitRect.right = headerView.getRight();
      hitRect.top = (int)headerView.getTranslationY();
      hitRect.bottom = (int)headerView.getTranslationY() + headerView.getHeight();

      return hitRect;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
      Log.i(TAG, "TouchEvent occurred from DragHelperView");
      if (headerViewDoubleTapOperatorEnabled)
        handleDoubleTap(event);

      event.offsetLocation(0, this.getTranslationY());
      if (event.getAction() == MotionEvent.ACTION_DOWN){
        this.captureMovementCheck(event);
        return true;
      }

      if (!tracking && !captureMovementCheck(event))
        return true;

      if (event.getAction() != MotionEvent.ACTION_MOVE || testMoveDirection(event))
        velocityTracker.addMovement(event);

      if (event.getAction() == MotionEvent.ACTION_MOVE){
        float y = event.getY();

        if(state == State.Opened && y > startY || state == State.FullyOpened && y < startY)
          return true;

        if(state == State.Opened && y > oldY || state == State.FullyOpened && y < oldY)
          velocityTracker.clear();

        int traveledDistance = (int) Math.round(Math.abs(y - startY));
        if (state == State.Opened)
          traveledDistance = getOffsetForState(State.Opened) - traveledDistance;

        setNewOffset(traveledDistance);
        oldY = y;
      } else if (event.getAction() == MotionEvent.ACTION_UP){
        velocityTracker.computeCurrentVelocity(1000, maxFlingVelocity);
        if (Math.abs(velocityTracker.getYVelocity()) > minFlingVelocity && Math.abs(velocityTracker.getYVelocity()) < maxFlingVelocity)
          setState(state == State.FullyOpened ? State.Opened : State.FullyOpened, true);
        else if (state == State.FullyOpened && headerViewOffset.offsetY > this.getHeight() / 2)
          setState(State.Opened, true);
        else if (state == State.Opened && headerViewOffset.offsetY < this.getHeight() / 2)
          setState(State.FullyOpened, true);
        else
          setState(state, true);

        preTracking = tracking = false;
        velocityTracker.clear();
        velocityTracker.recycle();
      }

      return true;
    }

    private void handleDoubleTap(MotionEvent event){
        if(dragHelperGestureDetector == null){
            initializeGestureDetector();
        }
        Log.i(TAG, "DoubleTap handler reacted");
        dragHelperGestureDetector.onTouchEvent(event);
    }

    private void initializeGestureDetector(){
        SimpleDoubleTapListener doubleTapListener = new SimpleDoubleTapListener(new Runnable() {
            @Override
            public void run() {
          Log.i(TAG, "DoubleTap occurred");
          setState(isFullyOpened() ? State.Opened : State.FullyOpened, true);
            }
        });
        dragHelperGestureDetector = new GestureDetector(this.context, doubleTapListener);
    }

    public void minimize(){
      Log.i(TAG, "minimize animation occurred!");
        if(!animationInitialized){
            initializeAnimation();
        }
        this.animating = true;
        headerView.startAnimation(minimizeAnimation);
    }

    public void maximize(){
      Log.i(TAG, "maximize animation occurred!");
        if(!animationInitialized){
            initializeAnimation();
        }
        this.animating = true;
        headerView.startAnimation(maximizeAnimation);
    }

    private boolean captureMovementCheck(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            oldY = startY = (int) event.getY();

            if(!isOpened())
                return false;

            velocityTracker = VelocityTracker.obtain();
            velocityTracker.addMovement(event);

            preTracking = true;
            stateBeforeTracking = state;

            return false;
        }

        if(event.getAction() == MotionEvent.ACTION_UP)
            preTracking = tracking = false;

        if(!preTracking)
            return false;

        velocityTracker.addMovement(event);

        if(event.getAction() == MotionEvent.ACTION_MOVE){
            if(!testMoveDirection(event)){
                preTracking = false;
                return false;
            }

            double distance = Math.abs(event.getY() - startY);
            if(distance < pagingTouchSlop)
                return false;
        }

        oldY = startY = (int) event.getY();
        tracking = true;

        return true;
    }

    private boolean testMoveDirection(MotionEvent event){
        return (stateBeforeTracking == State.FullyOpened ? event.getY() >= startY : event.getY() <= startY);
    }

    private void setNewOffset(int newOffset){
        if(state == State.Closed){
            headerViewOffset.offsetY = newOffset;
            this.setTranslationY(headerViewOffset.offsetY);
        } else {
            headerViewOffset.offsetY = Math.min(Math.max( getOffsetForState(State.FullyOpened), newOffset),
                                                          getOffsetForState(State.Opened));
            this.setTranslationY(headerViewOffset.offsetY);
        }
    }

    private int getOffsetForState(State state){
        if(layoutHeightFactor == -1)
            layoutHeightFactor = getHeight();
        if(headerViewLayoutHeightFactor == -1)
            headerViewLayoutHeightFactor = headerView.getHeight();

        switch(state){
            default:
            case Closed:
                return getBottom();
            case FullyOpened:
                return getBottom() - layoutHeightFactor;
            case Opened:
                return getBottom() - headerViewLayoutHeightFactor - getPaddingTop();
        }
    }
    private void initializeAnimation(){
        if(scaleAnimationEnabled){
            initializeScaleAnimation();
            return;
        }
        initializeDefaultAnimation();
    }

    private void initializeScaleAnimation(){
        maximizeAnimation = new HeaderViewScaleUpAnimation(headerView, (float)headerViewLayoutWidthFactor);
        maximizeAnimation.setDuration(animationDuration);
        maximizeAnimation.setInterpolator(animationInterpolator);

        float targetHeaderScaleX = ((float)headerViewLayoutWidthFactor) * scaleAnimationRatio;
        float targetScaleDownTranslationX = 0.0f;
        float targetScaleDownTranslationY = 0.0f;

        minimizeAnimation = new HeaderViewScaleDownAnimation(headerView,
                targetHeaderScaleX,
                targetScaleDownTranslationX, targetScaleDownTranslationY);
        minimizeAnimation.setDuration(animationDuration);
        minimizeAnimation.setInterpolator(animationInterpolator);
    }

    private void initializeDefaultAnimation() {
        maximizeAnimation = new SlideUpAnimation(this, layoutHeightFactor - headerViewLayoutHeightFactor);
        maximizeAnimation.setDuration(animationDuration);
        maximizeAnimation.setInterpolator(animationInterpolator);

        minimizeAnimation = new SlideDownAnimation(this, layoutHeightFactor - headerViewLayoutHeightFactor);
        minimizeAnimation.setDuration(animationDuration);
        minimizeAnimation.setInterpolator(animationInterpolator);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        if(hasFocus && !layoutInitialized){
            initializeLayout();
        }
    }

    private void initializeLayout() {
        initializeLayoutScaleFactor();
        layoutInitialized = true;
    }

    private void initializeViewConfigurations(){
        ViewConfiguration configuration = ViewConfiguration.get(this.context);
        this.pagingTouchSlop = configuration.getScaledPagingTouchSlop();
        this.minFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        this.maxFlingVelocity = configuration.getScaledMaximumFlingVelocity();

        final int baseShadowColor = 0;
        int[] shadowColors = {
                Color.argb(0x30, baseShadowColor, baseShadowColor, baseShadowColor),
                Color.argb(0, baseShadowColor, baseShadowColor, baseShadowColor)
        };

    }
}
