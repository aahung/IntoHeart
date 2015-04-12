package ee3316.intoheart;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Vivian on 12/4/15.
 */
public class DiseaseDetailActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_detail);
        WebView webView= (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());

        Bundle bundle= this.getIntent().getExtras();
        String urls= bundle.getString("url");
        webView.loadUrl(urls);
    }
}
