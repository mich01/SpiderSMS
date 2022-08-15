package com.mich01.spidersms.Setup;

import static com.mich01.spidersms.Crypto.PKI_Cipher.GenerateNewKey;
import static com.mich01.spidersms.Data.StringsConstants.APPEND_PARAM;
import static com.mich01.spidersms.Data.StringsConstants.ContactID;
import static com.mich01.spidersms.Data.StringsConstants.ContactName;
import static com.mich01.spidersms.Data.StringsConstants.IV;
import static com.mich01.spidersms.Data.StringsConstants.MyPinHash;
import static com.mich01.spidersms.Data.StringsConstants.Salt;
import static com.mich01.spidersms.Data.StringsConstants.SetupComplete;
import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;
import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefsEditor;
import static com.mich01.spidersms.Prefs.PrefsMgr.getPrefs;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.hbb20.CountryCodePicker;
import com.mich01.spidersms.Crypto.PKI_Cipher;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.R;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SetupActivity extends AppCompatActivity {
    Button registerUserBtn;
    EditText txtPinNumber;
    EditText txtConfirmPinNumber;
    EditText txtUserName;
    EditText txtPhoneNumber;
    String pinNumber;
    CountryCodePicker ccp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        registerUserBtn = findViewById(R.id.cmd_complete);
        registerUserBtn.setEnabled(false);
        txtPinNumber = findViewById(R.id.txt_pin);
        txtConfirmPinNumber = findViewById(R.id.txt_confirm_pin);
        txtUserName = findViewById(R.id.txt_username);
        txtPhoneNumber = findViewById(R.id.txt_phone_number);
        ccp = findViewById(R.id.country_code);
        ccp.registerCarrierNumberEditText(txtPhoneNumber);
        txtPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_invalid_24, 0);
        txtConfirmPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_invalid_24, 0);
        txtConfirmPinNumber.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                txtPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_invalid_24, 0);
                txtConfirmPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_invalid_24, 0);
                registerUserBtn.setEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                if(!txtPinNumber.getText().toString().isEmpty() && !txtConfirmPinNumber.getText().toString().isEmpty() &&
                        txtPinNumber.getText().toString().equals(txtConfirmPinNumber.getText().toString()) && ccp.isValidFullNumber())
                {
                    txtPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_match_24, 0);
                    txtConfirmPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_match_24, 0);
                    registerUserBtn.setEnabled(true);
                }
                else
                {
                    txtPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_invalid_24, 0);
                    txtConfirmPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_invalid_24, 0);
                    registerUserBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!txtPinNumber.getText().toString().isEmpty() && !txtConfirmPinNumber.getText().toString().isEmpty() &&
                        txtPinNumber.getText().toString().equals(txtConfirmPinNumber.getText().toString()))
                {
                    txtPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_match_24, 0);
                    txtConfirmPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_match_24, 0);
                    registerUserBtn.setEnabled(true);
                }
                else
                {
                    txtPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_invalid_24, 0);
                    txtConfirmPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_invalid_24, 0);
                    registerUserBtn.setEnabled(false);
                }
            }
        });
        registerUserBtn.setOnClickListener(view -> {
            pinNumber = txtPinNumber.getText().toString();
            if(setKeyPin(pinNumber))
            {
                checkAllFields(view);
            }
            else
            {
                Snackbar snackbar;
                snackbar =Snackbar.make(view,R.string.pin_setup_failed, BaseTransientBottomBar.LENGTH_LONG);
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(SetupActivity.this, R.color.error));
                snackbar.show();
            }
        });
    }

    private boolean setKeyPin(String pin)
    {
        boolean completed;
        String pinHash = PKI_Cipher.ComputeHash(pin);
        new DBManager(SetupActivity.this);
        MyPrefs = getPrefs(this);
        MyPrefsEditor = MyPrefs.edit();
        MyPrefsEditor.putString(MyPinHash, pinHash);
        MyPrefsEditor.putInt(SetupComplete, 0);
        MyPrefsEditor.putString(Salt, GenerateNewKey());
        MyPrefsEditor.putString(IV, GenerateNewKey());
        MyPrefsEditor.apply();
        completed = MyPrefsEditor.commit();
        return completed;
    }

    private void checkAllFields(View view) {
        if (txtUserName.length()==0)
        {
            txtUserName.setError(getString(R.string.username_field_required));
        }
        if(txtPhoneNumber.length()==0)
        {
            txtPhoneNumber.setError(getString(R.string.phone_number_field_required));
        }
        if(txtPinNumber.length()==0)
        {
            txtPinNumber.setError(getString(R.string.pin_required));
        }
        if(txtPinNumber.length()==4 && txtUserName.length()>0 && txtPhoneNumber.length()>0)
        {
            Snackbar snackbar;
            snackbar = Snackbar.make(view,R.string.pin_setup_successful, BaseTransientBottomBar.LENGTH_LONG);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(SetupActivity.this, R.color.light_blue_600));
            snackbar.show();
            Intent i = new Intent(SetupActivity.this, OTPActivity.class);
            i.putExtra(ContactID,APPEND_PARAM+ccp.getFullNumber());
            i.putExtra(ContactName,txtUserName.getText().toString());
            startActivity(i);
            finish();
        }
    }
}