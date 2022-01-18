package com.mich01.spidersms.UI;



import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;

@RequiresApi(api = Build.VERSION_CODES.R)
public class DataQRGenerator extends AppCompatActivity {
    String TAG = "SpiderMS";
    TextView ContactQR;
    TextView ContactName;
    Button DeleteContact;
    ImageView QRImage;
    Button SaveQR;
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
        SaveQR = findViewById(R.id.cmd_cave_qr);
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
        SaveQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean save;
                String result;
                try
                {
                    File ContactFile = new File("SpiderSMContactQR.bmp");
                    if(!ContactFile.exists())
                    {
                            ContactFile.createNewFile();
                    }
                    //save = new QRGSaver().save( "SpiderSMContactQR", bitmap, QRGContents.ImageType.IMAGE_JPEG);
                    //result = save ? "Image Saved" : "Image Not Saved";
                    //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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