package com.mich01.spidersms.UI;

import android.app.ProgressDialog;
import android.os.AsyncTask;
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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.mich01.spidersms.Adapters.ContactAdapter;
import com.mich01.spidersms.Backend.GetPhoneContacts;
import com.mich01.spidersms.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity {
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


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        ContactListView = findViewById(R.id.contacts_list);
        progressBar = findViewById(R.id.contacts_progressBar);
        StatusStext = findViewById(R.id.lbl_contact_Status);
        new GetPhoneContacts().getMyContacts(this.getApplicationContext());
        adapter = new ContactAdapter(this.getApplicationContext(),CID,ContactNames,ContactStatus, ContactImgs,CType);
        ContactListView.setAdapter(adapter);
        this.getSupportActionBar().setTitle(String.valueOf("Contacts"));
        Handler h = new Handler(getMainLooper());
        h.post(new Runnable()
        {
            @Override
            public void run()
            {
                PopulateContacts populateContacts = new PopulateContacts(ContactsActivity.this);
                populateContacts.execute();
                synchronized(this)
                {
                    dialog = ProgressDialog.show(ContactsActivity.this, "",
                            "Loading. Please wait...: ", true);
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ContactListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.i("onCreate", "menu");
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
        SearchView searchView= (SearchView) menu.findItem(R.id.search_contact).getActionView();
        SearchText = findViewById(R.id.search_contact);
        searchView.setQueryHint("Search Contact..");
        CharSequence query = searchView.getQuery();
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

    public static class PopulateContacts extends AsyncTask<String, String, String>
    {
        WeakReference<ContactsActivity> activityWeakReference;
        PopulateContacts(ContactsActivity activity)
        {
            activityWeakReference = new WeakReference<ContactsActivity>(activity);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ContactsActivity activity = activityWeakReference.get();
            if(activity==null|| activity.isFinishing())
            {
                return;
            }

        }
        @Override
        protected String doInBackground(String... strings)
        {

            ContactsActivity activity = activityWeakReference.get();
            if(activity==null|| activity.isFinishing())
            {
                return "null";
            }
            new GetPhoneContacts().getPhoneContacts(activity.getApplicationContext());
            return "populating ContactList";
        }
        @Override
        protected void onProgressUpdate(String... values) {ContactsActivity activity = activityWeakReference.get();
            super.onProgressUpdate(values);
        }
        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);
            ContactsActivity activity = activityWeakReference.get();
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