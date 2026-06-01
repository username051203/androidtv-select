package com.braviatv.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class TVActivity extends Activity {

    private WebView webView;
    // tv.html is bundled in app/src/main/assets/tv.html
    private static final String TV_HTML_URL = "file:///android_asset/tv.html";

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on while app is open
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Full-screen immersive — hides status bar and navigation bar
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        setContentView(R.layout.activity_tv);
        webView = findViewById(R.id.webview);

        setupWebView();
        webView.loadUrl(TV_HTML_URL);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings s = webView.getSettings();

        // Core
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);       // localStorage — needed for sync with mobile
        s.setDatabaseEnabled(true);

        // Media — no user gesture required (TV remote can't "tap" to start video)
        s.setMediaPlaybackRequiresUserGesture(false);

        // Viewport — fill the full 1080p screen
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);

        // Zoom — disabled, our HTML controls the layout
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);

        // Cache
        s.setCacheMode(WebSettings.LOAD_DEFAULT);

        // User-Agent — lets YouTube embeds know this is a TV
        s.setUserAgentString(s.getUserAgentString() + " BraviaYouTubeTV/1.0 AndroidTV");

        // Allow mixed HTTP/HTTPS content (thumbnail images)
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Cookies
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        // Hardware-accelerated rendering for smooth 1080p scrolling and video
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Background matches the dark YouTube TV theme
        webView.setBackgroundColor(0xFF0F0F0F);

        // JavaScript-to-Java bridge
        webView.addJavascriptInterface(new AndroidBridge(this), "AndroidTV");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Keep all navigation inside the WebView
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectTVEnhancements();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage msg) {
                return true; // suppress in production
            }
        });
    }

    /**
     * Injected after page load — adds D-pad focus rings and TV flags.
     */
    private void injectTVEnhancements() {
        webView.loadUrl("javascript:(function(){"
            // Flag the page so it knows it's inside the Android app
            + "window.IS_ANDROID_TV=true;"
            // Red focus ring on all interactive elements for D-pad navigation
            + "var st=document.createElement('style');"
            + "st.textContent='*:focus{outline:3px solid #FF0000!important;outline-offset:2px;border-radius:4px}';"
            + "document.head.appendChild(st);"
            // Make all cards and buttons focusable via D-pad
            + "document.querySelectorAll('.vc,.chip,.si,.tn,.icbtn,.cb,.srback,.avatar,.ebtn').forEach(function(el){"
            + "if(!el.getAttribute('tabindex'))el.setAttribute('tabindex','0');});"
            + "})();");
    }

    /**
     * Map Android TV remote control hardware keys to in-page actions.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {

            case KeyEvent.KEYCODE_BACK:
                webView.loadUrl("javascript:"
                    + "if(document.getElementById('plmodal').classList.contains('open'))closePlayer();"
                    + "else if(document.getElementById('srp').classList.contains('open'))closeSRP();"
                    + "else if(document.getElementById('so').classList.contains('open'))"
                    + "  document.getElementById('so').classList.remove('open');"
                    + "else if(window.history.length>1)window.history.back();");
                return true;

            case KeyEvent.KEYCODE_SEARCH:
                webView.loadUrl("javascript:startSearch();");
                return true;

            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                webView.loadUrl("javascript:togglePP();");
                return true;

            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
            case KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD:
                webView.loadUrl("javascript:skip(10);");
                return true;

            case KeyEvent.KEYCODE_MEDIA_REWIND:
            case KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD:
                webView.loadUrl("javascript:skip(-10);");
                return true;

            case KeyEvent.KEYCODE_MEDIA_STOP:
                webView.loadUrl("javascript:closePlayer();");
                return true;

            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onBackPressed() {
        // Handled via onKeyDown — do nothing here to prevent default exit
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
        // Re-apply immersive mode (some TVs reset it on resume)
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }

    /**
     * JavaScript bridge — HTML can call AndroidTV.method() for native features.
     */
    public static class AndroidBridge {
        private final Activity activity;

        AndroidBridge(Activity a) { this.activity = a; }

        @JavascriptInterface
        public void showToast(String msg) {
            activity.runOnUiThread(() ->
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show());
        }

        @JavascriptInterface
        public String getDeviceModel() { return android.os.Build.MODEL; }

        @JavascriptInterface
        public boolean isAndroidTV() { return true; }

        @JavascriptInterface
        public int getScreenWidth() {
            return activity.getResources().getDisplayMetrics().widthPixels;
        }

        @JavascriptInterface
        public int getScreenHeight() {
            return activity.getResources().getDisplayMetrics().heightPixels;
        }
    }
}
