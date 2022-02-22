package com.mich01.spidersms.UI;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mich01.spidersms.Adapters.ContactAdapter;
import com.mich01.spidersms.Backend.GetPhoneContacts;
import com.mich01.spidersms.R;
import com.mich01.spidersms.Setup.ConfigChoiceActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    //Temp
    public static ListView ContactListView;
    public static ContactAdapter adapter;
    TextView StatusStext;
    ProgressBar progressBar;
    EditText SearchText;
    public static ArrayList<String> CID = new ArrayList<String>();
    public static ArrayList<String> ContactNames = new ArrayList<String>();
    public static ArrayList<String> ContactStatus = new ArrayList<String>();
    public static ArrayList<Integer> ContactImgs = new ArrayList<Integer>();
    public static ArrayList<Integer> CType = new ArrayList<Integer>();
    static ProgressDialog dialog;
    //Temp-End
    FloatingActionButton fab;
    //private ActivityHomeBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ContactListView = findViewById(R.id.chats_list);
        progressBar = findViewById(R.id.chats_progressBar);
        StatusStext = findViewById(R.id.lbl_contact_Status);
        new GetPhoneContacts().getMyContacts(this.getApplicationContext());
        adapter = new ContactAdapter(this.getApplicationContext(),CID,ContactNames,ContactStatus, ContactImgs,CType);
        ContactListView.setAdapter(adapter);
        Handler h = new Handler(getMainLooper());
       /* h.post(new Runnable()
        {
            @Override
            public void run()
            {
                PopulateContacts populateContacts = new HomeActivity.PopulateContacts(HomeActivity.this);
                populateContacts.execute();
                synchronized(this)
                {
                    dialog = ProgressDialog.show(HomeActivity.this, "",
                            "Loading. Please wait...: ", true);
                }
            }
        });*/
        ContactListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
            }
        });
        fab = findViewById(R.id.fab_chat);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
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

    @RequiresApi(api = Build.VERSION_CODES.R)
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
    public static class PopulateContacts extends AsyncTask<String, String, String>
    {
        WeakReference<HomeActivity> activityWeakReference;
        PopulateContacts(HomeActivity activity)
        {
            activityWeakReference = new WeakReference<HomeActivity>(activity);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            HomeActivity activity = activityWeakReference.get();
            if(activity==null|| activity.isFinishing())
            {
                return;
            }

        }
        @Override
        protected String doInBackground(String... strings)
        {

            HomeActivity activity = activityWeakReference.get();
            if(activity==null|| activity.isFinishing())
            {
                return "null";
            }
            new GetPhoneContacts().getPhoneContacts(activity.getApplicationContext());
            return "populating ContactList";
        }
        @Override
        protected void onProgressUpdate(String... values) {HomeActivity activity = activityWeakReference.get();
            super.onProgressUpdate(values);
        }
        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);
            HomeActivity activity = activityWeakReference.get();
            if(activity==null|| activity.isFinishing())
            {
                return;
            }
            adapter = new ContactAdapter(activity.getApplicationContext(),CID,ContactNames,ContactStatus, ContactImgs,CType);
            dialog.dismiss();
            ContactListView.setAdapter(adapter);
        }
    }
    public void FilterContacts(String SearchString)
    {
        ArrayList<String> FilteredContactID = new ArrayList<String>();
        ArrayList<String> FilteredContactName = new ArrayList<String>();
        ArrayList<String> FilteredContactStatus = new ArrayList<String>();
        ArrayList<Integer> FilteredContactImg = new ArrayList<Integer>();
        ArrayList<Integer> FilteredCType = new ArrayList<Integer>();
        for(int i=0;i<ContactNames.size();i++)
        {
            if(ContactNames.get(i).toLowerCase().contains(SearchString.toLowerCase()))
            {
                FilteredContactID.add(CID.get(i));
                FilteredContactName.add(ContactNames.get(i));
                FilteredContactImg.add(ContactImgs.get(i));
                FilteredContactStatus.add(ContactStatus.get(i));
                FilteredCType.add(CType.get(i));
            }
        }
        ContactAdapter Filteredadapter = new ContactAdapter(getApplicationContext(),FilteredContactID,FilteredContactName,FilteredContactStatus, FilteredContactImg,FilteredCType);
        ContactListView.setAdapter(Filteredadapter);
    }
}