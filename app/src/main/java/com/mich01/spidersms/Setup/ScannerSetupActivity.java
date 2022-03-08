package com.mich01.spidersms.Setup;


import static com.mich01.spidersms.Setup.SetupConfig.ReadScan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mich01.spidersms.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class ScannerSetupActivity extends AppCompatActivity {
    CodeScanner codeScanner;
    CodeScannerView Scannerview;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);
        Scannerview = findViewById(R.id.scannerView);
        FloatingActionButton backButton = findViewById(R.id.cmdBack);
        codeScanner = new CodeScanner(this, Scannerview);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        codeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            try {
                ReadScan(ScannerSetupActivity.this,new JSONObject(result.toString()));
            } catch (JSONException e) {
                Toast.makeText(ScannerSetupActivity.this, "Config Data Corrupted!!", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }));
        Scannerview.setOnClickListener(v -> codeScanner.startPreview());
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)  != PackageManager.PERMISSION_GRANTED)
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