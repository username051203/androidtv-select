# ProGuard rules for BraviaYouTubeTV
# Keep WebView JavaScript interface methods
-keepclassmembers class com.braviatv.app.TVActivity$AndroidBridge {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface
