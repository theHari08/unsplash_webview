package example.hari.webview;

import android.Manifest;
import android.app.DownloadManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private WebView webview;
    private ProgressBar progressbar;
    boolean doubleBackToExitPressedOnce = false;
    String url = "https://unsplash.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webview = (WebView)findViewById(R.id.webView);
        webview.loadUrl(url);
        webview.setWebViewClient(new WebViewClient());
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        final SwipeRefreshLayout swipetorefresh;
        swipetorefresh = findViewById(R.id.swiperefresh);

        // request storage permission to download
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},5);

        // SwipeRefreshLayout
        swipetorefresh.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // This method performs the actual data-refresh operation.
                // The method calls setRefreshing(false) when it's finished.
                webview.loadUrl(webview.getUrl());
            }
        });

        // Download manager to allow downloading in webview
        webview.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(url));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                // save downloaded file in its own filename
                final String filename= URLUtil.guessFileName(url, contentDisposition, mimetype);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading File",
                Toast.LENGTH_LONG).show();
            }
        });

        // Get the widgets reference from XML layout
        progressbar = findViewById(R.id.pb);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Visible the progressbar
                progressbar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                swipetorefresh.setRefreshing(false);
                progressbar.setVisibility(View.GONE);
            }
        });

        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int newProgress){
                // Update the progress bar with page loading progress
                progressbar.setProgress(newProgress);
                if(newProgress == 100){
                    // Hide the progressbar
                    progressbar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onBackPressed(){
        // go back through history on pressing back
        if(webview.canGoBack()) {
            webview.goBack();
        } else
        {

            // double tap BACK within two seconds to exit
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);

            // clear cache when exit
            webview.clearCache(true);
        }
    }

}
