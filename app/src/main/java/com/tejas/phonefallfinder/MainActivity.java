package com.tejas.phonefallfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;


public class MainActivity extends AppCompatActivity {
    Button tracking_btn, settings_btn;
    public static final int LOCATION_PERMISSION_ID = 108;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // Finding ids
        tracking_btn = findViewById(R.id.tracking_btn);
        settings_btn = findViewById(R.id.settings_btn);

        // Getting location permission from user
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){}
        else
            askLocationPermission();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)==PackageManager.PERMISSION_GRANTED){}
        else
            askSmsPermission();

        // Getting SharedPreferences to check mobile number is already added or not
        SharedPreferences sharedPreferences = getSharedPreferences("SharedPref", MODE_PRIVATE);
        String mobile_number = sharedPreferences.getString("mobile_number", null);
        if (mobile_number==null){
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(intent);
        }

        // Click Listeners
        settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        tracking_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("SharedPref", MODE_PRIVATE);
                boolean is_service_running = sharedPreferences.getBoolean("is_service_running", false);
                SharedPreferences.Editor spEditor = sharedPreferences.edit();

                if (is_service_running==false) {
                    startService(new Intent(MainActivity.this, TrackingService.class));
                    spEditor.putBoolean("is_service_running", true);
                    spEditor.apply();
                    tracking_btn.setText("Stop Tracking");
                } else{
                    stopService(new Intent(MainActivity.this, TrackingService.class));
                    spEditor.putBoolean("is_service_running", false);
                    spEditor.apply();
                    tracking_btn.setText("Start Tracking");
                }


            }
        });
    }

    private void askLocationPermission(){
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
    }

    private void askSmsPermission(){
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, LOCATION_PERMISSION_ID);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else{
                Toast.makeText(this, "Please Provide the necessary Permission.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}