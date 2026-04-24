package com.nv.nvgeowatch;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.nv.nvgeowatch.databinding.ActivityMainBinding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "HikerWatch";
    private static final int LOCATION_PERMISSION_REQUEST = 1;

    private ActivityMainBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    updateLocationInfo(location);
                }
            }
        };

        requestLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasLocationPermission()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        backgroundExecutor.shutdownNow();
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        if (hasLocationPermission()) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, R.string.error_permission_denied, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startLocationUpdates() {
        if (!hasLocationPermission()) return;

        showLoading(true);

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .setWaitForAccurateLocation(false)
                .build();

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
                    Looper.getMainLooper());

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    updateLocationInfo(location);
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission lost", e);
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.statusText.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateLocationInfo(Location location) {
        showLoading(false);

        binding.lat.setText(String.format(Locale.getDefault(), "Latitude:  %.6f", location.getLatitude()));
        binding.lon.setText(String.format(Locale.getDefault(), "Longitude: %.6f", location.getLongitude()));
        binding.alt.setText(String.format(Locale.getDefault(), "Altitude:  %.1f m", location.getAltitude()));
        binding.acc.setText(String.format(Locale.getDefault(), "Accuracy:  %.1f m", location.getAccuracy()));

        resolveAddress(location.getLatitude(), location.getLongitude());
    }

    private void resolveAddress(double latitude, double longitude) {
        backgroundExecutor.execute(() -> {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            String addressText = getString(R.string.error_no_address);

            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address addr = addresses.get(0);
                    StringBuilder sb = new StringBuilder();

                    appendIfNotNull(sb, addr.getSubThoroughfare());
                    appendIfNotNull(sb, addr.getThoroughfare());
                    appendIfNotNull(sb, addr.getSubAdminArea());
                    appendIfNotNull(sb, addr.getLocality());
                    appendIfNotNull(sb, addr.getAdminArea());
                    appendIfNotNull(sb, addr.getPostalCode());
                    appendIfNotNull(sb, addr.getCountryName());

                    if (sb.length() > 0) {
                        addressText = sb.toString().trim();
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoder failed", e);
            }

            final String finalAddress = addressText;
            runOnUiThread(() -> binding.address.setText(finalAddress));
        });
    }

    private void appendIfNotNull(StringBuilder sb, String value) {
        if (value != null && !value.isEmpty()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(value);
        }
    }
}
