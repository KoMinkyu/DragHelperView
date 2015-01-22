package so.library.draghelper.helper.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;

import so.library.draghelper.helper.SimpleDoubleTapListener;
import so.library.draghelper.helper.animation.HeaderViewMinimizeAnimation;
import so.library.draghelper.helper.animation.SlideDownAnimation;
import so.library.draghelper.helper.animation.SlideUpAnimation;
import so.library.draghelper.helper.interpolator.SmoothInterpolator;

/**
 * Created by minkyu on 2015. 1. 20..
 */
public class DragHelperView extends RelativeLayout{

    public enum State{Closed, Opened, FullyOpened};

    public static final String TAG = DragHelperView.class.getSimpleName();

    static final int DEFAULT_ANIMATION_DURATION = 600;
    static final float DEFAULT_SCALE_ANIMATION_RATIO = 0.4f;

    private static final int HEADER_VIEW_INDEX = 0;
    private static final int BODY_VIEW_INDEX = 1;

    private State state;

    private View headerView;
    private View bodyView;

    private Animation headerMaximizeAnimation;
    private Animation headerMinimizeAnimation;

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

    private boolean animating = false;
    private boolean headerViewDoubleTapOperatorEnabled = false;

    private GestureDetector dragHelperGestureDetector;

    private boolean layoutInitialized = false;
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
        return state == State.Opened || state == State.FullyOpened;
    }

    public void minimize(){
        this.animating = true;
        this.state = State.Opened;

        int targetTranslationY = layoutHeightFactor - headerViewLayoutHeightFactor;

        SlideDownAnimation animation = new SlideDownAnimation(headerView, (float)targetTranslationY);
        animation.setDuration(animationDuration);
        animation.setInterpolator(animationInterpolator);
        bodyView.setVisibility(INVISIBLE);
        headerView.startAnimation(animation);
    }

    public void maximize(){
        this.animating = true;
        this.state = State.FullyOpened;

        SlideUpAnimation animation = new SlideUpAnimation(headerView);
        animation.setDuration(animationDuration);
        animation.setInterpolator(animationInterpolator);
        headerView.startAnimation(animation);
    }

    public void setState(State newState, boolean animated){
        if(useDefaultAnimationInterpolator)
            this.animationInterpolator = new SmoothInterpolator();

        Interpolator interpolator = state == State.Closed && newState == State.Opened ? overshootInterpolator : animationInterpolator;

        this.state = newState;
        if (newState != State.Closed)
            this.setVisibility(VISIBLE);
        if (!animated){

        } else {
            this.animating = true;

        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event){
        final int action = MotionEventCompat.getActionMasked(event);

        if(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP)
            return false;

        if(isTouchInterceptableByChildViews(event))
            return true;

        return false;
    }

    private boolean isTouchInterceptableByChildViews(MotionEvent event){
        return isMotionEventOccurredOnHeaderView(event) && isMotionEventOccurredOnBodyView(event);
    }

    private boolean isMotionEventOccurredOnHeaderView(MotionEvent event){
        Rect viewHitRect = new Rect();

        headerView.getHitRect(viewHitRect);
        Log.i(TAG, "Hit Rect(HeaderView):" + viewHitRect.toString());
        return viewHitRect.contains((int)event.getRawX(), (int)event.getRawY());
    }

    private boolean isMotionEventOccurredOnBodyView(MotionEvent event){
        Rect viewHitRect = new Rect();

        bodyView.getHitRect(viewHitRect);
        return viewHitRect.contains((int)event.getRawX(), (int)event.getRawY());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        if(hasFocus && !layoutInitialized){
            initializeLayout();
        }
    }

    private void initializeLayout() {
        int bodyViewHeight = getBottom() - headerView.getBottom();

        ViewGroup.LayoutParams initialBodyViewLayoutParams;
        initialBodyViewLayoutParams = bodyView.getLayoutParams();
        initialBodyViewLayoutParams.height = bodyViewHeight;

        bodyView.setTranslationY(headerView.getBottom());

        initializeLayoutScaleFactor();

        layoutInitialized = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (headerViewDoubleTapOperatorEnabled)
            handleDoubleTap(event);

        return true;
    }

    private void handleDoubleTap(MotionEvent event){
        if(dragHelperGestureDetector == null){
            initializeGestureDetector();
        }
        dragHelperGestureDetector.onTouchEvent(event);
    }

    private void initializeGestureDetector(){
        SimpleDoubleTapListener doubleTapListener = new SimpleDoubleTapListener(new Runnable() {
                @Override
public void run() {
            testAnimate(isOpened() && isFullyOpened() ? State.Opened : State.FullyOpened);
            }
        });
        dragHelperGestureDetector = new GestureDetector(this.context, doubleTapListener);
    }

    private void testAnimate(State newState){
        if (newState == State.FullyOpened)
            maximize();
        else if(newState == State.Opened)
            minimize();
    }

}
