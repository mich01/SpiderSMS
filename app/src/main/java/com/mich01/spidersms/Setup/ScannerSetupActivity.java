package com.mich01.spidersms.Setup;


import static com.mich01.spidersms.Setup.SetupConfig.readScan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mich01.spidersms.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class ScannerSetupActivity extends AppCompatActivity {
    CodeScanner codeScanner;
    CodeScannerView scannerview;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);
        scannerview = findViewById(R.id.scannerView);
        FloatingActionButton backButton = findViewById(R.id.cmdBack);
        codeScanner = new CodeScanner(this, scannerview);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        codeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            try {
                readScan(ScannerSetupActivity.this,new JSONObject(result.toString()));
            } catch (JSONException e) {
                Toast.makeText(ScannerSetupActivity.this, R.string.config_data_corrupted, Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }));
        scannerview.setOnClickListener(v -> codeScanner.startPreview());
        backButton.setOnClickListener(v -> finish());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        requestCameraPermission();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)  != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.CAMERA},1111);
        }
        else
        {
            codeScanner.startPreview();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}