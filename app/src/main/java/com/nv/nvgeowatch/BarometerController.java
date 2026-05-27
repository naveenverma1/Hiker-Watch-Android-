package com.nv.nvgeowatch;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Reads the barometric pressure sensor (TYPE_PRESSURE).
 *
 * On the (rare) devices that have a barometer this gives a much steadier
 * altitude than GPS once calibrated to a known sea-level pressure.
 * For now we report the raw pressure in hPa and the altitude assuming
 * standard sea-level pressure (1013.25 hPa). A calibration UI can be
 * layered on top in a later release.
 *
 * If the device has no pressure sensor, {@link #isAvailable()} returns
 * false and {@link #start} is a no-op, so callers can hide the row safely.
 */
public class BarometerController implements SensorEventListener {

    public interface Listener {
        /**
         * @param hPa            current pressure in hectopascals
         * @param altitudeMeters altitude above mean sea level using the
         *                       1013.25 hPa standard reference
         */
        void onPressureChanged(float hPa, float altitudeMeters);
    }

    @NonNull private final SensorManager sensorManager;
    @Nullable private final Sensor pressureSensor;
    @Nullable private Listener listener;

    public BarometerController(@NonNull Context context) {
        sensorManager = (SensorManager) context.getApplicationContext()
                .getSystemService(Context.SENSOR_SERVICE);
        pressureSensor = sensorManager == null
                ? null
                : sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }

    public boolean isAvailable() { return pressureSensor != null; }

    public void start(@NonNull Listener l) {
        this.listener = l;
        if (pressureSensor != null) {
            sensorManager.registerListener(this, pressureSensor,
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop() {
        if (pressureSensor != null) sensorManager.unregisterListener(this);
        this.listener = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_PRESSURE) return;
        if (listener == null) return;
        float hPa = event.values[0];
        float altMeters = SensorManager.getAltitude(
                SensorManager.PRESSURE_STANDARD_ATMOSPHERE, hPa);
        listener.onPressureChanged(hPa, altMeters);
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
