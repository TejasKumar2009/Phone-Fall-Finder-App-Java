package com.tejas.phonefallfinder;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class TrackingService extends Service implements SensorEventListener {
    public static final int NOTIFICATION_REQ_CODE = 100;
    public static final String CHANNEL_ID = "Service Channel";
    public static final int NOTIFICATION_ID = 100;
    public static final int LOCATION_PERMISSION_ID = 108;
    public static boolean phone_fall = false;

    MediaPlayer mediaPlayer;
    NotificationManager notificationManager;

    // Location
    FusedLocationProviderClient fusedLocationProviderClient;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

//       Phone Fall detection
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        setNotification(NOTIFICATION_ID, NOTIFICATION_REQ_CODE, CHANNEL_ID, "Phone Fall Finder Service is Running.", "Phone Fall Finder App", 1);


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mediaPlayer.stop();
            notificationManager.cancelAll();

        } catch (Exception e) {
            Toast.makeText(this, "Sorry Failed to stop ringtone. It will automatically stop after few seconds!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float xValue = event.values[0];
        float yValue = event.values[1];
        float zValue = event.values[2];

        double rootSquare = Math.sqrt(Math.pow(xValue, 2) + Math.pow(yValue, 2) + Math.pow(zValue, 2));
        if (rootSquare < 2.0) {
            setNotification(200, 200, CHANNEL_ID, "Phone Fall Finder : Ringing.", "Phone Ringing !", 2);
            mediaPlayer.setLooping(false);
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
//                 Code for fetching location and sending message
                        getLastLocation();
                        notificationManager.cancelAll();
                        stopSelf();
                    SharedPreferences sharedPreferences = getSharedPreferences("SharedPref", MODE_PRIVATE);
                    SharedPreferences.Editor spEditor = sharedPreferences.edit();
                    spEditor.putBoolean("is_service_running", false);
                    spEditor.apply();

                }
            });
        }
        }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void setNotification(int notification_id, int notification_req_code, String channel_id, String cText, String sText, int flag) {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification;
        Intent notificationIntent;

        if (flag == 1) {
            notificationIntent = new Intent(this, MainActivity.class);
        } else {
            notificationIntent = new Intent(this, StopRingingActivity.class);
        }
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notification_req_code, notificationIntent, PendingIntent.FLAG_IMMUTABLE);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, channel_id)
                    .setSmallIcon(R.drawable.ic_baseline_miscellaneous_services_24)
                    .setContentText(cText)
                    .setSubText(sText)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
                    .build();

            notificationManager.createNotificationChannel(new NotificationChannel(channel_id, "All Notifications", NotificationManager.IMPORTANCE_HIGH));
        } else {
            notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_baseline_miscellaneous_services_24)
                    .setContentText("Service is running.")
                    .setSubText("Service chal rahi hai bhai")
                    .setContentIntent(pendingIntent)
                    .build();
        }
        notificationManager.notify(notification_id, notification);

    }

    private void getLastLocation(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)== PackageManager.PERMISSION_GRANTED){

            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null){
                                Geocoder geocoder = new Geocoder(TrackingService.this, Locale.getDefault());
                                try {
                                    List<Address> address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    String current_latitude = String.valueOf(address.get(0).getLatitude());
                                    String current_longitude = String.valueOf(address.get(0).getLongitude());

                                    // Getting data from shared preferences
                                    SharedPreferences sharedPreferences = getSharedPreferences("SharedPref", MODE_PRIVATE);
                                    String mobile_number = sharedPreferences.getString("mobile_number", null);
                                    String mobile_name = sharedPreferences.getString("mobile_name", null);

                                    if(current_latitude!=null && current_longitude!=null) {
                                        String url = "https://www.google.com/maps/search/?api=1&query="+current_latitude+","+current_longitude;
                                        String message = "Hey, your "+mobile_name+" phone has been dropped and no one picked it up after a minute.";
                                        String message2 = "Drop Location : "+url;
                                        try {
                                            sendSms(mobile_number, message);
                                            sendSms(mobile_number, message2);
                                            Toast.makeText(TrackingService.this, "Message Sent Successfully !", Toast.LENGTH_SHORT).show();
                                        } catch(Exception e){
                                            e.printStackTrace();
                                            Toast.makeText(TrackingService.this, "There is a problem while sending the message to the phone number.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else{
                                        Toast.makeText(TrackingService.this, "There is a problem while getting the location of the phone.", Toast.LENGTH_SHORT).show();
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else{
                                Toast.makeText(TrackingService.this, "Your Location is not enabled or any other problem occured !", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        else{
            Toast.makeText(this, "Please Give the location permission !", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSms(String mobile_no, String message){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(mobile_no, null, message, null, null);
    }

}
