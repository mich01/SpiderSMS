package com.mich01.spidersms.UI;

import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;
import static com.mich01.spidersms.Prefs.PrefsMgr.PREF_NAME;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mich01.spidersms.Crypto.IDManagementProtocol;
import com.mich01.spidersms.Prefs.PrefsMgr;
import com.mich01.spidersms.R;

import java.util.concurrent.Executor;

public class UnlockActivity extends AppCompatActivity {

    private Executor executor;
    ImageView UnlockStatus;
    EditText UserPin;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    public UnlockActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);
        UserPin = findViewById(R.id.txt_user_pin);
        UnlockStatus = findViewById(R.id.img_lock_status);
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(UnlockActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication Method Changed: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                UnlockStatus.setImageResource(R.drawable.ic_unlocked_fingerprint_24);
                startActivity(new Intent(UnlockActivity.this, HomeActivity.class));
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();

        biometricPrompt.authenticate(promptInfo);
        // Prompt appears when user clicks "Log in".
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.
        UnlockStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                biometricPrompt.authenticate(promptInfo);
            }
        });
        UserPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                MyPrefs = UnlockActivity.this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                if(!UserPin.getText().toString().isEmpty() & IDManagementProtocol.ComputeHash(UserPin.getText().toString()).equals(MyPrefs.getString("MyPinHash", "0")))
                {
                    UnlockStatus.setImageResource(R.drawable.ic_unlocked_fingerprint_24);
                    startActivity(new Intent(UnlockActivity.this, HomeActivity.class));
                    finish();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}