package com.mich01.spidersms.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import com.mich01.spidersms.R;

import java.util.Objects;

public class AboutActivity extends AppCompatActivity {

    WebView ContentView;
    Bundle BrowserBundles;
    String BrowserURL;
    String Content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        BrowserBundles = getIntent().getExtras();
        BrowserURL = BrowserBundles.getString("URL");
        Content = BrowserBundles.getString("Section");
        ContentView = findViewById(R.id.about_webview);
        ContentView.getSettings().setJavaScriptEnabled(false);
        setTitle(Content);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        ContentView.loadUrl(BrowserURL);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        finish();
    }
}