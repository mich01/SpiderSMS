package com.mich01.spidersms.UI;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.mich01.spidersms.Adapters.ContactAdapter;
import com.mich01.spidersms.Backend.GetPhoneContacts;
import com.mich01.spidersms.Data.Contact;
import com.mich01.spidersms.R;

import java.util.ArrayList;
import java.util.Objects;

public class ContactsActivity extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    static ListView ContactListView;
    public ContactAdapter adapter;
    TextView StatusStext;
    ProgressBar progressBar;
    EditText SearchText;
    public static ArrayList<Contact> Contacts = new ArrayList<>();
    static ProgressDialog dialog;


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
    public class PopulateContacts extends AsyncTask<Void, Void, String> {
        @Override protected String doInBackground(Void... params)
        {
            new GetPhoneContacts().getPhoneContacts(ContactsActivity.this);
            return "Processing";
        }
        @Override protected void onPostExecute(String result)
        {
            dialog.dismiss();
            adapter = new ContactAdapter(ContactsActivity.this, R.layout.contact_item,Contacts);
            ContactListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            Snackbar snackbar = Snackbar.make(ContactListView, "Contacts Updated", Snackbar.LENGTH_LONG);
            snackbar.setBackgroundTint(Color.GREEN);
            snackbar.setTextColor(Color.BLACK);
            snackbar.show();
        }
    }

    public void FilterContacts(String SearchString)
    {
        ArrayList<Contact> FilteredContacts = new ArrayList<>();
        for(int i=0;i<Contacts.size();i++)
        {
            if(Contacts.get(i).getContactNames().toLowerCase().contains(SearchString.toLowerCase()))
            {
                Log.i("Filtered",SearchString);
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