package com.mich01.spidersms.UI;


import static com.mich01.spidersms.Data.StringsConstants.AppName;
import static com.mich01.spidersms.Data.StringsConstants.C_ID;
import static com.mich01.spidersms.Data.StringsConstants.Contact;
import static com.mich01.spidersms.Data.StringsConstants.ContactName;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
    TextView contactName;
    Button deleteContact;
    ImageView qRImage;
    Button shareQRContact;
    String inputValue;
    Bitmap bitmap;
    QRGEncoder qrgEncoder;
    JSONObject contactJSON;
    String contactID;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_qrgenerator);
        contactName = findViewById(R.id.txt_contact_shared);
        deleteContact = findViewById(R.id.cmdDeleteContact);
        qRImage = findViewById(R.id.img_contact_qr);
        shareQRContact = findViewById(R.id.cmd_share_qr);
        context = DataQRGenerator.this;
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Bundle bundle = getIntent().getExtras();
        inputValue = bundle.getString(Contact);
        contactName.setText(bundle.getString(ContactName));
        try {
            contactJSON = new JSONObject(inputValue);
            contactID = contactJSON.getString(C_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(bundle.getString(ContactName)==null)
        {
            contactName.setText(R.string.my_contact);
            deleteContact.setVisibility(View.GONE);
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
                qRImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, getString(R.string.value_required),Toast.LENGTH_LONG).show();
        }
        shareQRContact.setOnClickListener(v -> {
            try {
                String tempFile = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,contactID, contactID);
                Uri bmpUri = Uri.parse(tempFile);
                final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                shareIntent.setType("image/png");
                startActivity(Intent.createChooser(shareIntent, ContactName));
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            finally {
                String tempFile = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,AppName+"-", contactID);
                Uri bmpUri = Uri.parse(tempFile);
                final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                shareIntent.setType("image/png");
                startActivity(Intent.createChooser(shareIntent, ContactName));
            }

        });
        deleteContact.setOnClickListener(v -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(v.getRootView().getContext());
            alert.setTitle(R.string.confirm_delete_contact);
            alert.setPositiveButton(R.string.yes, (dialog, whichButton) -> {
                new DBManager(getApplicationContext()).DeleteContact(contactID);
                    try {
                        HomeActivity.rePopulateChats(DataQRGenerator.this);
                        HomeActivity.adapter.notifyDataSetChanged();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }finally {
                        finish();
                    }
            });
            alert.setNegativeButton(R.string.cancel,
                    (dialog, whichButton) -> dialog.cancel());
            alert.show();
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