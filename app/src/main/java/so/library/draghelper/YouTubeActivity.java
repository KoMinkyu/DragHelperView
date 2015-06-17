package so.library.draghelper;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import so.library.draghelper.youtube.CustomWebView;


public class YouTubeActivity extends ActionBarActivity {

  private CustomWebView youTubeWebView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_you_tube);

    youTubeWebView = (CustomWebView) findViewById(R.id.youtube_wv_player);
  }
}
