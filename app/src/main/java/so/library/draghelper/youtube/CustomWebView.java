package so.library.draghelper.youtube;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

/**
 * Created by minkyu on 2015. 2. 3..
 */
public class CustomWebView extends WebView {
  public CustomWebView(Context context) {
    super(context);
    setOverScrollMode(OVER_SCROLL_NEVER);
    blockLongClick();
  }

  public CustomWebView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setOverScrollMode(OVER_SCROLL_NEVER);
    blockLongClick();
  }

  public CustomWebView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setOverScrollMode(OVER_SCROLL_NEVER);
    blockLongClick();
  }

  private void blockLongClick(){
    setVerticalScrollBarEnabled(false);
    setHorizontalScrollBarEnabled(false);
    setScrollContainer(false);
    setOnLongClickListener(new OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        return true;
      }
    });
    setLongClickable(false);
  }
}
