package com.mich01.spidersms.UI;

import static com.mich01.spidersms.Data.StringsConstants.global_pref;
import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.mich01.spidersms.Crypto.PKI_Cipher;
import com.mich01.spidersms.R;

import java.util.Objects;
import java.util.concurrent.Executor;

public class UnlockActivity extends AppCompatActivity {

    private Executor executor;
    ImageView UnlockStatus;
    EditText UserPin;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    public UnlockActivity() {
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);
        UserPin = findViewById(R.id.txt_user_pin);
        UnlockStatus = findViewById(R.id.img_lock_status);
        executor = ContextCompat.getMainExecutor(this);
        Objects.requireNonNull(this.getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        this.getSupportActionBar().setCustomView(R.layout.unclock_action_bar);
        getBiometrics();
        UnlockStatus.setOnClickListener(view -> getBiometrics());

    }
    public void getBiometrics()
    {
        biometricPrompt = new BiometricPrompt(UnlockActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                SnackBarAlert("Authentication Method Changed: " );
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
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
                SnackBarAlertError();
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
        UnlockStatus.setOnClickListener(view -> biometricPrompt.authenticate(promptInfo));
        UserPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                MyPrefs = UnlockActivity.this.getSharedPreferences(global_pref, Context.MODE_PRIVATE);
                if(!UserPin.getText().toString().isEmpty() & PKI_Cipher.ComputeHash(UserPin.getText().toString()).equals(MyPrefs.getString("MyPinHash", "0")))
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

    private void SnackBarAlertError() {

            Snackbar mSnackBar = Snackbar.make(findViewById(android.R.id.content), "Authentication failed", Snackbar.LENGTH_LONG);
            TextView SnackBarView = (mSnackBar.getView()).findViewById(R.id.snackbar_text);
            SnackBarView.setTextColor(ContextCompat.getColor(UnlockActivity.this, R.color.white));
            SnackBarView.setBackgroundColor(ContextCompat.getColor(UnlockActivity.this, R.color.error));
            SnackBarView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            SnackBarView.setGravity(Gravity.CENTER_HORIZONTAL);
            mSnackBar.show();
    }

    public void SnackBarAlert(String AlertMessage)
    {
        Snackbar mSnackBar = Snackbar.make(findViewById(android.R.id.content), AlertMessage, Snackbar.LENGTH_LONG);
        TextView SnackBarView = (mSnackBar.getView()).findViewById(R.id.snackbar_text);
        SnackBarView.setTextColor(ContextCompat.getColor(UnlockActivity.this, R.color.white));
        SnackBarView.setBackgroundColor(ContextCompat.getColor(UnlockActivity.this, R.color.darkblue));
        SnackBarView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        SnackBarView.setGravity(Gravity.CENTER_HORIZONTAL);
        mSnackBar.show();
    }
}