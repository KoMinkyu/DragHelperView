package so.library.draghelper;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import so.library.draghelper.helper.animation.HeaderViewMaximizeAnimation;
import so.library.draghelper.helper.animation.HeaderViewMinimizeAnimation;
import so.library.draghelper.helper.interpolator.SmoothInterpolator;
import so.library.draghelper.helper.view.DragHelperView;


public class MainActivity extends YouTubeBaseActivity{

	private final float VIDEO_PLAYER_RATIO = 450f / 800f;
	private final float VIDEO_MINIMIZE_RATIO = 0.6000f;

	private static final String YOUTUBE_API_KEY = "AIzaSyC1rMU-mkhoyTvBIdTnYU0dss0tU9vtK48";
	private static final String YOUTUBE_VIDEO_KEY = "IgR5xajsaT4";

	private YouTubePlayerView youTubePlayerView;
	private YouTubePlayer youTubeController;

    private ImageView bodyView;

    private DragHelperView dragHelperView;
    private RelativeLayout dragLayout;

	private int screenWidth;
	private int screenHeight;

	private int PLAYER_MAXIMIZED = 1;
	private int PLAYER_MINIMIZED = 2;
	private int playerStatus = PLAYER_MAXIMIZED;
	private boolean isAnimating = false;

	private SmoothInterpolator smoothInterpolator = new SmoothInterpolator();

    private GestureDetector headerViewGestureDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getDeviceScreenSize();
		initializeView();
	}

	private void initializeView(){
//        dragHelperView = (DragHelperView) findViewById(R.id.drag_helper_view);
        dragLayout = (RelativeLayout) findViewById(R.id.drag_helper_view);
		youTubePlayerView = (YouTubePlayerView)findViewById(R.id.youtube_player);
        bodyView = (ImageView)findViewById(R.id.body_view);

		youTubePlayerView.initialize(YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {
			@Override
			public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
				youTubeController = youTubePlayer;
				initializeYouTubeVideoPlayer();
			}

			@Override
			public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

			}
		});
		youTubePlayerView.setPivotX(0);
		youTubePlayerView.setPivotY(0);

        dragLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (playerStatus == PLAYER_MINIMIZED){
                    maximizeWithObjectAnimator();
                } else if(playerStatus == PLAYER_MAXIMIZED){
                    minimizeWithObjectAnimator();
                }
                return false;
            }
        });
//        dragHelperView.initialize(youTubePlayerView, bodyView)
//                      .setAnimationInterpolator(new so.library.draghelper.helper.interpolator.SmoothInterpolator())
//                      .setDoubleTapAnimationEnabled(true);
	}

	private void initializeYouTubeVideoPlayer(){
		youTubeController.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
		youTubeController.setShowFullscreenButton(true);
		youTubeController.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION
				| YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI
				| YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);

		youTubeController.loadVideo(YOUTUBE_VIDEO_KEY);
	}

	private void logViewInformation(){
		Log.i("ANIMATION_INFO", "header scale info : " + youTubePlayerView.getScaleX() + ", " + youTubePlayerView.getScaleY());
	}

	private void logTranslationInformation(float x, float y){
		Log.i("ANIMATION_INFO", "translation info : " + x + ", " + y);
	}

	private void getDeviceScreenSize(){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		this.screenWidth = displayMetrics.widthPixels;
		this.screenHeight = displayMetrics.heightPixels;
	}

	private float getMinimizeTranslationX(){
		float translationX = ((float)screenWidth) - (((float)screenWidth) * VIDEO_MINIMIZE_RATIO);
		return translationX;
	}

	private float getMinimizeTranslationY(){
		float translationY = ((float)((View) youTubePlayerView.getParent()).getHeight()) - ((float) youTubePlayerView.getHeight() * VIDEO_MINIMIZE_RATIO);
		return translationY;
	}

	private void minimize(Runnable endAction){

		isAnimating = true;
		playerStatus = PLAYER_MINIMIZED;

		ViewPropertyAnimatorCompat animator = ViewCompat.animate(youTubePlayerView);
		float translationX = getMinimizeTranslationX();
		float translationY = getMinimizeTranslationY();

		logTranslationInformation(translationX, translationY);

		if (endAction != null){
			animator.withEndAction(endAction);
		}
		animator.setDuration(500);
		animator.translationX(translationX).translationY(translationY);
		animator.scaleX(VIDEO_MINIMIZE_RATIO).scaleY(VIDEO_MINIMIZE_RATIO);
		animator.setInterpolator(smoothInterpolator);

		animator.start();
	}

	private void minimizeWithObjectAnimator(){
		isAnimating = true;
		playerStatus = PLAYER_MINIMIZED;

		float translationX = getMinimizeTranslationX();
		float translationY = getMinimizeTranslationY();

		HeaderViewMinimizeAnimation animation = new HeaderViewMinimizeAnimation(youTubePlayerView, (int)(((float)screenWidth) * VIDEO_MINIMIZE_RATIO), translationX, translationY);
		animation.setDuration(600);
		animation.setInterpolator(smoothInterpolator);
		youTubeController.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
        bodyView.setVisibility(View.INVISIBLE);
		youTubePlayerView.startAnimation(animation);
	}

	private void maximizeWithObjectAnimator(){
		isAnimating = true;
		playerStatus = PLAYER_MAXIMIZED;

		HeaderViewMaximizeAnimation animation = new HeaderViewMaximizeAnimation(youTubePlayerView, screenWidth);
		animation.setDuration(600);
		animation.setInterpolator(smoothInterpolator);
		youTubeController.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
		youTubePlayerView.startAnimation(animation);
	}

	private class SmoothInterpolator extends LinearInterpolator{
		@Override
		public float getInterpolation(float input){
			return (float) Math.pow(input - 1, 5) + 1;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
