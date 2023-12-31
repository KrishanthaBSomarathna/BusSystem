package com.example.bussystem.SA22403810;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.example.bussystem.R;
import com.example.bussystem.SA22404350.BusDriverProfile;
import com.example.bussystem.SA22404350.Register;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class BusDriverView extends AppCompatActivity {

    ImageView menu;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private ImageView start,stop;
    private boolean locationUpdatesActive = false;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    TextView Speed,LocationName,Type,Number,Route;

    String mobile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_driver_view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Speed = findViewById(R.id.speed);
        Type = findViewById(R.id.type);
        Number = findViewById(R.id.number);
        Route = findViewById(R.id.route);
         LocationName= findViewById(R.id.location);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );




        menu = findViewById(R.id.menu);

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), BusDriverProfile.class));
            }
        });

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        mobile = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

        start = findViewById(R.id.startbtn);
        stop = findViewById(R.id.stopbtn);

        stop.setVisibility(View.GONE);
        

        if(firebaseUser==null){
            startActivity(new Intent(this, Register.class));
        }




        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Create a location request
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000); // Update interval in milliseconds
        locationRequest.setFastestInterval(500); // Fastest update interval in milliseconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);




        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String type = (String) snapshot.child("Bus Drivers").child(mobile).child("bustype").getValue();
                Type.setText(type);

                String number = (String) snapshot.child("Bus Drivers").child(mobile).child("vehicleNum").getValue();
                Number.setText(number);

                String route = (String) snapshot.child("Bus Drivers").child(mobile).child("roadnumber").getValue();
                Route.setText(route);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        // Define a location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    double speedInMetersPerSecond = location.getSpeed();
                    int speedInKmPerHour = (int) (speedInMetersPerSecond * 3.6); // Conversion from m/s to km/h

                    Speed.setText(String.valueOf(speedInKmPerHour));

                    // Now you can use the latitude and longitude
//                    Toast.makeText(BusDriverView.this, "Latitude: "  + longitude + ", Speed: " + Speed, Toast.LENGTH_SHORT).show();

                    // Use Geocoder to get the location name
                    Geocoder geocoder = new Geocoder(BusDriverView.this, Locale.getDefault());
                    String locationName = null;
                    try {
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if (!addresses.isEmpty()) {
                            locationName = addresses.get(0).getAddressLine(0);

//                            Toast.makeText(BusDriverView.this, locationName, Toast.LENGTH_SHORT).show();
                            LocationName.setText(locationName);

                            // You can store or use the locationName as needed
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Store latitude and longitude in Firebase
                    databaseReference.child("Bus Drivers").child(firebaseUser.getPhoneNumber()).child("Latitude").setValue(latitude);
                    databaseReference.child("Bus Drivers").child(firebaseUser.getPhoneNumber()).child("Longitude").setValue(longitude);
                    databaseReference.child("Bus Drivers").child(firebaseUser.getPhoneNumber()).child("LocationName").setValue(locationName);


                }
            }
        };




        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop.setVisibility(View.VISIBLE);
                start.setVisibility(View.GONE);
                if (ContextCompat.checkSelfPermission(BusDriverView.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(BusDriverView.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                } else {
                    // Start location updates
                    startLocationUpdates();
                    locationUpdatesActive = true;
                    databaseReference.child("Bus Drivers").child(firebaseUser.getPhoneNumber()).child("status").setValue("online");

                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start.setVisibility(View.VISIBLE);
                stop.setVisibility(View.GONE);
                stopLocationUpdates();
                databaseReference.child("Bus Drivers").child(firebaseUser.getPhoneNumber()).child("status").setValue("offline");

            }
        });

        // Check and request location permissions if needed

    }
    private void stopLocationUpdates() {
        if (locationUpdatesActive) {
            // Stop location updates
            fusedLocationClient.removeLocationUpdates(locationCallback);
            locationUpdatesActive = false;
            Toast.makeText(this, "Location updates stopped", Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        databaseReference.child("Bus Drivers").child(firebaseUser.getPhoneNumber()).child("status").setValue("offline");
//
//    }
public void onBackPressed() {
    // Handle back button press, navigate to home screen
    Intent intent = new Intent(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_HOME);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(intent);
    stopLocationUpdates();
}

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        databaseReference.child("Bus Drivers").child(firebaseUser.getPhoneNumber()).child("status").setValue("offline");

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        startLocationUpdates();
        stop.setVisibility(View.VISIBLE);
        start.setVisibility(View.GONE);
        locationUpdatesActive = true;
        databaseReference.child("Bus Drivers").child(firebaseUser.getPhoneNumber()).child("status").setValue("online");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                startLocationUpdates();
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationUpdates() {
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

}
