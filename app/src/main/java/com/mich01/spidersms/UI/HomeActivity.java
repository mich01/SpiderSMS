package com.mich01.spidersms.UI;

import static com.mich01.spidersms.Crypto.PKI_Cipher.SharePublicKey;
import static com.mich01.spidersms.Data.StringsConstants.CName;
import static com.mich01.spidersms.Data.StringsConstants.C_ID;
import static com.mich01.spidersms.Data.StringsConstants.Contact;
import static com.mich01.spidersms.Data.StringsConstants.ContactName;
import static com.mich01.spidersms.Data.StringsConstants.Data;
import static com.mich01.spidersms.Data.StringsConstants.HelloContact;
import static com.mich01.spidersms.Data.StringsConstants.MessageText;
import static com.mich01.spidersms.Data.StringsConstants.MyContact;
import static com.mich01.spidersms.Data.StringsConstants.PubKey;
import static com.mich01.spidersms.Data.StringsConstants.ReadStatus;
import static com.mich01.spidersms.Data.StringsConstants.Section;
import static com.mich01.spidersms.Data.StringsConstants.Timestamp;
import static com.mich01.spidersms.Data.StringsConstants.about_path;
import static com.mich01.spidersms.Data.StringsConstants.global_pref;
import static com.mich01.spidersms.Data.StringsConstants.privacy_path;
import static com.mich01.spidersms.Setup.SetupConfig.readScan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.view.WindowManager;
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
import com.mich01.spidersms.Receivers.AlarmReceiver;
import com.mich01.spidersms.Setup.ScannerSetupActivity;
import com.mich01.spidersms.services.MainService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.M)
public class HomeActivity extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    public static ListView chatListView;
    @SuppressLint("StaticFieldLeak")
    public static ChatsAdapter adapter;
    private static ArrayList<LastChat> chatsList;
    TextView statusText;
    ProgressBar progressBar;
    FloatingActionButton fab;
    ActivityResultLauncher<Intent> gerConfigBMPFile = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();
                if (data == null || data.getData() == null) {
                    snackBarAlert(getString(R.string.havent_selected_file));
                    return;
                }
                Uri uri = data.getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap == null) {
                        snackBarAlert(getString(R.string.havent_selected_file));
                        return;
                    }
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    int[] pixels = new int[width * height];
                    bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                    bitmap.recycle();
                    RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                    BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
                    MultiFormatReader reader = new MultiFormatReader();
                    try {
                        Result results = reader.decode(bBitmap);
                        JSONObject resultsJSON = new JSONObject(results.getText());
                        readScan(HomeActivity.this, resultsJSON);
                    } catch (NotFoundException | JSONException e) {
                        snackBarAlert(getString(R.string.need_qr_bitmap));
                    }
                } catch (FileNotFoundException e) {
                    snackBarAlert(getString(R.string.cannot_open_file));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_home);
        chatListView = findViewById(R.id.chats_list);
        progressBar = findViewById(R.id.chats_progressBar);
        statusText = findViewById(R.id.lbl_contact_Status);
        Intent alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, alarmIntent, PendingIntent.FLAG_MUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+ (60  * 1000), pendingIntent);
        Objects.requireNonNull(this.getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        this.getSupportActionBar().setCustomView(R.layout.main_action_bar);
        adapter = new ChatsAdapter(HomeActivity.this, R.layout.chat_list_item, chatsList);
        populateChats(this);
        adapter.notifyDataSetChanged();
        fab = findViewById(R.id.fab_chat);
        fab.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1111);
                }
            } else {
                startActivity(new Intent(getApplicationContext(), ContactsActivity.class));
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, MainService.class));
        }
        else
        {
            startService(new Intent(this, MainService.class));
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS}, 1111);
            }
        } else {
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
            SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
            searchView.setQueryHint(getString(R.string.search_contacts));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    filterChats(query);
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
        Intent browserIntent = new Intent(this,AboutActivity.class);
        switch (item.getItemId())
        {
            case R.id.send_invite:
                Intent sendIntent = new Intent();
                sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_app));
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_app_link));
                startActivity(shareIntent);
                break;
            case R.id.add_contact:
            case R.id.reconfigure:
                Button scanQRButton;
                Button selectFileButton;
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                ViewGroup viewGroup = findViewById(android.R.id.content);
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.activity_config_choice, viewGroup, false);
                builder.setView(dialogView);
                scanQRButton = dialogView.findViewById(R.id.cmdNavigateToQR);
                selectFileButton = dialogView.findViewById(R.id.cmdFilechooser);
                AlertDialog alertDialog = builder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();
                scanQRButton.setOnClickListener(v -> {
                    startActivity(new Intent(getApplicationContext(), ScannerSetupActivity.class));
                    alertDialog.dismiss();
                });
                selectFileButton.setOnClickListener(v -> {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1111);
                    } else {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        gerConfigBMPFile.launch(intent);
                        alertDialog.dismiss();
                    }
                });
                break;
            case R.id.share_contact:
                SharedPreferences preferences = this.getSharedPreferences(global_pref, Context.MODE_PRIVATE);
                JSONObject contactJson = new JSONObject();
                try {
                    contactJson.put(Data, HelloContact);
                    contactJson.put(C_ID, preferences.getString(MyContact, null));
                    contactJson.put(CName, preferences.getString(ContactName, null));
                    contactJson.put(PubKey, SharePublicKey());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent qrIntent = new Intent(this, DataQRGenerator.class);
                qrIntent.putExtra(Contact, contactJson.toString());
                qrIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(qrIntent);
                break;
            case R.id.terms_conditions:
                browserIntent.putExtra(Section,getString(R.string.t_n_c));
                browserIntent.putExtra("URL",privacy_path);
                startActivity(browserIntent);
                break;
            case R.id.about_app:
                browserIntent.putExtra(Section,getString(R.string.about_spidersms));
                browserIntent.putExtra("URL",about_path);
                startActivity(browserIntent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public void populateChats(Context context) {
        chatsList = new ArrayList<>();
        Handler h = new Handler(context.getMainLooper());
        h.post(new Runnable() {
            @SuppressLint("Range")
            @Override
            public void run() {
                Cursor cur = new DBManager(context).getLastChatList();
                while (cur != null && cur.moveToNext()) {
                    if (cur.getString(cur.getColumnIndex(ContactName)) == null) {
                        chatsList.add(new LastChat(cur.getString(cur.getColumnIndex(C_ID)),
                                cur.getString(cur.getColumnIndex(C_ID)),
                                cur.getString(cur.getColumnIndex(MessageText)),
                                cur.getString(cur.getColumnIndex(Timestamp)),
                                cur.getInt(cur.getColumnIndex(ReadStatus))));
                    } else {
                        chatsList.add(new LastChat(cur.getString(cur.getColumnIndex(C_ID)),
                                cur.getString(cur.getColumnIndex(ContactName)),
                                cur.getString(cur.getColumnIndex(MessageText)),
                                cur.getString(cur.getColumnIndex(Timestamp)),
                                cur.getInt(cur.getColumnIndex(ReadStatus))));
                    }
                }
                synchronized (this) {
                    ChatsAdapter updatedChats = new ChatsAdapter(context, R.layout.chat_list_item, chatsList);
                    try {
                        chatListView.setAdapter(updatedChats);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }
    public static void rePopulateChats(Context context) {
        chatsList = new ArrayList<>();
        Handler h = new Handler(context.getMainLooper());
        h.post(new Runnable() {
            @SuppressLint("Range")
            @Override
            public void run() {
                Cursor cur = new DBManager(context).getLastChatList();
                while (cur != null && cur.moveToNext()) {
                    if (cur.getString(cur.getColumnIndex(ContactName)) == null) {
                        chatsList.add(new LastChat(cur.getString(cur.getColumnIndex(C_ID)),
                                cur.getString(cur.getColumnIndex(C_ID)),
                                cur.getString(cur.getColumnIndex(MessageText)),
                                cur.getString(cur.getColumnIndex(Timestamp)),
                                cur.getInt(cur.getColumnIndex(ReadStatus))));
                    } else {
                        chatsList.add(new LastChat(cur.getString(cur.getColumnIndex(C_ID)),
                                cur.getString(cur.getColumnIndex(ContactName)),
                                cur.getString(cur.getColumnIndex(MessageText)),
                                cur.getString(cur.getColumnIndex(Timestamp)),
                                cur.getInt(cur.getColumnIndex(ReadStatus))));
                    }
                }
                synchronized (this) {
                    ChatsAdapter updatedChats = new ChatsAdapter(context, R.layout.chat_list_item, chatsList);
                    try {
                        chatListView.setAdapter(updatedChats);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    public void filterChats(String searchString) {
        ArrayList<LastChat> filteredChatList = new ArrayList<>();
        for (int i = 0; i < chatsList.size(); i++) {
            if (chatsList.get(i).getContactName().toLowerCase().contains(searchString.toLowerCase()) | chatsList.get(i).getLastMessage().toLowerCase().contains(searchString.toLowerCase())) {
                filteredChatList.add(new LastChat(chatsList.get(i).getContactID(),
                        chatsList.get(i).getContactName(),
                        chatsList.get(i).getLastMessage(),
                        chatsList.get(i).getTimestamp(),
                        chatsList.get(i).getStatus()));
            }
        }
        ChatsAdapter updatedChats = new ChatsAdapter(HomeActivity.this, R.layout.chat_list_item, filteredChatList);
        chatListView.setAdapter(updatedChats);
    }

    public void snackBarAlert(String alertMessage) {
        Snackbar mSnackBar = Snackbar.make(findViewById(android.R.id.content), alertMessage, Snackbar.LENGTH_LONG);
        TextView snackBarView = (mSnackBar.getView()).findViewById(R.id.snackbar_text);
        snackBarView.setTextColor(ContextCompat.getColor(HomeActivity.this, R.color.white));
        snackBarView.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.error));
        snackBarView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snackBarView.setGravity(Gravity.CENTER_HORIZONTAL);
        mSnackBar.show();
    }
}