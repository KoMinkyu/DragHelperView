package so.library.draghelper;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import so.library.draghelper.helper.interpolator.SmoothInterpolator;
import so.library.draghelper.youtube.CustomWebView;
import so.library.draghelper.helper.view.DragHelperView;
import so.library.draghelper.helper.view.PopupPanel;


public class MainActivity extends YouTubeBaseActivity{

	private final float VIDEO_PLAYER_RATIO = 450f / 800f;
	private final float VIDEO_MINIMIZE_RATIO = 0.6000f;

	private static final String YOUTUBE_API_KEY = "AIzaSyCRA4oW3UyK04ml3dOp94IO53mHdc3XNEg";
	private static final String YOUTUBE_VIDEO_KEY = "IgR5xajsaT4";

	private YouTubePlayerView youTubePlayerView;
	private YouTubePlayer youTubeController;


  private ImageView bodyView;
  private LinearLayout popupBodyView;
  private Button btnPlayVideo;

  private DragHelperView dragHelperView;

	@Override	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

    getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

		initializeTestView();
	}

  private void initializeYouTubeWebView(){
    bodyView = (ImageView) findViewById(R.id.body_view);
    bodyView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, YouTubeActivity.class);
        startActivity(intent);
      }
    });
  }

  private void initializeTestView() {
    final PopupPanel dragHelperView = (PopupPanel) getLayoutInflater().inflate(R.layout.popup_dialog, null);
    final CustomWebView testHeaderView = (CustomWebView) dragHelperView.findViewById(R.id.test_popup_header);
    btnPlayVideo = (Button) dragHelperView.findViewById(R.id.btn_play_video);

    btnPlayVideo.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        testHeaderView.loadUrl("javascript:player.loadVideoById(\"IgR5xajsaT4\", 0, \"large\")");
      }
    });

    WebSettings youTubeWebViewSettings = testHeaderView.getSettings();
    youTubeWebViewSettings.setJavaScriptEnabled(true);
    youTubeWebViewSettings.setPluginState(WebSettings.PluginState.ON);
//    youTubeWebViewSettings.setUseWideViewPort(true);
    youTubeWebViewSettings.setLoadWithOverviewMode(true);
    youTubeWebViewSettings.setSupportMultipleWindows(true);
//    youTubeWebViewSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
    testHeaderView.setWebChromeClient(new WebChromeClient());
    testHeaderView.setWebViewClient(new WebViewClient());
    testHeaderView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){

          int temp_ScrollY = testHeaderView.getScrollY();
          testHeaderView.scrollTo(testHeaderView.getScrollX(), testHeaderView.getScrollY() + 1);
          testHeaderView.scrollTo(testHeaderView.getScrollX(), temp_ScrollY);

        }
        return false;
      }
    });
//    testHeaderView.loadUrl("http://assets2.moncast.com/web/youtube.html?videoId=IgR5xajsaT4&autoplay=0");
    testHeaderView.loadDataWithBaseURL("https://www.youtube.com/", getHTML(), "text/html", "utf-8", null);

    final LinearLayout testBodyView = (LinearLayout) dragHelperView.findViewById(R.id.test_popup_body);
    final View testDragHelperViewParent = dragHelperView.findViewById(R.id.test_popup_drag_view);
    dragHelperView.setPaneHeaderViewId(R.id.test_popup_header);
    dragHelperView.setParentView(testDragHelperViewParent);

    bodyView = (ImageView) findViewById(R.id.body_view);
    bodyView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Dialog dialog = new Dialog(MainActivity.this, R.style.VideoDetailDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dragHelperView);
        dialog.show();

        dragHelperView.setChildViews(testHeaderView, testBodyView);
      }
    });
  }

  public String getHTML(){
    String html= "\n" +
      "<!--\n" +
      "     Copyright 2014 Google Inc. All rights reserved.\n" +
      "\n" +
      "     Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
      "     you may not use this file except in compliance with the License.\n" +
      "     You may obtain a copy of the License at\n" +
      "\n" +
      "     http://www.apache.org/licenses/LICENSE-2.0\n" +
      "\n" +
      "     Unless required by applicable law or agreed to in writing, software\n" +
      "     distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
      "     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
      "     See the License for the specific language governing permissions and\n" +
      "     limitations under the License.\n" +
      "-->\n" +
      "<!DOCTYPE html>\n" +
      "<html>\n" +
      "<head>\n" +
      "    <style>\n" +
      "        body { margin: 0; width:100%%; height:100%%; background-color: #000; }\n" +
      "        html { width:100%%; height:100%%; background-color: #000; }\n" +
      "    </style>\n" +
      "</head>\n" +
      "<body>\n" +
      "    <div id=\"player\"></div>\n" +
      "    <script src=\"https://www.youtube.com/iframe_api\"></script>\n" +
      "    <script>\n" +
      "    var iOS = (navigator.userAgent.match(/(iPad|iPhone|iPod)/g) ? true : false);\n" +
      "    var Android = (navigator.userAgent.toLowerCase().match(/(android)/g) ? true : false);\n" +
      "\n" +
      "    var QueryString = function () {\n" +
      "        var query_string = {};\n" +
      "        var query = window.location.search.substring(1);\n" +
      "        var vars = query.split(\"&\");\n" +
      "        for (var i=0;i<vars.length;i++) {\n" +
      "            var pair = vars[i].split(\"=\");\n" +
      "            if (typeof query_string[pair[0]] === \"undefined\") {\n" +
      "                query_string[pair[0]] = pair[1];\n" +
      "            } else if (typeof query_string[pair[0]] === \"string\") {\n" +
      "                var arr = [ query_string[pair[0]], pair[1] ];\n" +
      "                query_string[pair[0]] = arr;\n" +
      "            } else {\n" +
      "                query_string[pair[0]].push(pair[1]);\n" +
      "            }\n" +
      "        }\n" +
      "        return query_string;\n" +
      "    }();\n" +
      "\n" +
      "    var player;\n" +
      "    var playerTimer = null;\n" +
      "    var disableTimer = 0;\n" +
      "    if (QueryString['disableTimer'])\n" +
      "        disableTimer = parseInt(QueryString['disableTimer']);\n" +
      "    var showInterface = 0;\n" +
      "    if (QueryString['showInterface'])\n" +
      "        showInterface = parseInt(QueryString['showInterface']);\n" +
      "\n" +
      "    YT.ready(function() {\n" +
      "        player = new YT.Player('player', {\n" +
      "            height: '100%',\n" +
      "            width: '100%',\n" +
      "            videoId: QueryString['videoId'],\n" +
      "            playerVars: {\n" +
      "                'autoplay': 0,\n" +
      "                'modestbranding': 1,\n" +
      "                'rel': 0,\n" +
      "                'showinfo': 0,\n" +
      "                'controls': showInterface,\n" +
      "                'egm' : 0,\n" +
      "                'autohide': 1,\n" +
      "                'wmode': 'transparent',\n" +
      "                'showsearch' : 0,\n" +
      "                'iv_load_policy': 3,\n" +
      "                'playsinline': 1\n" +
      "            },\n" +
      "            events: {\n" +
      "                'onReady': onReady,\n" +
      "                'onStateChange': onStateChange,\n" +
      "                'onPlaybackQualityChange': onPlaybackQualityChange,\n" +
      "                'onError': onPlayerError\n" +
      "            }\n" +
      "        });\n" +
      "\n" +
      "        if (iOS == true) {\n" +
      "            window.location.href = 'ytplayer://onYouTubeIframeAPIReady';\n" +
      "        }\n" +
      "        if (Android == true) {\n" +
      "\n" +
      "        }\n" +
      "    });\n" +
      "    function onReady(event) {\n" +
      "        if (QueryString['autoplay'] == 1)\n" +
      "            event.target.playVideo();\n" +
      "\n" +
      "        if (iOS == true) {\n" +
      "            window.location.href = 'ytplayer://onReady?data=' + event.data;\n" +
      "        }\n" +
      "        if (Android == true) {\n" +
      "\n" +
      "        }\n" +
      "    }\n" +
      "    function onStateChange(event) {\n" +
      "        if (iOS == true) {\n" +
      "            window.location.href = 'ytplayer://onStateChange?data=' + event.data;\n" +
      "\n" +
      "            if (disableTimer == 0)\n" +
      "            {\n" +
      "                if (event.data == YT.PlayerState.PLAYING)\n" +
      "                {\n" +
      "                    if (playerTimer != null)\n" +
      "                        clearInterval(playerTimer);\n" +
      "\n" +
      "                    playerTimer = setInterval(function(){\n" +
      "                        window.location.href = 'ytplayer://updateTime?loaded=' + player.getVideoLoadedFraction() + \"&currentTime=\" + player.getCurrentTime() + \"&duration=\" + player.getDuration();\n" +
      "                    }, 500);\n" +
      "                }\n" +
      "                else if (event.data == YT.PlayerState.ENDED || event.data == YT.PlayerState.PAUSED)\n" +
      "                {\n" +
      "                    clearInterval(playerTimer);\n" +
      "                    playerTimer = null;\n" +
      "                }\n" +
      "            }\n" +
      "        }\n" +
      "        if (Android == true) {\n" +
      "\n" +
      "        }\n" +
      "    }\n" +
      "\n" +
      "    function onPlaybackQualityChange(event) {\n" +
      "        if (iOS == true) {\n" +
      "            window.location.href = 'ytplayer://onPlaybackQualityChange?data=' + event.data;\n" +
      "        }\n" +
      "        if (Android == true) {\n" +
      "\n" +
      "        }\n" +
      "    }\n" +
      "    function onPlayerError(event) {\n" +
      "        if (iOS == true) {\n" +
      "            window.location.href = 'ytplayer://onError?data=' + event.data;\n" +
      "        }\n" +
      "        else if (Android == true) {\n" +
      "\n" +
      "        }\n" +
      "        else {\n" +
      "            if (event.data == 100) {\n" +
      "                alert('동영상을 찾을 수 없습니다!\\n(Error code: ' + event.data + ')');\n" +
      "            }\n" +
      "            else if (event.data == 101 || event.data == 150) {\n" +
      "                alert('동영상의 소유자가 내장 플레이어에서 동영상을 재생하는 것을 허용하지 않았습니다!\\n(Error code: ' + event.data + ')');\n" +
      "            }\n" +
      "        }\n" +
      "    }\n" +
      "    </script>\n" +
      "</body>\n" +
      "</html>";
    return html;

  }
  private void initializeView(){

    dragHelperView = (DragHelperView) getLayoutInflater().inflate(R.layout.popup_youtube_player, null);
    youTubePlayerView = (YouTubePlayerView) dragHelperView.findViewById(R.id.popup_youtube_player);
    popupBodyView = (LinearLayout) dragHelperView.findViewById(R.id.popup_body_view);

    bodyView = (ImageView) findViewById(R.id.body_view);
    bodyView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Dialog dialog = new Dialog(MainActivity.this, R.style.VideoDetailDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dragHelperView);
        dragHelperView.initialize(youTubePlayerView, popupBodyView);
        dragHelperView.setAnimationInterpolator(new SmoothInterpolator())
                      .setDoubleTapAnimationEnabled(true);
        dialog.show();

        initializeYouTubePlayerView();
      }
    });

    DisplayMetrics metrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(metrics);
	}

  private void initializeYouTubePlayerView(){
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
  }

	private void initializeYouTubeVideoPlayer(){
		youTubeController.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
		youTubeController.setShowFullscreenButton(true);
		youTubeController.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION
				| YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI
				| YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);

		youTubeController.loadVideo(YOUTUBE_VIDEO_KEY);
	}

  private int getStatusBarHeight() {
    Rect rect = new Rect();
    Window window = getWindow();
    window.getDecorView().getWindowVisibleDisplayFrame(rect);

    return rect.top;
  }

  private class CustomChromeClient extends WebChromeClient implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    FrameLayout.LayoutParams COVER_SCREEN_GRAVITY_CENTER = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
      if (view instanceof FrameLayout){

      }
    }
    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
      return false;
    }
  }

}
