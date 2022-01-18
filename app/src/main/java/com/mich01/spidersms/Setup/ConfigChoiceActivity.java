package com.mich01.spidersms.Setup;


import static com.mich01.spidersms.Setup.SetupConfig.ReadScan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import com.mich01.spidersms.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

public class ConfigChoiceActivity extends AppCompatActivity {
    private static final int FILE_SELECT_CODE = 0;
    private static final String TAG = "App";
    private Button ScanQRButton;
    private Button SelectFileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_choice);
        ScanQRButton = findViewById(R.id.cmdNavigateToQR);
        SelectFileButton = findViewById(R.id.cmdFilechooser);
        ScanQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ScannerSetupActivity.class));
                finish();
            }
        });
        SelectFileButton.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1111);
                }
                else
                {
                    showFileChooser();
                }
            }
        });


    }
    private void showFileChooser()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK)
                {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d(TAG, "File Uri: " + uri.toString());
                    // Get the path
                    /*String path = null;
                    try {
                        path = FileUtils.getPath(this, uri);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }*/
                    StringBuilder text = new StringBuilder();

                    /*try {
                        BufferedReader br = new BufferedReader(new FileReader(path));
                        String line;

                        while ((line = br.readLine()) != null) {
                            text.append(line);
                            text.append('\n');
                        }
                        br.close();
                        try {
                            ReadScan(this,new JSONObject(text.toString()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        fileList();
                    }
                    catch (IOException e) {
                        //You'll need to add proper error handling here
                    }*/
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}