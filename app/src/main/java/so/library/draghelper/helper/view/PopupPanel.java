package so.library.draghelper.helper.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import so.library.draghelper.R;
import so.library.draghelper.helper.DragHelperViewCallback;

public class PopupPanel extends LinearLayout{

  public enum State{
    Closed,
    Opened,
    FullyOpened
  }

  public interface OnStateChangedListener{
    void onStateChanged(State state);
  }

  private Context context;

  OnStateChangedListener onStateChangedListener;
  State state;
  int contentOffsetY;
  boolean isAnimating;
  SmoothInterpolator smoothInterpolator = new SmoothInterpolator();
  OvershootInterpolator overInterpolator = new OvershootInterpolator();
  VelocityTracker velocityTracker;
  GestureDetector paneGestureDetector;
  State stateBeforeTracking;
  boolean isTracking;
  boolean preTracking;
  int startY = -1;
  float oldY;

  int pagingTouchSlop;
  int minFlingVelocity;
  int maxFlingVelocity;

  int idPaneHeaderView = 0;

  GradientDrawable shadowDrawable;

  View parentView;

  private View headerView;
  private View bodyView;

  int deviceHeight;
  int deviceWidth;


  /**
   * Relevant variables with ViewDragHelper
   */
  private ViewDragHelper viewDragHelper;

  private boolean layoutInitialized = false;

  public PopupPanel(Context context){
    super(context);
    this.context = context;
    this.initialize();
  }

  public PopupPanel(Context context, AttributeSet attrs){
    super(context, attrs);
    this.context = context;
    this.initialize();
  }

  public PopupPanel(Context context, AttributeSet attrs, int defStyleAttr){
    super(context, attrs, defStyleAttr);
    this.context = context;
    this.initialize();
  }

  public void setPaneHeaderViewId(int idPaneHeaderView){
    this.idPaneHeaderView = idPaneHeaderView;
  }

  public void setParentView(View parentView){
    this.parentView = parentView;
  }

  private int getPaneHeaderViewId(){
    return this.idPaneHeaderView;
  }

  public void setChildViews(WebView headerView, View bodyView) {
    this.headerView = headerView;
    this.bodyView = bodyView;
  }

  private void initialize(){
    state = State.FullyOpened;
    ViewConfiguration config = ViewConfiguration.get(this.context);
    this.pagingTouchSlop = config.getScaledPagingTouchSlop();
    this.minFlingVelocity = config.getScaledMinimumFlingVelocity();
    this.maxFlingVelocity = config.getScaledMaximumFlingVelocity();
    final int baseShadowColor = 0;
    int[] shadowColors = {
            Color.argb(0x30, baseShadowColor, baseShadowColor, baseShadowColor),
            Color.argb(0, baseShadowColor, baseShadowColor, baseShadowColor)
    };

    this.shadowDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, shadowColors);

    viewDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperViewCallback(this, headerView));
  }

  public boolean isOpened(){
    return state == State.Opened || state == State.FullyOpened;
  }

  public boolean isFullyOpened(){
    return state == State.FullyOpened;
  }

  public void setState(State newState, boolean animated){
    Interpolator interpolator = state == State.Closed && newState == State.Opened ? overInterpolator : smoothInterpolator;
    this.state = newState;
    if (newState != State.Closed)
      this.setVisibility(View.VISIBLE);
    if (!animated){
      setNewOffset(getOffsetForState(newState));
      if (state == State.Closed)
        this.setVisibility(View.INVISIBLE);
    } else {
      isAnimating = true;
      int duration = Resources.getSystem().getInteger(android.R.integer.config_mediumAnimTime);
      this.translationYAnimate(this, getOffsetForState(newState), duration, interpolator, new Runnable() {
        @Override
        public void run() {
          isAnimating = false;
          if (state == State.Closed)
            setVisibility(View.INVISIBLE);
        }
      });
    }
    if (this.onStateChangedListener != null){
      this.onStateChangedListener.onStateChanged(newState);
    }
  }

  public void setOnStateChangedListener(OnStateChangedListener l){
    this.onStateChangedListener = l;
  }

  private void setNewOffset(int newOffset){
    if (state == State.Closed){
      contentOffsetY = newOffset;
      this.setTranslationY(contentOffsetY);
    } else {
      contentOffsetY = Math.min(Math.max( getOffsetForState(State.FullyOpened), newOffset),
              getOffsetForState(State.Opened));
      this.setTranslationY(contentOffsetY);
    }
  }

  private int getOffsetForState(State state){

    switch(state){
      default:
      case Closed:
        return parentView.getBottom();
      case FullyOpened:
        return parentView.getBottom() - this.getHeight();
      case Opened:
        int idPaneHeaderView = this.getPaneHeaderViewId();
        return parentView.getBottom() - findViewById(idPaneHeaderView).getHeight() - this.getPaddingTop();
    }
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent e){
    final int action = MotionEventCompat.getActionMasked(e);

    if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP){
      return false;
    }

    boolean interceptTap = viewDragHelper.isViewUnder(headerView, (int) e.getX(), (int) e.getY());
    return viewDragHelper.shouldInterceptTouchEvent(e) || interceptTap;
  }

  @Override
  public boolean onTouchEvent(MotionEvent e){
    if (paneGestureDetector == null){
      DoubleTapListener l = new DoubleTapListener(new Runnable() {

        @Override
        public void run() {
          setState(isOpened() && isFullyOpened() ? State.Opened : State.FullyOpened, true);
        }
      });
      paneGestureDetector = new GestureDetector(this.context, l);
    }

    paneGestureDetector.onTouchEvent(e);

    e.offsetLocation(0, this.getTranslationY());
    if (e.getAction() == MotionEvent.ACTION_DOWN){
      this.captureMovementCheck(e);
      headerView.dispatchTouchEvent(e);
      return true;
    }

    if (!isTracking && !captureMovementCheck(e)){
      headerView.dispatchTouchEvent(e);
      return true;
    }

    if (e.getAction() != MotionEvent.ACTION_MOVE || moveDirectionTest(e))
      velocityTracker.addMovement(e);

    if (e.getAction() == MotionEvent.ACTION_MOVE){
      float y = e.getY();

      if(state == State.Opened && y > startY || state == State.FullyOpened && y < startY)
        return true;

      if(state == State.Opened && y > oldY || state == State.FullyOpened && y < oldY)
        velocityTracker.clear();

      int traveledDistance = (int) Math.round(Math.abs(y - startY));
      if (state == State.Opened)
        traveledDistance = getOffsetForState(State.Opened) - traveledDistance;

      setNewOffset(traveledDistance);
      oldY = y;
    } else if (e.getAction() == MotionEvent.ACTION_UP){
      velocityTracker.computeCurrentVelocity(1000, maxFlingVelocity);
      if (Math.abs(velocityTracker.getYVelocity()) > minFlingVelocity && Math.abs(velocityTracker.getYVelocity()) < maxFlingVelocity)
        setState(state == State.FullyOpened ? State.Opened : State.FullyOpened, true);
      else if (state == State.FullyOpened && contentOffsetY > this.getHeight() / 2)
        setState(State.Opened, true);
      else if (state == State.Opened && contentOffsetY < this.getHeight() / 2)
        setState(State.FullyOpened, true);
      else
        setState(state, true);

      preTracking = isTracking = false;
      velocityTracker.clear();
      velocityTracker.recycle();
    }

    return true;
  }

  private boolean captureMovementCheck(MotionEvent e){
    if (e.getAction() == MotionEvent.ACTION_DOWN){
      oldY = startY = (int)e.getY();

      if(!isOpened())
        return false;

      velocityTracker = VelocityTracker.obtain();
      velocityTracker.addMovement(e);

      preTracking = true;
      stateBeforeTracking = state;

      return false;
    }

    if (e.getAction() == MotionEvent.ACTION_UP)
      preTracking = isTracking = false;

    if (!preTracking)
      return false;

    velocityTracker.addMovement(e);

    if (e.getAction() == MotionEvent.ACTION_MOVE){
      if(!this.moveDirectionTest(e)){
        preTracking = false;
        return false;
      }

      double distance = Math.abs(e.getY() - startY);
      if (distance < pagingTouchSlop)
        return false;
    }

    oldY = startY = (int) e.getY();
    isTracking = true;

    return true;

  }

  private boolean moveDirectionTest(MotionEvent e){
    return (stateBeforeTracking == State.FullyOpened ? e.getY() >= startY : e.getY() <= startY);
  }

  private class SmoothInterpolator extends LinearInterpolator{
    @Override
    public float getInterpolation(float input){
      return (float) Math.pow(input - 1, 5) + 1;
    }
  }

  private class DoubleTapListener extends SimpleOnGestureListener{
    Runnable callback;

    public DoubleTapListener(Runnable callback){
      this.callback = callback;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e){
      callback.run();
      return true;
    }
  }

  private void translationYAnimate(View view, int translation, int duration, Interpolator interpolator, Runnable endAction){
    ViewPropertyAnimatorCompat animator = ViewCompat.animate(view);
    animator.setDuration(duration).translationY(translation);
    if(endAction != null)
      animator.withEndAction(endAction);
    if(interpolator != null)
      animator.setInterpolator(interpolator);

    animator.start();

  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus){
    if(hasFocus && !layoutInitialized){
      deviceHeight = getHeight();
      deviceWidth = getWidth();
      layoutInitialized = true;

      this.headerView.getLayoutParams().height = (int)((float)deviceWidth * 0.55f);
      this.headerView.requestLayout();
    }
  }

}
