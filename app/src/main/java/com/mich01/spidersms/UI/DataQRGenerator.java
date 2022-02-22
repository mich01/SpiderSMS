package com.mich01.spidersms.UI;



import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

@RequiresApi(api = Build.VERSION_CODES.R)
public class DataQRGenerator extends AppCompatActivity {
    String TAG = "SpiderMS";
    TextView ContactQR;
    TextView ContactName;
    Button DeleteContact;
    ImageView QRImage;
    Button ShareQRContact;
    String inputValue;
    Bitmap bitmap;
    QRGEncoder qrgEncoder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_qrgenerator);
        ContactQR = findViewById(R.id.txt_contact_shared);
        ContactName = findViewById(R.id.txt_contact_shared);
        DeleteContact = findViewById(R.id.cmdDeleteContact);
        QRImage = findViewById(R.id.img_ContactQR);
        ShareQRContact = findViewById(R.id.cmd_share_qr);
        Bundle bundle = getIntent().getExtras();
        inputValue = bundle.getString("Contact");
        ContactName.setText(bundle.getString("ContactName"));
        //((ContactsActivity)getApplicationContext()).finish();
        if(bundle.getString("ContactName")==null)
        {
            ContactName.setText("My Contact");
            DeleteContact.setVisibility(View.GONE);
        }
        if (inputValue.length() > 0)
        {
            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallerDimension = width < height ? width : height;
            smallerDimension = smallerDimension * 3 / 4;
            qrgEncoder = new QRGEncoder(
                    inputValue, null,
                    QRGContents.Type.TEXT,
                    smallerDimension);
            qrgEncoder.setColorBlack(Color.BLACK);
            qrgEncoder.setColorWhite(Color.WHITE);
            try {
                bitmap = qrgEncoder.getBitmap();
                QRImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Value Required ",Toast.LENGTH_LONG).show();
        }
        ShareQRContact.setOnClickListener(new View.OnClickListener() {
            String tempFile = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,"MyContact", null);
            @Override
            public void onClick(View v) {
                Uri bmpUri = Uri.parse(tempFile);
                final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                shareIntent.setType("image/png");
                startActivity(Intent.createChooser(shareIntent, ContactName.toString()));
            }
        });
        DeleteContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(new DBManager(getApplicationContext()).DeleteContact(bundle.getString("ContactID"))==0);
                {
                    startActivity(new Intent(getApplicationContext(), ContactsActivity.class));
                    finish();
                }
            }
        });
    }
}