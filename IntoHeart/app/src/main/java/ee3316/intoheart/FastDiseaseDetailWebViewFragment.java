package ee3316.intoheart;

    import android.os.Bundle;
    import android.util.Log;
    import android.webkit.WebView;
    import android.webkit.WebViewFragment;
    import android.widget.Button;

    /**
     * Created by Vivian on 10/4/15.
     */
    public class FastDiseaseDetailWebViewFragment extends WebViewFragment{
        private static final String DEBUG_TAG = "FastDiseaseDetailWebViewFragment";
        public static FastDiseaseDetailWebViewFragment newInstance(int index){
            Log.v(DEBUG_TAG, "Creating new instance: " + index);
            FastDiseaseDetailWebViewFragment fragment = new FastDiseaseDetailWebViewFragment();

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
            String[] fastDiseaseUrls= getResources().getStringArray(R.array.fastdiseaseurls_array);
            int fastDiseaseUrlIndex = getShownIndex();

            WebView webview = getWebView();
            webview.setPadding(0,0,0,0);
            webview.getSettings().setLoadWithOverviewMode(true);
            webview.getSettings().setUseWideViewPort(true);


                String fastDiseaseUrl = fastDiseaseUrls[fastDiseaseUrlIndex];
                webview.loadUrl(fastDiseaseUrl);


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


