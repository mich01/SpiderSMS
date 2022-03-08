package com.mich01.spidersms.UI;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

@RequiresApi(api = Build.VERSION_CODES.R)
public class DataQRGenerator extends AppCompatActivity {
    TextView ContactName;
    Button DeleteContact;
    ImageView QRImage;
    Button ShareQRContact;
    String inputValue;
    Bitmap bitmap;
    QRGEncoder qrgEncoder;
    JSONObject ContactJSON;
    String ContactID;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_qrgenerator);
        ContactName = findViewById(R.id.txt_contact_shared);
        DeleteContact = findViewById(R.id.cmdDeleteContact);
        QRImage = findViewById(R.id.img_contact_qr);
        ShareQRContact = findViewById(R.id.cmd_share_qr);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Bundle bundle = getIntent().getExtras();
        inputValue = bundle.getString("Contact");
        ContactName.setText(bundle.getString("ContactName"));
        try {
            ContactJSON = new JSONObject(inputValue);
            ContactID = ContactJSON.getString("CID");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //((ContactsActivity)getApplicationContext()).finish();
        if(bundle.getString("ContactName")==null)
        {
            ContactName.setText(R.string.my_contact);
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
            int smallerDimension = Math.min(width, height);
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
        ShareQRContact.setOnClickListener(v -> {
            String tempFile = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,ContactID, null);
            Uri bmpUri = Uri.parse(tempFile);
            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.setType("image/png");
            startActivity(Intent.createChooser(shareIntent, ContactName.toString()));
        });
        DeleteContact.setOnClickListener(v -> {
            Log.i("Contact..",inputValue);
            new DBManager(getApplicationContext()).DeleteContact(ContactID);
            {
                startActivity(new Intent(getApplicationContext(), ContactsActivity.class));
                HomeActivity.PopulateChats(DataQRGenerator.this);
                HomeActivity.adapter.notifyDataSetChanged();
                finish();
            }
        });
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