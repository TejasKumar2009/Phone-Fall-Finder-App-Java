package com.tejas.phonefallfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    EditText mobile_number_input, mobile_name_input;
    Button save_btn;
    ImageView back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Finding ids
        mobile_number_input = findViewById(R.id.mobile_number_input);
        mobile_name_input = findViewById(R.id.mobile_name_input);
        save_btn = findViewById(R.id.save_btn);
        back_btn = findViewById(R.id.back_btn);

        // Getting shared Preferences Data
        SharedPreferences sharedPreferences = getSharedPreferences("SharedPref", MODE_PRIVATE);
        String mobile_number = sharedPreferences.getString("mobile_number", null);
        String mobile_name = sharedPreferences.getString("mobile_name", null);

        // Adding text to mobile number input
        mobile_number_input.setText(mobile_number);
        mobile_name_input.setText(mobile_name);

//        Click Listeners
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone_no = mobile_number_input.getText().toString();
                String phone_name = mobile_name_input.getText().toString();

                if (mobile_name.length() >= 12) {
                    Toast.makeText(SettingsActivity.this, "Please enter mobile no. less tham 12 characters.", Toast.LENGTH_SHORT).show();
                }
                else if (phone_name.equals("") || phone_no.equals("")){
                    Toast.makeText(SettingsActivity.this, "Input Cannot be empty !", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences sharedPreferences = getSharedPreferences("SharedPref", MODE_PRIVATE);
                    SharedPreferences.Editor spEditor = sharedPreferences.edit();
                    spEditor.putString("mobile_number", phone_no);
                    spEditor.putString("mobile_name", phone_name);
                    spEditor.apply();

                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            };

    });
};
}