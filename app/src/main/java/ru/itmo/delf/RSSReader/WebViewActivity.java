package ru.itmo.delf.RSSReader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

public class WebViewActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view);
        Intent intent = getIntent();
        String summary = intent.getStringExtra("summary");
        String title = intent.getStringExtra("title");
        String link = intent.getStringExtra("link");
        summary = "<p><a href=\"" + link + "\">Direct link</a></p><h3>" + title + "</h3>" + summary;
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.loadDataWithBaseURL(null, summary, "text/html", "utf-8", null);
    }
}
