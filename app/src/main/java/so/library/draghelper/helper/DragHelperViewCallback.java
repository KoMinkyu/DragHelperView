package so.library.draghelper.helper;

import android.support.v4.widget.ViewDragHelper;
import android.view.View;
import android.view.ViewGroup;

import so.library.draghelper.helper.view.PopupPanel;

/**
 * Created by minkyu on 2015. 2. 4..
 */
public class DragHelperViewCallback extends ViewDragHelper.Callback {

  private PopupPanel dragHelperView;
  private View headerView;

  /**
   * Main constructor.
   *
   * @param dragHelperView
   */
  public DragHelperViewCallback(PopupPanel dragHelperView, View headerView){
    this.dragHelperView = dragHelperView;
    this.headerView = headerView;
  }

  /**
   *
   * @param view
   * @param pointerId
   * @return
   */
  @Override
  public boolean tryCaptureView(View view, int pointerId) {
    return view.equals(headerView);
  }
}
