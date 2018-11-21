package mx.ivancastro.android_search_by_image;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Show the wikipedia page of the landmark
 */
public class InfoActivity extends AppCompatActivity {
    private static final String TAG = "InfoActivity";

    private static final String WIKIPEDIA_WEBPAGE = "https://en.wikipedia.org/wiki/";

    private WebView infoWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // We get the name of the landmark from the MainScreen
        Intent intent = getIntent();
        String landmarkName = intent.getStringExtra("landmarkName");

        // We need replace the white spaces with an underscore, so we can search the landmark in
        // Wikipedia
        landmarkName = landmarkName.replace(' ', '_');

        infoWebView = findViewById(R.id.infoWebView);
        infoWebView.loadUrl(WIKIPEDIA_WEBPAGE + landmarkName);
        infoWebView.getSettings().setJavaScriptEnabled(true);
        infoWebView.setWebViewClient(new WebViewClient());
    }
}
