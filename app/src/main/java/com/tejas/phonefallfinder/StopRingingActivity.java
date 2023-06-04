package com.tejas.phonefallfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StopRingingActivity extends AppCompatActivity {
    Button stop_ringing_btn;
    public static boolean isStopRinging = false;
    public static boolean getIsStopRinging() {
        return isStopRinging;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_ringing);

        // Finding ids
        stop_ringing_btn = findViewById(R.id.stop_ringing_btn);

//        Click Listeners
        stop_ringing_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(StopRingingActivity.this, TrackingService.class));

                SharedPreferences sharedPreferences = getSharedPreferences("SharedPref", MODE_PRIVATE);
                SharedPreferences.Editor spEditor = sharedPreferences.edit();
                spEditor.putBoolean("is_service_running", false);
                spEditor.apply();

                Intent intent = new Intent(StopRingingActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


    }
}