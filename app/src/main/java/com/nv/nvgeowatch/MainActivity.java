package com.nv.nvgeowatch;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
 * Hiker's Watch — single-screen GPS display.
 *
 * Lifecycle
 *   - onCreate: install splash, inflate view, wire toolbar/actions, ask for permission.
 *   - onResume: re-evaluate state (permission / GPS / coarse-only), start updates.
 *   - onPause:  stop location updates and cancel pending tasks.
 *
 * Edge-to-edge for Android 15 (API 35) via setDecorFitsSystemWindows(false) +
 * inset-aware padding on the app bar and scroll view.
 *
 * User preferences (altitude unit, coordinate format) live in SharedPreferences
 * so they survive process restarts. Defaults: meters + decimal degrees.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "HikerWatch";
    private static final int LOCATION_PERMISSION_REQUEST = 1;

    private static final long LOCATION_UPDATE_INTERVAL_MS = 5_000L;
    private static final long LOCATION_FASTEST_INTERVAL_MS = 2_000L;
    /** If we don't have a fix this long after starting, surface a hint to the user. */
    private static final long FIX_TIMEOUT_MS    = 30_000L;
    /** If the geocoder hasn't returned after this long, surface a hint to the user. */
    private static final long GEOCODE_TIMEOUT_MS = 10_000L;

    // SharedPreferences keys
    private static final String PREFS = "hiker_prefs";
    private static final String KEY_HAS_REQUESTED_PERMISSION = "has_requested_permission";
    private static final String KEY_ALTITUDE_UNIT = "altitude_unit";   // "m" | "ft"
    private static final String KEY_COORD_FORMAT  = "coord_format";    // "decimal" | "dms"

    private ActivityMainBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private SharedPreferences prefs;
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable private Location lastKnown;
    @Nullable private String   lastAddress;

    private final Runnable fixTimeoutRunnable     = this::onFixTimeout;
    private final Runnable geocodeTimeoutRunnable = this::onGeocodeTimeout;

    // -------------------------------------------------------------------------
    //  Lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setSubtitle(R.string.app_tagline);
        }

        applyEdgeToEdgeInsets();
        renderVersionFooter();
        wireActionButtons();
        wireCardTapToCopy();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) onLocationFix(location);
            }
        };

        ensurePermissionAndStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        mainHandler.removeCallbacks(geocodeTimeoutRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        backgroundExecutor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }

    // -------------------------------------------------------------------------
    //  Edge-to-edge
    // -------------------------------------------------------------------------

    private void applyEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return windowInsets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollView, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bars.bottom);
            return windowInsets;
        });
    }

    // -------------------------------------------------------------------------
    //  Permission flow (with first-run-aware UX)
    // -------------------------------------------------------------------------

    private boolean hasFineLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasAnyLocationPermission() {
        return hasFineLocationPermission()
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void ensurePermissionAndStart() {
        if (hasAnyLocationPermission()) {
            startLocationUpdates();
        } else {
            // Mark that we've now requested at least once so onResume distinguishes
            // "never asked" from "denied permanently" reliably.
            prefs.edit().putBoolean(KEY_HAS_REQUESTED_PERMISSION, true).apply();
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
        if (hasAnyLocationPermission()) startLocationUpdates();
        refreshWarningBanner();
    }

    private boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return lm != null && LocationManagerCompat.isLocationEnabled(lm);
    }

    /**
     * Decide which warning banner (if any) to surface based on current state.
     * Critical: distinguish "never asked" from "permanently denied" via the
     * {@link #KEY_HAS_REQUESTED_PERMISSION} flag — they look identical from
     * {@link ActivityCompat#shouldShowRequestPermissionRationale}.
     */
    private void refreshWarningBanner() {
        if (!hasAnyLocationPermission()) {
            boolean hasAskedBefore = prefs.getBoolean(KEY_HAS_REQUESTED_PERMISSION, false);
            boolean canAskAgain = ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (!hasAskedBefore) {
                // Truly first run — the system dialog will appear momentarily.
                // Don't scare the user with "blocked"; show a gentle inline note.
                showWarning(getString(R.string.error_permission_first_run),
                        getString(R.string.action_grant), this::ensurePermissionAndStart);
            } else if (canAskAgain) {
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

    // -------------------------------------------------------------------------
    //  Location updates
    // -------------------------------------------------------------------------

    private void startLocationUpdates() {
        if (!hasAnyLocationPermission() || !isLocationEnabled()) return;

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

        binding.lat.setText("Latitude:  " + LocationFormatter.formatLatitude(location.getLatitude(), getCoordFormat()));
        binding.lon.setText("Longitude: " + LocationFormatter.formatLongitude(location.getLongitude(), getCoordFormat()));
        binding.alt.setText("Altitude:  " + LocationFormatter.formatAltitude(location.getAltitude(), getAltitudeUnit()));
        binding.acc.setText(String.format(Locale.US, "Accuracy:  %.1f m", location.getAccuracy()));

        setActionsEnabled(true);
        resolveAddress(location.getLatitude(), location.getLongitude());
    }

    private void onFixTimeout() {
        refreshWarningBanner();
    }

    private void onGeocodeTimeout() {
        if (lastAddress == null) binding.address.setText(R.string.error_geocoder_timeout);
    }

    private void showLoading(boolean show) {
        binding.loadingRow.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void setActionsEnabled(boolean enabled) {
        binding.btnShare.setEnabled(enabled);
        binding.btnCopy.setEnabled(enabled);
        binding.btnMaps.setEnabled(enabled);
    }

    private void refreshNow() {
        // Re-render with the current fix (picks up new units / format) and re-fetch the geocode.
        lastAddress = null;
        if (lastKnown != null) {
            onLocationFix(lastKnown);
        }
        // Force a fresh location pull from the fused client.
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) onLocationFix(location);
            });
        } catch (SecurityException ignore) {}
    }

    // -------------------------------------------------------------------------
    //  Reverse geocoding (with timeout)
    // -------------------------------------------------------------------------

    private void resolveAddress(double latitude, double longitude) {
        if (!Geocoder.isPresent()) {
            lastAddress = null;
            binding.address.setText(R.string.error_geocoder_unavailable);
            return;
        }

        mainHandler.removeCallbacks(geocodeTimeoutRunnable);
        mainHandler.postDelayed(geocodeTimeoutRunnable, GEOCODE_TIMEOUT_MS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            geocoder.getFromLocation(latitude, longitude, 1, new Geocoder.GeocodeListener() {
                @Override public void onGeocode(@NonNull List<Address> addresses) {
                    mainHandler.removeCallbacks(geocodeTimeoutRunnable);
                    String text = formatAddress(addresses);
                    lastAddress = text;
                    binding.address.setText(text);
                }
                @Override public void onError(@Nullable String errorMessage) {
                    mainHandler.removeCallbacks(geocodeTimeoutRunnable);
                    Log.e(TAG, "Geocoder error: " + errorMessage);
                    lastAddress = null;
                    binding.address.setText(R.string.error_no_address);
                }
            });
        } else {
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
                    mainHandler.removeCallbacks(geocodeTimeoutRunnable);
                    lastAddress = finalText;
                    binding.address.setText(finalText);
                });
            });
        }
    }

    @NonNull
    private String formatAddress(@Nullable List<Address> addresses) {
        if (addresses == null || addresses.isEmpty()) return getString(R.string.error_no_address);
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

    // -------------------------------------------------------------------------
    //  Actions (Share / Copy / Maps / per-card tap-to-copy)
    // -------------------------------------------------------------------------

    private void wireActionButtons() {
        binding.btnShare.setOnClickListener(v -> shareCurrentLocation());
        binding.btnCopy.setOnClickListener(v -> copyToClipboard(getString(R.string.clipboard_label),
                buildShareText(), R.string.clipboard_copied));
        binding.btnMaps.setOnClickListener(v -> openInMaps());
    }

    private void wireCardTapToCopy() {
        binding.coordsCard.setOnClickListener(v -> {
            if (lastKnown == null) return;
            String text = String.format(Locale.US, "%.6f, %.6f",
                    lastKnown.getLatitude(), lastKnown.getLongitude());
            copyToClipboard("Coordinates", text, R.string.clipboard_coords_copied);
        });
        binding.addressCard.setOnClickListener(v -> {
            if (lastAddress == null || lastAddress.isEmpty()) return;
            copyToClipboard("Address", lastAddress.replace("\n", ", "),
                    R.string.clipboard_address_copied);
        });
    }

    private void copyToClipboard(@NonNull String label, @NonNull String text, int toastResId) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null) return;
        cm.setPrimaryClip(ClipData.newPlainText(label, text));
        // Android 13+ shows a system clipboard preview, so we only toast on older builds.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(this, toastResId, Toast.LENGTH_SHORT).show();
        }
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
            // Fall back to Google Maps web URL.
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
        sb.append(getString(R.string.share_prefix)).append("\n\n");
        sb.append(LocationFormatter.formatLatitude(lastKnown.getLatitude(), getCoordFormat()))
                .append(", ")
                .append(LocationFormatter.formatLongitude(lastKnown.getLongitude(), getCoordFormat()))
                .append("\n");
        if (lastKnown.hasAltitude()) {
            sb.append("Altitude: ")
                    .append(LocationFormatter.formatAltitude(lastKnown.getAltitude(), getAltitudeUnit()))
                    .append("\n");
        }
        if (lastAddress != null && !lastAddress.isEmpty()) {
            sb.append(lastAddress.replace("\n", ", ")).append("\n");
        }
        sb.append("\n").append(String.format(Locale.US,
                "https://www.google.com/maps/search/?api=1&query=%f,%f",
                lastKnown.getLatitude(), lastKnown.getLongitude()));
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    //  Settings (units & coord format)
    // -------------------------------------------------------------------------

    @NonNull
    private LocationFormatter.AltitudeUnit getAltitudeUnit() {
        return "ft".equals(prefs.getString(KEY_ALTITUDE_UNIT, "m"))
                ? LocationFormatter.AltitudeUnit.FEET
                : LocationFormatter.AltitudeUnit.METERS;
    }

    @NonNull
    private LocationFormatter.CoordFormat getCoordFormat() {
        return "dms".equals(prefs.getString(KEY_COORD_FORMAT, "decimal"))
                ? LocationFormatter.CoordFormat.DMS
                : LocationFormatter.CoordFormat.DECIMAL;
    }

    private void showSettingsDialog() {
        boolean isFeet = getAltitudeUnit() == LocationFormatter.AltitudeUnit.FEET;
        boolean isDms  = getCoordFormat()  == LocationFormatter.CoordFormat.DMS;

        // Build a 4-row choice list: 2 altitude options + 2 coord options.
        CharSequence[] items = new CharSequence[] {
                getString(R.string.settings_altitude_meters),
                getString(R.string.settings_altitude_feet),
                getString(R.string.settings_coord_decimal),
                getString(R.string.settings_coord_dms),
        };
        boolean[] checked = new boolean[] {
                !isFeet, isFeet, !isDms, isDms,
        };
        // We want exclusive choice within each group — use two single-choice dialogs.
        // Simpler: just use AlertDialog with single-choice for altitude first, then coord format.
        showAltitudeChoice(isFeet);
    }

    private void showAltitudeChoice(boolean isFeetCurrent) {
        CharSequence[] items = {
                getString(R.string.settings_altitude_meters),
                getString(R.string.settings_altitude_feet),
        };
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_altitude_header)
                .setSingleChoiceItems(items, isFeetCurrent ? 1 : 0, (dialog, which) -> {
                    prefs.edit().putString(KEY_ALTITUDE_UNIT, which == 1 ? "ft" : "m").apply();
                    dialog.dismiss();
                    showCoordChoice(getCoordFormat() == LocationFormatter.CoordFormat.DMS);
                })
                .setNegativeButton(R.string.dialog_close, null)
                .show();
    }

    private void showCoordChoice(boolean isDmsCurrent) {
        CharSequence[] items = {
                getString(R.string.settings_coord_decimal),
                getString(R.string.settings_coord_dms),
        };
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_coord_header)
                .setSingleChoiceItems(items, isDmsCurrent ? 1 : 0, (dialog, which) -> {
                    prefs.edit().putString(KEY_COORD_FORMAT, which == 1 ? "dms" : "decimal").apply();
                    dialog.dismiss();
                    refreshNow();
                })
                .setNegativeButton(R.string.dialog_close, null)
                .show();
    }

    // -------------------------------------------------------------------------
    //  Menu / about
    // -------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            refreshNow();
            return true;
        }
        if (id == R.id.menu_units) {
            showSettingsDialog();
            return true;
        }
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
