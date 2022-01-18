package com.mich01.spidersms.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.mich01.spidersms.R;
import com.mich01.spidersms.Setup.ConfigChoiceActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {
    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        fab = findViewById(R.id.fab_chat);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS)  != PackageManager.PERMISSION_GRANTED)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},1111);
                    }
                }
                else
                {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    startActivity(new Intent(getApplicationContext(), ContactsActivity.class));
                }

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i("onCreate", "menu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.add_contact:
                startActivity(new Intent(this, ConfigChoiceActivity.class));
                break;
            case  R.id.reconfigure:
                startActivity(new Intent(this, ConfigChoiceActivity.class));
                break;
            case  R.id.share_contact:
                //new XMPPConnection(this).createNewUser();
                //Log.i("Contact Domain",MyContact);
                SharedPreferences preferences = this.getSharedPreferences("global", Context.MODE_PRIVATE);
                JSONObject ContactJson = new JSONObject();
                try {
                    ContactJson.put("Data","HelloContact");
                    ContactJson.put("CID", "254724724008");
                    ContactJson.put("CName", preferences.getString("PublicName",null));
                    ContactJson.put("PubKey", preferences.getString("ServerPubKey",null));
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
}