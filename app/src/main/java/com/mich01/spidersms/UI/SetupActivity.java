package com.mich01.spidersms.UI;

import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;
import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefsEditor;
import static com.mich01.spidersms.Prefs.PrefsMgr.PREF_NAME;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.transition.Fade;
import android.transition.Slide;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.mich01.spidersms.Crypto.IDManagementProtocol;
import com.mich01.spidersms.DB.DBManager;
import com.mich01.spidersms.R;
import com.mich01.spidersms.Setup.OTPActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class SetupActivity extends AppCompatActivity {
    Button RegisterUserBtn;
    EditText txtPinNumber;
    EditText txtConfirmPinNumber;
    EditText txtUserName;
    EditText txtPhoneNumber;
    String Pin_Number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        RegisterUserBtn = findViewById(R.id.cmd_complete);
        txtPinNumber = findViewById(R.id.txt_pin);
        txtConfirmPinNumber = findViewById(R.id.txt_confirm_pin);
        txtUserName = findViewById(R.id.txt_username);
        txtPhoneNumber = findViewById(R.id.txt_phone_number);
        RegisterUserBtn.setEnabled(false);
        txtPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_invalid_24, 0);
        txtConfirmPinNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_invalid_24, 0);
        txtConfirmPinNumber.addTextChangedListener(new TextWatcher() {
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
        RegisterUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Pin_Number = txtPinNumber.getText().toString();
                Snackbar snackbar;
                if(SetKeyPin(Pin_Number, view))
                {
                    snackbar = Snackbar.make(view,"Pin Setup Successful",Snackbar.LENGTH_LONG);
                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(SetupActivity.this, R.color.light_blue_600));
                    snackbar.show();
                    Intent i = new Intent(SetupActivity.this, OTPActivity.class);
                    i.putExtra("ContactID",txtPhoneNumber.getText().toString());
                    i.putExtra("ContactName",txtUserName.getText().toString());
                    startActivity(i);
                    finish();
                }
                else
                {
                    snackbar =Snackbar.make(view,"Pin Setup Failed",Snackbar.LENGTH_LONG);
                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(SetupActivity.this, R.color.error));
                    snackbar.show();
                }
            }
        });
    }

    private boolean SetKeyPin(String Pin, View view)
    {
        boolean completed;
        String PinHash = IDManagementProtocol.ComputeHash(Pin);
        new DBManager(SetupActivity.this);
        JSONObject KeyJSON = IDManagementProtocol.PKI_CURVE_25519();
        MyPrefs = SetupActivity.this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        MyPrefsEditor = MyPrefs.edit();
        MyPrefsEditor.putString("MyPinHash", PinHash);
        try {
            MyPrefsEditor.putString("PublicKey",KeyJSON.getString("PublicKey"));
            MyPrefsEditor.putString("PrivateKey",KeyJSON.getString("PrivateKey"));
            MyPrefsEditor.putString("KeyPair",KeyJSON.getString("KeyPair"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MyPrefsEditor.putInt("SetupComplete", 1);
        MyPrefsEditor.apply();
        if(MyPrefsEditor.commit())
        {
            completed =true;
        }
        else
        {
            completed= false;
        }
        return completed;
    }

}