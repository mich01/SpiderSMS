package com.mich01.spidersms.UI;

import static com.mich01.spidersms.Setup.SetupConfig.ReadScan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.mich01.spidersms.Adapters.ChatsAdapter;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.Data.LastChat;
import com.mich01.spidersms.R;
import com.mich01.spidersms.Setup.ScannerSetupActivity;
import com.mich01.spidersms.services.MainService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
    //Temp
    @SuppressLint("StaticFieldLeak")
    public static ListView ChatListView;
    @SuppressLint("StaticFieldLeak")
    public static ChatsAdapter adapter;
    private static ArrayList<LastChat> ChatsList;
    TextView StatusText;
    ProgressBar progressBar;
    FloatingActionButton fab;
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
                        SnackBarAlert( "You haven't selected any file");
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
                        ReadScan(HomeActivity.this,ResultsJSON);
                    } catch (NotFoundException | JSONException e) {
                        SnackBarAlert("You need to select a QR Code bitmap");
                    }
                } catch (FileNotFoundException e) {
                    SnackBarAlert("Cannot open file");
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ChatListView = findViewById(R.id.chats_list);
        progressBar = findViewById(R.id.chats_progressBar);
        StatusText = findViewById(R.id.lbl_contact_Status);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, MainService.class));
        }else
        {
            startService(new Intent(this, MainService.class));

        }
        Objects.requireNonNull(this.getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        this.getSupportActionBar().setCustomView(R.layout.main_action_bar);
        adapter = new ChatsAdapter(HomeActivity.this,R.layout.chat_list_item,ChatsList);
        //new DBManager(getApplicationContext()).DeleteAllContacts("06");
        Handler h = new Handler(getMainLooper());
        h.post(() -> PopulateChats(HomeActivity.this));
        fab = findViewById(R.id.fab_chat);
        fab.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS)  != PackageManager.PERMISSION_GRANTED)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},1111);
                }
            }
            else
            {
                startActivity(new Intent(getApplicationContext(), ContactsActivity.class));
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECEIVE_SMS)  != PackageManager.PERMISSION_GRANTED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS},1111);
            }
        }
        else
        {
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
            menu.findItem(R.id.search).setOnActionExpandListener(onActionExpandListener);
            SearchView searchView= (SearchView) menu.findItem(R.id.search).getActionView();
            searchView.setQueryHint("Search Contact..");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query)
                {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    FilterChats(query);
                    return false;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.send_invite:
                Intent sendIntent = new Intent();
                sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_app));
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent, "Share App Link");
                startActivity(shareIntent);
                break;
            case R.id.add_contact:
            case  R.id.reconfigure:
                Button ScanQRButton;
                Button SelectFileButton;
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                ViewGroup viewGroup = findViewById(android.R.id.content);
                LayoutInflater inflater = this.getLayoutInflater();
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
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED)
                    {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1111);
                    }
                    else
                    {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        gerConfigBMPFile.launch(intent);
                        alertDialog.dismiss();
                    }
                });
                break;
            case  R.id.share_contact:
                SharedPreferences preferences = this.getSharedPreferences("global", Context.MODE_PRIVATE);
                JSONObject ContactJson = new JSONObject();
                try {
                    ContactJson.put("Data","HelloContact");
                    ContactJson.put("CID", "+"+preferences.getString("MyContact",null));
                    ContactJson.put("CName", preferences.getString("ContactName",null));
                    ContactJson.put("PubKey", preferences.getString("PublicKey",null));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent QRIntent = new Intent(this, DataQRGenerator.class);
                QRIntent.putExtra("Contact", ContactJson.toString());
                QRIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(QRIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public static void PopulateChats(Context context)
    {
        ChatsList = new ArrayList<>();
        Handler h = new Handler(context.getMainLooper());
        h.post(new Runnable()
        {
            @SuppressLint("Range")
            @Override
            public void run()
            {
                Cursor cur = new DBManager(context).getLastChatList();
                while (cur != null && cur.moveToNext())
                {
                    if(cur.getString(cur.getColumnIndex("ContactName"))==null)
                    {
                        ChatsList.add(new LastChat(cur.getString(cur.getColumnIndex("CID")),
                                cur.getString(cur.getColumnIndex("CID")),
                                cur.getString(cur.getColumnIndex("MessageText")),
                                cur.getString(cur.getColumnIndex("Timestamp")),
                                cur.getInt(cur.getColumnIndex("ReadStatus"))));
                    }
                    else {
                        ChatsList.add(new LastChat(cur.getString(cur.getColumnIndex("CID")),
                                cur.getString(cur.getColumnIndex("ContactName")),
                                cur.getString(cur.getColumnIndex("MessageText")),
                                cur.getString(cur.getColumnIndex("Timestamp")),
                                cur.getInt(cur.getColumnIndex("ReadStatus"))));
                    }
                }
                synchronized(this)
                {
                    ChatsAdapter UpdatedChats =new ChatsAdapter(context, R.layout.chat_list_item,ChatsList);
                    try {
                        ChatListView.setAdapter(UpdatedChats);
                    }catch (NullPointerException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });

    }
    public void FilterChats(String SearchString)
    {
        ArrayList<LastChat> FilteredChatList = new ArrayList<>();
        for(int i=0;i<ChatsList.size();i++)
        {
            if(ChatsList.get(i).getContactName().toLowerCase().contains(SearchString.toLowerCase()) | ChatsList.get(i).getLastMessage().toLowerCase().contains(SearchString.toLowerCase()))
            {
                FilteredChatList.add(new LastChat(ChatsList.get(i).getContactID(),
                        ChatsList.get(i).getContactName(),
                        ChatsList.get(i).getLastMessage(),
                        ChatsList.get(i).getTimestamp(),
                        ChatsList.get(i).getStatus()));
            }
        }
        ChatsAdapter UpdatedChats =new ChatsAdapter(HomeActivity.this, R.layout.chat_list_item,FilteredChatList);
        ChatListView.setAdapter(UpdatedChats);
    }
    public void SnackBarAlert(String AlertMessage)
    {
        Snackbar mSnackBar = Snackbar.make(findViewById(android.R.id.content), AlertMessage, Snackbar.LENGTH_LONG);
        TextView SnackBarView = (mSnackBar.getView()).findViewById(R.id.snackbar_text);
        SnackBarView.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.white));
        SnackBarView.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.error));
        SnackBarView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        SnackBarView.setGravity(Gravity.CENTER_HORIZONTAL);
        mSnackBar.show();
    }
}