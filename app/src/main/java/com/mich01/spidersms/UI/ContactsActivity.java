package com.mich01.spidersms.UI;

import static com.mich01.spidersms.Setup.SetupConfig.ReadScan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.mich01.spidersms.Adapters.ChatsAdapter;
import com.mich01.spidersms.Adapters.ContactAdapter;
import com.mich01.spidersms.Backend.GetPhoneContacts;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.Data.Contact;
import com.mich01.spidersms.Data.LastChat;
import com.mich01.spidersms.R;
import com.mich01.spidersms.Setup.ScannerSetupActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ContactsActivity extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    static ListView ContactListView;
    public static ContactAdapter adapter;
    TextView StatusStext;
    ProgressBar progressBar;
    EditText SearchText;
    public static ArrayList<Contact> Contacts = new ArrayList<>();
    static ProgressDialog dialog;

    ActivityResultLauncher<Intent> gerConfigBMPFile = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();
                if (data == null || data.getData() == null) {
                    SnackBarAlert("You Haven't selected any file");
                    return;
                }
                Uri uri = data.getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap == null) {
                        SnackBarAlert("You haven't selected any file");
                        return;
                    }
                    int width = bitmap.getWidth(), height = bitmap.getHeight();
                    int[] pixels = new int[width * height];
                    bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                    bitmap.recycle();
                    RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                    BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
                    MultiFormatReader reader = new MultiFormatReader();
                    try {
                        Result results = reader.decode(bBitmap);
                        JSONObject ResultsJSON = new JSONObject(results.getText());
                        ReadScan(ContactsActivity.this, ResultsJSON);
                    } catch (NotFoundException | JSONException e) {
                        SnackBarAlert("You need to select a QR Code bitmap");
                    }
                } catch (FileNotFoundException e) {
                    SnackBarAlert("Cannot open file");
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        ContactListView = findViewById(R.id.contacts_list);
        progressBar = findViewById(R.id.contacts_progressBar);
        StatusStext = findViewById(R.id.lbl_contact_Status);
        SwipeRefreshLayout swipeRefreshLayout;
        swipeRefreshLayout = findViewById(R.id.contactRefreshLayout);

        new GetPhoneContacts().getMyContacts(ContactsActivity.this);
        if(Contacts.size()==0)
        {
            SnackBarAlert(getString(R.string.drag_to_update_contact));
        }
        adapter = new ContactAdapter(ContactsActivity.this,R.layout.contact_item,Contacts);
        ContactListView.setAdapter(adapter);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.contacts_view_title)+ " ("+Contacts.size()+")");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ContactListView.setOnItemClickListener((parent, view, position, id) -> {
        });
        // Implementing setOnRefreshListener on SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
            dialog = ProgressDialog.show(ContactsActivity.this, "Updating Contacts",
                    getString(R.string.updating_contacts), true);
            dialog.setIcon(R.drawable.update_24);
            // User defined method to shuffle the array list items
            PopulateContacts runningTask = new PopulateContacts();
            runningTask.execute();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contact_menu, menu);
        MenuItem.OnActionExpandListener onActionExpandListener = new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return false;
            }

        };
        menu.findItem(R.id.search_contact).setOnActionExpandListener(onActionExpandListener);
        menu.findItem(R.id.add_contact).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Button ScanQRButton;
                Button SelectFileButton;
                AlertDialog.Builder builder = new AlertDialog.Builder(ContactsActivity.this);
                ViewGroup viewGroup = findViewById(android.R.id.content);
                LayoutInflater inflater = ContactsActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.activity_config_choice, viewGroup, false);
                builder.setView(dialogView);
                ScanQRButton = dialogView.findViewById(R.id.cmdNavigateToQR);
                SelectFileButton = dialogView.findViewById(R.id.cmdFilechooser);
                AlertDialog alertDialog = builder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();
                ScanQRButton.setOnClickListener(v -> {
                    startActivity(new Intent(getApplicationContext(), ScannerSetupActivity.class));
                    alertDialog.dismiss();
                });
                SelectFileButton.setOnClickListener(v -> {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1111);
                    } else {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        gerConfigBMPFile.launch(intent);
                        alertDialog.dismiss();
                    }
                });
                return false;
            }
        });
        SearchView searchView= (SearchView) menu.findItem(R.id.search_contact).getActionView();
        SearchText = findViewById(R.id.search_contact);
        searchView.setQueryHint("Search Contact..");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                FilterContacts(query);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
    @SuppressLint("StaticFieldLeak")
    public static class PopulateContacts extends AsyncTask<Void, Void, String> {
        @Override protected String doInBackground(Void... params)
        {
            new GetPhoneContacts().getPhoneContacts(adapter.getContext());
            return "Processing";
        }
        @Override protected void onPostExecute(String result)
        {
            dialog.dismiss();
            adapter = new ContactAdapter(adapter.getContext(), R.layout.contact_item,Contacts);
            ContactListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            Snackbar snackbar = Snackbar.make(ContactListView, "Contacts Updated", Snackbar.LENGTH_LONG);
            snackbar.setBackgroundTint(Color.GREEN);
            snackbar.setTextColor(Color.BLACK);
            snackbar.show();
        }
    }

    public static void RepopulateContacts(Context context){
        Handler h = new Handler(context.getMainLooper());
        h.post(new Runnable() {
            @SuppressLint("Range")
            @Override
            public void run() {
                new GetPhoneContacts().getMyContacts(adapter.getContext());
                synchronized (this) {
                    adapter = new ContactAdapter(adapter.getContext(), R.layout.contact_item,Contacts);
                    ContactListView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    Snackbar snackbar = Snackbar.make(ContactListView, "Contacts Updated", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(Color.GREEN);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }
            }
        });

    }
    public void FilterContacts(String SearchString)
    {
        ArrayList<Contact> FilteredContacts = new ArrayList<>();
        for(int i=0;i<Contacts.size();i++)
        {
            if(Contacts.get(i).getContactNames().toLowerCase().contains(SearchString.toLowerCase()))
            {
                FilteredContacts.add(new Contact(Contacts.get(i).getCID(),
                    Contacts.get(i).getContactNames(), Contacts.get(i).getPubKey(), Contacts.get(i).getCType()));
            }
        }
        ContactAdapter Filteredadapter = new ContactAdapter(getApplicationContext(),R.layout.contact_item, FilteredContacts);
        ContactListView.setAdapter(Filteredadapter);
        Filteredadapter.notifyDataSetChanged();
    }
    public void SnackBarAlert(String AlertMessage)
    {
        Snackbar mSnackBar = Snackbar.make(findViewById(android.R.id.content), AlertMessage, Snackbar.LENGTH_LONG);
        TextView SnackBarView = (mSnackBar.getView()).findViewById(R.id.snackbar_text);
        SnackBarView.setTextColor(ContextCompat.getColor(ContactsActivity.this, R.color.white));
        SnackBarView.setBackgroundColor(ContextCompat.getColor(ContactsActivity.this, R.color.error));
        SnackBarView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        SnackBarView.setGravity(Gravity.CENTER_HORIZONTAL);
        mSnackBar.show();
    }
}