package com.nv.nvgeowatch;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

/**
 * Wraps {@link GnssStatus.Callback} and exposes the two numbers a UI cares
 * about: how many satellites are visible, and how many are actually
 * contributing to the current fix. Requires API 24 (Nougat) and
 * ACCESS_FINE_LOCATION already granted.
 *
 * No new permissions required.
 */
public final class GnssStatusController {

    public interface Listener {
        /**
         * @param totalSatellites    satellites in view
         * @param usedInFixSatellites satellites that contributed to the last fix
         */
        void onSatellitesChanged(int totalSatellites, int usedInFixSatellites);
    }

    private final Context context;
    @Nullable private final LocationManager locationManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable private Listener listener;
    @Nullable private GnssStatus.Callback gnssCallback;

    public GnssStatusController(Context context) {
        this.context = context.getApplicationContext();
        this.locationManager =
                (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
    }

    public boolean isAvailable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && locationManager != null;
    }

    @SuppressLint("MissingPermission")
    public void start(@NonNull Listener listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;
        if (locationManager == null) return;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        this.listener = listener;
        registerGnssCallback();
    }

    public void stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && locationManager != null && gnssCallback != null) {
            locationManager.unregisterGnssStatusCallback(gnssCallback);
        }
        gnssCallback = null;
        listener = null;
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    private void registerGnssCallback() {
        gnssCallback = new GnssStatus.Callback() {
            @Override
            public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
                int total = status.getSatelliteCount();
                int used  = 0;
                for (int i = 0; i < total; i++) {
                    if (status.usedInFix(i)) used++;
                }
                final int totalSnap = total;
                final int usedSnap  = used;
                mainHandler.post(() -> {
                    if (listener != null) listener.onSatellitesChanged(totalSnap, usedSnap);
                });
            }
        };
        try {
            locationManager.registerGnssStatusCallback(gnssCallback, mainHandler);
        } catch (SecurityException ignore) {
            // Permission could have flipped off between check and use.
        }
    }
}
