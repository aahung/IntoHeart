package ee3316.intoheart;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewFragment;
import android.widget.Button;

/**
 * Created by Vivian on 10/4/15.
 */
public class SlowDiseaseDetailWebViewFragment extends WebViewFragment{
    private static final String DEBUG_TAG = "SlowDiseaseDetailWebViewFragment";
    public static SlowDiseaseDetailWebViewFragment newInstance(int index){
        Log.v(DEBUG_TAG, "Creating new instance: " + index);
        SlowDiseaseDetailWebViewFragment fragment = new SlowDiseaseDetailWebViewFragment();

        Bundle args = new Bundle();
        args.putInt("index",index);
        fragment.setArguments(args);
        return fragment;
    }

    public int getShownIndex() {
        int index = -1;
        Bundle args = getArguments();
        if(args != null ){
            index=args.getInt("index",-1);
        }
        if(index == -1){
            Log.e(DEBUG_TAG,"Not an array index.");
        }
        return index;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        String[] slowDiseaseUrls= getResources().getStringArray(R.array.slowdiseaseurls_array);
        int slowDiseaseUrlIndex = getShownIndex();

        WebView webview = getWebView();
        webview.setPadding(0,0,0,0);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setUseWideViewPort(true);


            String slowDiseaseUrl = slowDiseaseUrls[slowDiseaseUrlIndex];
            webview.loadUrl(slowDiseaseUrl);


    }
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
