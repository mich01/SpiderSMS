package com.mich01.spidersms.Setup;

import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefs;
import static com.mich01.spidersms.Prefs.PrefsMgr.MyPrefsEditor;
import static com.mich01.spidersms.Prefs.PrefsMgr.PREF_NAME;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

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


    // [END declare_auth]
    String ContactID;
    String ContactName;
    ImageView OTP_Status;
    Button VerifyOTP;
    EditText OTP_Text;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        OTP_Status = findViewById(R.id.img_status);
        VerifyOTP =findViewById(R.id.cmd_complete_setup);
        OTP_Text = findViewById(R.id.txt_otp);
        Bundle bundle = getIntent().getExtras();
        ContactID = bundle.getString("ContactID");
        ContactName = bundle.getString("ContactName");
        // [START declare_auth]
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String phoneNumber = "+"+ContactID;
        String smsCode = "123456";
        // The test phone number and code should be whitelisted in the console.


        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseAuthSettings firebaseAuthSettings = firebaseAuth.getFirebaseAuthSettings();

// Configure faking the auto-retrieval with the whitelisted numbers.
        firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phoneNumber, smsCode);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(120L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        OTP_Status.setImageResource(R.drawable.otp_success);
                        MyPrefs = OTPActivity.this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                        MyPrefsEditor = MyPrefs.edit();
                        MyPrefsEditor.putString("MyContact", ContactID);
                        MyPrefsEditor.putString("ContactName", ContactName);
                        MyPrefsEditor.apply();
                        MyPrefsEditor.commit();
                        startActivity(new Intent(OTPActivity.this, HomeActivity.class));

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                    }

                    // ...
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        VerifyOTP.setOnClickListener(view -> {
            if(OTP_Text.getText().toString().equals("1234"))
            {
                OTP_Status.setImageResource(R.drawable.otp_success);
                MyPrefs = OTPActivity.this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                MyPrefsEditor = MyPrefs.edit();
                MyPrefsEditor.putString("MyContact", ContactID);
                MyPrefsEditor.putString("ContactName", ContactName);
                MyPrefsEditor.apply();
                MyPrefsEditor.commit();
                startActivity(new Intent(OTPActivity.this, HomeActivity.class));
            }
        });
    }
}