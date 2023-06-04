package com.tejas.phonefallfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class WelcomeActivity extends AppCompatActivity {
    EditText mobile_number_input, mobile_name_input;
    Button save_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Finding ids
        mobile_number_input = findViewById(R.id.mobile_number_input);
        mobile_name_input = findViewById(R.id.mobile_name_input);
        save_btn = findViewById(R.id.save_btn);


        // Save Button Click Listener
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mobile_number = mobile_number_input.getText().toString();
                String mobile_name = mobile_name_input.getText().toString();

                if (mobile_name.length() > 12) {
                    Toast.makeText(WelcomeActivity.this, "Please enter mobile no. less tham 13 characters.", Toast.LENGTH_SHORT).show();
                } 
                else if (mobile_name.equals("") || mobile_number.equals("")){
                    Toast.makeText(WelcomeActivity.this, "Input Cannot be empty !", Toast.LENGTH_SHORT).show();
                }
                else{
                    SharedPreferences sharedPreferences = getSharedPreferences("SharedPref", MODE_PRIVATE);
                    SharedPreferences.Editor spEditor = sharedPreferences.edit();
                    spEditor.putString("mobile_number", mobile_number);
                    spEditor.putString("mobile_name", mobile_name);
                    spEditor.apply();

                    Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        intent.putExtra("isFinish", true);
        startActivity(intent);
    }
}
