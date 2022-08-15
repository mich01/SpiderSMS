package com.mich01.spidersms.UI;


import static com.mich01.spidersms.Data.StringsConstants.AppName;
import static com.mich01.spidersms.Data.StringsConstants.C_ID;
import static com.mich01.spidersms.Data.StringsConstants.Contact;
import static com.mich01.spidersms.Data.StringsConstants.ContactName;
import static com.mich01.spidersms.Data.StringsConstants.KeyConfirmTrigger;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;



@RequiresApi(api = Build.VERSION_CODES.R)
public class DataQRGenerator extends AppCompatActivity {
    TextView contactName;
    Button deleteContact;
    ImageView qRImage;
    Button shareQRContact;
    String inputValue;
    Bitmap bitmap;
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
        } catch (JSONException ignored) {}
        if(bundle.getString(ContactName)==null)
        {
            contactName.setText(R.string.my_contact);
            deleteContact.setVisibility(View.GONE);
        }
        if (inputValue.length() > 0)
        {
            //Encode with a QR Code image
            generateQR(inputValue);
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
            }catch (Exception ignored){}
            finally {
                String tempFile = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,AppName+KeyConfirmTrigger, contactID);
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
                    }catch (Exception ignored){}
                    finally {
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

    private void generateQR(String ContactValue) {
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix matrix = writer.encode(ContactValue, BarcodeFormat.QR_CODE, 600, 600);
            BarcodeEncoder encoder = new BarcodeEncoder();
            bitmap = encoder.createBitmap(matrix);
            //set data image to imageview
            qRImage.setImageBitmap(bitmap);

        } catch (WriterException ignored){}
    }
}