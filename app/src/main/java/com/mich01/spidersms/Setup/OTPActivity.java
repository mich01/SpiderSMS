package com.mich01.spidersms.Setup;

import static com.mich01.spidersms.Data.StringsConstants.APPEND_PARAM;
import static com.mich01.spidersms.Data.StringsConstants.ContactID;
import static com.mich01.spidersms.Data.StringsConstants.ContactName;
import static com.mich01.spidersms.Data.StringsConstants.DEFAULT_PREF_VALUE;
import static com.mich01.spidersms.Data.StringsConstants.MyContact;
import static com.mich01.spidersms.Data.StringsConstants.SetupComplete;
import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;
import static com.mich01.spidersms.Prefs.PrefsMgr.getPrefs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mich01.spidersms.R;
import com.mich01.spidersms.UI.HomeActivity;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {

    String contactID;
    String contactName;
    ImageView otpStatus;
    Button verifyOTP;
    EditText otpText;
    SharedPreferences.Editor MyPrefsEditor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        otpStatus = findViewById(R.id.img_status);
        verifyOTP =findViewById(R.id.cmd_complete_setup);
        otpText = findViewById(R.id.txt_otp);
        Bundle bundle = getIntent().getExtras();
        contactID = bundle.getString(ContactID);
        contactName = bundle.getString(ContactName);
        String phoneNumber = APPEND_PARAM+contactID;
        // The test phone number and code should be whitelisted in the console.
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseAuthSettings firebaseAuthSettings = firebaseAuth.getFirebaseAuthSettings();

// Configure faking the auto-retrieval with the whitelisted numbers.
        firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phoneNumber, DEFAULT_PREF_VALUE);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(120L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        completeSetup();
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        //Doesn't really need add
                        Toast.makeText(OTPActivity.this, R.string.verification_failed, Toast.LENGTH_SHORT).show();
                    }

                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        verifyOTP.setOnClickListener(view ->checkOTP(phoneNumber));
    }
    private void checkOTP(String phoneNumber)
    {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseAuthSettings firebaseAuthSettings = firebaseAuth.getFirebaseAuthSettings();
        firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phoneNumber, DEFAULT_PREF_VALUE);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(120L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        completeSetup();
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        //Doesn't really need add
                        Toast.makeText(OTPActivity.this, R.string.verification_failed, Toast.LENGTH_SHORT).show();
                    }

                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    public void completeSetup()
    {
        otpStatus.setImageResource(R.drawable.otp_success);
        MyPrefs = getPrefs(this);
        MyPrefsEditor = MyPrefs.edit();
        MyPrefsEditor.putString(MyContact, contactID);
        MyPrefsEditor.putString(ContactName, contactName);
        MyPrefsEditor.putInt(SetupComplete, 1);
        MyPrefsEditor.apply();
        MyPrefsEditor.commit();
        startActivity(new Intent(OTPActivity.this, HomeActivity.class));
        finish();
    }
}