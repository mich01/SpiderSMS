package com.mich01.spidersms.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import com.mich01.spidersms.R;

import java.util.Objects;

public class AboutActivity extends AppCompatActivity {

    WebView contentView;
    Bundle browserBundles;
    String browserURL;
    String content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        browserBundles = getIntent().getExtras();
        browserURL = browserBundles.getString("URL");
        content = browserBundles.getString("Section");
        contentView = findViewById(R.id.about_webview);
        contentView.getSettings().setJavaScriptEnabled(false);
        setTitle(content);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        contentView.loadUrl(browserURL);
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