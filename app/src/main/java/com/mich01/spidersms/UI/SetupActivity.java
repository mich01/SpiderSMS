package com.mich01.spidersms.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mich01.spidersms.R;
import com.mich01.spidersms.Setup.OTPActivity;

public class SetupActivity extends AppCompatActivity {
    Button RegisterUserBtn;
    EditText txtPhoneNumber;
    String PhoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        RegisterUserBtn = findViewById(R.id.cmd_next);
        txtPhoneNumber = findViewById(R.id.txt_phone);
        RegisterUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhoneNumber = txtPhoneNumber.getText().toString();
                if(!PhoneNumber.isEmpty())
                {
                    Intent i = new Intent(SetupActivity.this, OTPActivity.class);
                    i.putExtra("PhoneNumber",PhoneNumber);
                    startActivity(i);
                }
            }
        });
    }
}