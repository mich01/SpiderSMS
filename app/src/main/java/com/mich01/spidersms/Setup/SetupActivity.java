package com.mich01.spidersms.Setup;

import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;
import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefsEditor;
import static com.mich01.spidersms.Prefs.PrefsMgr.PREF_NAME;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.mich01.spidersms.Crypto.PKI_Cipher;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.R;

import org.json.JSONException;
import org.json.JSONObject;

public class SetupActivity extends AppCompatActivity {
    Button RegisterUserBtn;
    EditText txtPinNumber;
    EditText txtConfirmPinNumber;
    EditText txtUserName;
    EditText txtPhoneNumber;
    String Pin_Number;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        RegisterUserBtn = findViewById(R.id.cmd_complete);
        RegisterUserBtn.setEnabled(false);
        txtPinNumber = findViewById(R.id.txt_pin);
        txtConfirmPinNumber = findViewById(R.id.txt_confirm_pin);
        txtUserName = findViewById(R.id.txt_username);
        txtPhoneNumber = findViewById(R.id.txt_phone_number);
        txtPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_invalid_24, 0);
        txtConfirmPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_invalid_24, 0);
        txtConfirmPinNumber.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                txtPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_invalid_24, 0);
                txtConfirmPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_invalid_24, 0);
                RegisterUserBtn.setEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                if(!txtPinNumber.getText().toString().isEmpty() & !txtConfirmPinNumber.getText().toString().isEmpty() &
                        txtPinNumber.getText().toString().equals(txtConfirmPinNumber.getText().toString()))
                {
                    txtPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_match_24, 0);
                    txtConfirmPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_match_24, 0);
                    RegisterUserBtn.setEnabled(true);
                }
                else
                {
                    txtPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_invalid_24, 0);
                    txtConfirmPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_invalid_24, 0);
                    RegisterUserBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!txtPinNumber.getText().toString().isEmpty() & !txtConfirmPinNumber.getText().toString().isEmpty() &
                        txtPinNumber.getText().toString().equals(txtConfirmPinNumber.getText().toString()))
                {
                    txtPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_match_24, 0);
                    txtConfirmPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_match_24, 0);
                    RegisterUserBtn.setEnabled(true);
                }
                else
                {
                    txtPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_invalid_24, 0);
                    txtConfirmPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_invalid_24, 0);
                    RegisterUserBtn.setEnabled(false);
                }
            }
        });
        RegisterUserBtn.setOnClickListener(view -> {
            Pin_Number = txtPinNumber.getText().toString();
            if(SetKeyPin(Pin_Number))
            {
                CheckAllFields(view);
            }
            else
            {
                Snackbar snackbar;
                snackbar =Snackbar.make(view,"Pin Setup Failed",Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(SetupActivity.this, R.color.error));
                snackbar.show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean SetKeyPin(String Pin)
    {
        boolean completed;
        String PinHash = PKI_Cipher.ComputeHash(Pin);
        new DBManager(SetupActivity.this);
        //JSONObject KeyJSON = PKI_Cipher.PKI_CURVE_25519();
        MyPrefs = SetupActivity.this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        MyPrefsEditor = MyPrefs.edit();
        MyPrefsEditor.putString("MyPinHash", PinHash);
        MyPrefsEditor.putInt("SetupComplete", 0);
        MyPrefsEditor.apply();
        completed = MyPrefsEditor.commit();
        return completed;
    }

    private void CheckAllFields(View view) {
        if (txtUserName.length()==0)
        {
            txtUserName.setError("Username field is required");
        }
        if(txtPhoneNumber.length()==0)
        {
            txtPhoneNumber.setError("Phone Number field is required");
        }
        if(txtPinNumber.length()==0)
        {
            txtPinNumber.setError("Pin Is Required field is required");
        }
        if(txtPinNumber.length()==4 && txtUserName.length()>0 && txtPhoneNumber.length()>0)
        {
            Snackbar snackbar;
            snackbar = Snackbar.make(view,"Pin Setup Successful",Snackbar.LENGTH_LONG);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(SetupActivity.this, R.color.light_blue_600));
            snackbar.show();
            Intent i = new Intent(SetupActivity.this, OTPActivity.class);
            i.putExtra("ContactID",txtPhoneNumber.getText().toString());
            i.putExtra("ContactName",txtUserName.getText().toString());
            startActivity(i);
            finish();
        }
    }
}