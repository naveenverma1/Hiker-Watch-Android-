package com.nv.nvgeowatch;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.location.LocationManagerCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

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

/**
 * The single screen of Hiker's Watch.
 *
 * Responsibilities:
 *   - Request and observe location permission (FINE preferred, COARSE acceptable).
 *   - Stream fused location updates while in the foreground.
 *   - Reverse-geocode the most recent fix and render lat/lon/alt/accuracy + address.
 *   - Expose Share / Copy / Open-in-Maps actions over the current fix.
 *   - Recover gracefully when the user has denied permission permanently, blocked
 *     location services, or only granted approximate location.
 *
 * Edge-to-edge: the activity targets Android 15 (API 35) and applies window
 * insets to the toolbar (top) and scroll view (bottom) so content is never
 * drawn under the system bars.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "HikerWatch";
    private static final int LOCATION_PERMISSION_REQUEST = 1;

    private static final long LOCATION_UPDATE_INTERVAL_MS = 5_000L;
    private static final long LOCATION_FASTEST_INTERVAL_MS = 2_000L;
    /** If we don't have a fix this long after starting, surface a hint to the user. */
    private static final long FIX_TIMEOUT_MS = 30_000L;

    private ActivityMainBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable private Location lastKnown;
    @Nullable private String   lastAddress;

    private final Runnable fixTimeoutRunnable = this::onFixTimeout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Install the system splash screen BEFORE super.onCreate so the system
        // can hand off seamlessly from the splash to our content.
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // Edge-to-edge for Android 15 (API 35) — required since we target SDK 35.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setSubtitle(R.string.app_tagline);
        }

        applyEdgeToEdgeInsets();
        renderVersionFooter();
        wireActionButtons();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    onLocationFix(location);
                }
            }
        };

        // Kick off the permission flow. onResume() will start updates once
        // permission is granted.
        ensurePermissionAndStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The user may have changed Settings (permission, GPS) while we were
        // backgrounded — re-evaluate every time we come back to the foreground.
        refreshWarningBanner();
        if (hasAnyLocationPermission() && isLocationEnabled()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        mainHandler.removeCallbacks(fixTimeoutRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        backgroundExecutor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    // ---------------------------------------------------------------------
    //  Edge-to-edge
    // ---------------------------------------------------------------------

    /**
     * Push the system status-bar inset down onto the toolbar's top padding, and
     * the navigation-bar inset onto the scroll view's bottom padding. Without
     * this, on Android 15 the toolbar title sits under the status bar and the
     * version footer sits under the gesture pill.
     */
    private void applyEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return windowInsets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollView, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Top is already handled by the AppBar; only adjust the bottom.
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bars.bottom);
            return windowInsets;
        });
    }

    // ---------------------------------------------------------------------
    //  Permission flow
    // ---------------------------------------------------------------------

    private boolean hasFineLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCoarseLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasAnyLocationPermission() {
        return hasFineLocationPermission() || hasCoarseLocationPermission();
    }

    private void ensurePermissionAndStart() {
        if (hasAnyLocationPermission()) {
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
        if (requestCode != LOCATION_PERMISSION_REQUEST) return;

        if (hasAnyLocationPermission()) {
            startLocationUpdates();
        } else {
            // If the user *permanently* denied, shouldShowRequestPermissionRationale
            // returns false for the denied permission — surface a Settings CTA.
            boolean canAskAgain = ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (canAskAgain) {
                Toast.makeText(this, R.string.error_permission_denied, Toast.LENGTH_LONG).show();
            }
        }
        refreshWarningBanner();
    }

    // ---------------------------------------------------------------------
    //  Location services state
    // ---------------------------------------------------------------------

    private boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return lm != null && LocationManagerCompat.isLocationEnabled(lm);
    }

    /** Re-evaluate the in-line warning banner each time something might have changed. */
    private void refreshWarningBanner() {
        if (!hasAnyLocationPermission()) {
            boolean canAskAgain = ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (canAskAgain) {
                showWarning(getString(R.string.error_permission_denied),
                        getString(R.string.action_retry), this::ensurePermissionAndStart);
            } else {
                showWarning(getString(R.string.error_permission_blocked),
                        getString(R.string.action_open_settings), this::openAppSettings);
            }
            return;
        }
        if (!isLocationEnabled()) {
            showWarning(getString(R.string.error_location_disabled),
                    getString(R.string.action_enable_location), this::openLocationSettings);
            return;
        }
        if (!hasFineLocationPermission()) {
            // We have coarse only — keep working, but tell the user how to upgrade.
            showWarning(getString(R.string.status_approx_only),
                    getString(R.string.action_open_settings), this::openAppSettings);
            return;
        }
        hideWarning();
    }

    private void showWarning(@NonNull String message,
                             @NonNull String actionLabel,
                             @NonNull Runnable onAction) {
        binding.warningText.setText(message);
        binding.warningAction.setText(actionLabel);
        binding.warningAction.setOnClickListener(v -> onAction.run());
        binding.warningCard.setVisibility(View.VISIBLE);
    }

    private void hideWarning() {
        binding.warningCard.setVisibility(View.GONE);
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void openLocationSettings() {
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    // ---------------------------------------------------------------------
    //  Location updates
    // ---------------------------------------------------------------------

    private void startLocationUpdates() {
        if (!hasAnyLocationPermission() || !isLocationEnabled()) return;

        // If we don't yet have a fix, show the loading row. Once we get a fix
        // it auto-hides in updateLocationInfo().
        if (lastKnown == null) {
            showLoading(true);
            mainHandler.removeCallbacks(fixTimeoutRunnable);
            mainHandler.postDelayed(fixTimeoutRunnable, FIX_TIMEOUT_MS);
        }

        int priority = hasFineLocationPermission()
                ? Priority.PRIORITY_HIGH_ACCURACY
                : Priority.PRIORITY_BALANCED_POWER_ACCURACY;

        LocationRequest request = new LocationRequest.Builder(priority, LOCATION_UPDATE_INTERVAL_MS)
                .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL_MS)
                .setWaitForAccurateLocation(false)
                .build();

        try {
            fusedLocationClient.requestLocationUpdates(request, locationCallback,
                    Looper.getMainLooper());

            // getLastLocation() returns the cached fix immediately if available.
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) onLocationFix(location);
            });
        } catch (SecurityException e) {
            Log.e(TAG, "Lost location permission between check and request", e);
            refreshWarningBanner();
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void onLocationFix(@NonNull Location location) {
        lastKnown = location;
        showLoading(false);
        mainHandler.removeCallbacks(fixTimeoutRunnable);

        binding.lat.setText(String.format(Locale.US, "Latitude:  %+.6f", location.getLatitude()));
        binding.lon.setText(String.format(Locale.US, "Longitude: %+.6f", location.getLongitude()));
        binding.alt.setText(String.format(Locale.US, "Altitude:  %.1f m", location.getAltitude()));
        binding.acc.setText(String.format(Locale.US, "Accuracy:  %.1f m", location.getAccuracy()));

        setActionsEnabled(true);

        resolveAddress(location.getLatitude(), location.getLongitude());
    }

    private void onFixTimeout() {
        // We waited FIX_TIMEOUT_MS and still no fix. Surface the warning that's
        // most likely the cause so the user has a recovery path.
        refreshWarningBanner();
    }

    private void showLoading(boolean show) {
        binding.loadingRow.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void setActionsEnabled(boolean enabled) {
        binding.btnShare.setEnabled(enabled);
        binding.btnCopy.setEnabled(enabled);
        binding.btnMaps.setEnabled(enabled);
    }

    // ---------------------------------------------------------------------
    //  Reverse geocoding
    // ---------------------------------------------------------------------

    private void resolveAddress(double latitude, double longitude) {
        if (!Geocoder.isPresent()) {
            lastAddress = null;
            binding.address.setText(R.string.error_geocoder_unavailable);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Async API — required on API 33+ and avoids potential ANRs.
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            geocoder.getFromLocation(latitude, longitude, 1, new Geocoder.GeocodeListener() {
                @Override
                public void onGeocode(@NonNull List<Address> addresses) {
                    String text = formatAddress(addresses);
                    lastAddress = text;
                    binding.address.setText(text);
                }

                @Override
                public void onError(@Nullable String errorMessage) {
                    Log.e(TAG, "Geocoder error: " + errorMessage);
                    lastAddress = null;
                    binding.address.setText(R.string.error_no_address);
                }
            });
        } else {
            // Legacy sync API — keep on a background thread.
            backgroundExecutor.execute(() -> {
                String text;
                try {
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    @SuppressWarnings("deprecation")
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    text = formatAddress(addresses);
                } catch (IOException e) {
                    Log.e(TAG, "Geocoder failed", e);
                    text = getString(R.string.error_no_address);
                }
                final String finalText = text;
                runOnUiThread(() -> {
                    lastAddress = finalText;
                    binding.address.setText(finalText);
                });
            });
        }
    }

    @NonNull
    private String formatAddress(@Nullable List<Address> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return getString(R.string.error_no_address);
        }
        Address addr = addresses.get(0);
        StringBuilder sb = new StringBuilder();
        appendIfNotNull(sb, addr.getSubThoroughfare());
        appendIfNotNull(sb, addr.getThoroughfare());
        appendIfNotNull(sb, addr.getSubAdminArea());
        appendIfNotNull(sb, addr.getLocality());
        appendIfNotNull(sb, addr.getAdminArea());
        appendIfNotNull(sb, addr.getPostalCode());
        appendIfNotNull(sb, addr.getCountryName());
        return sb.length() > 0 ? sb.toString().trim() : getString(R.string.error_no_address);
    }

    private void appendIfNotNull(StringBuilder sb, @Nullable String value) {
        if (value != null && !value.isEmpty()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(value);
        }
    }

    // ---------------------------------------------------------------------
    //  Actions
    // ---------------------------------------------------------------------

    private void wireActionButtons() {
        binding.btnShare.setOnClickListener(v -> shareCurrentLocation());
        binding.btnCopy.setOnClickListener(v -> copyCurrentLocation());
        binding.btnMaps.setOnClickListener(v -> openInMaps());
    }

    private void shareCurrentLocation() {
        if (lastKnown == null) return;
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
        share.putExtra(Intent.EXTRA_TEXT, buildShareText());
        try {
            startActivity(Intent.createChooser(share, getString(R.string.share_chooser_title)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.error_no_apps_to_handle, Toast.LENGTH_SHORT).show();
        }
    }

    private void copyCurrentLocation() {
        if (lastKnown == null) return;
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null) return;
        cm.setPrimaryClip(ClipData.newPlainText(
                getString(R.string.clipboard_label), buildShareText()));
        // From Android 13, the system shows its own clipboard toast — so on
        // older versions we surface our own confirmation.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(this, R.string.clipboard_copied, Toast.LENGTH_SHORT).show();
        }
    }

    private void openInMaps() {
        if (lastKnown == null) return;
        String label = Uri.encode(getString(R.string.app_name));
        Uri geoUri = Uri.parse(String.format(Locale.US,
                "geo:%f,%f?q=%f,%f(%s)",
                lastKnown.getLatitude(), lastKnown.getLongitude(),
                lastKnown.getLatitude(), lastKnown.getLongitude(),
                label));
        Intent intent = new Intent(Intent.ACTION_VIEW, geoUri);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Fall back to Google Maps web URL if no geo: handler is installed.
            Uri webUri = Uri.parse(String.format(Locale.US,
                    "https://www.google.com/maps/search/?api=1&query=%f,%f",
                    lastKnown.getLatitude(), lastKnown.getLongitude()));
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, webUri));
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(this, R.string.error_no_apps_to_handle, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @NonNull
    private String buildShareText() {
        if (lastKnown == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US, "%.6f, %.6f",
                lastKnown.getLatitude(), lastKnown.getLongitude()));
        if (lastAddress != null && !lastAddress.isEmpty()) {
            sb.append("\n").append(lastAddress.replace("\n", ", "));
        }
        sb.append("\n").append(String.format(Locale.US,
                "https://www.google.com/maps/search/?api=1&query=%f,%f",
                lastKnown.getLatitude(), lastKnown.getLongitude()));
        return sb.toString();
    }

    // ---------------------------------------------------------------------
    //  Menu / about
    // ---------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_about) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.about_title)
                    .setMessage(getString(R.string.about_body,
                            BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE))
                    .setPositiveButton(R.string.dialog_close, null)
                    .show();
            return true;
        }
        if (id == R.id.menu_privacy) {
            Intent browse = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://naveenverma1.github.io/Hiker-Watch-Android-/privacy-policy.html"));
            try {
                startActivity(browse);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.error_no_apps_to_handle, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void renderVersionFooter() {
        binding.versionFooter.setText(getString(R.string.footer_version,
                BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
    }
}
