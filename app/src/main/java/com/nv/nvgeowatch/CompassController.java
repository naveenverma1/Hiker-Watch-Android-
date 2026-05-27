package com.nv.nvgeowatch;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.Nullable;

/**
 * Read the device's orientation from the rotation-vector sensor and expose
 * a normalised compass bearing (degrees clockwise from north, 0..360).
 *
 * The rotation vector is a fused sensor (accel + magnetometer + gyro on
 * devices that have one) — it's the recommended source for "which way is
 * the phone pointing" on modern Android because the fusion already does
 * the math we'd otherwise have to do ourselves.
 *
 * True-north correction is applied by adding the magnetic declination at
 * the user's current location, retrieved from {@link GeomagneticField}
 * (a built-in offline model).
 *
 * Lifecycle:
 *   - {@link #start(Listener)} from onResume
 *   - {@link #stop()} from onPause
 *   - {@link #updateLocation(Location)} every time we get a new fix, so
 *     the declination stays current as the user moves.
 *
 * No new permissions required.
 */
public final class CompassController {

    /** What the activity needs to know whenever the bearing changes. */
    public interface Listener {
        /**
         * @param magneticDeg bearing referenced to magnetic north, 0..360
         * @param trueDeg     bearing referenced to true north, 0..360
         *                    (equal to magneticDeg until we have a location)
         * @param accuracy    SensorManager.SENSOR_STATUS_*
         */
        void onBearingChanged(float magneticDeg, float trueDeg, int accuracy);
    }

    private final SensorManager sensorManager;
    @Nullable private final Sensor rotationSensor;
    @Nullable private final WindowManager windowManager;

    private final float[] rotationMatrix = new float[9];
    private final float[] remapped       = new float[9];
    private final float[] orientation    = new float[3];

    @Nullable private GeomagneticField magneticField;
    @Nullable private Listener listener;

    /**
     * The minimum change in degrees we treat as "moved enough to notify",
     * to keep the UI from flickering due to sensor noise.
     */
    private static final float MIN_DEGREE_CHANGE = 0.5f;
    private float lastMagneticDeg = Float.NaN;

    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR) return;
            handleRotation(event);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Forward the latest reading with the new accuracy so the UI can
            // show a calibration hint when accuracy is poor.
            if (listener != null && !Float.isNaN(lastMagneticDeg)) {
                float trueDeg = applyDeclination(lastMagneticDeg);
                listener.onBearingChanged(lastMagneticDeg, trueDeg, accuracy);
            }
        }
    };

    public CompassController(Context context) {
        sensorManager  = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager != null
                ? sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                : null;
        windowManager  = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    /** Whether this device exposes a rotation-vector sensor at all. */
    public boolean isAvailable() {
        return rotationSensor != null;
    }

    public void start(Listener listener) {
        this.listener = listener;
        if (sensorManager != null && rotationSensor != null) {
            sensorManager.registerListener(sensorListener, rotationSensor,
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop() {
        if (sensorManager != null) sensorManager.unregisterListener(sensorListener);
        listener = null;
    }

    /**
     * Update the device's known position so that we can compute the magnetic
     * declination at the user's exact location. Without this we still work,
     * but trueDeg == magneticDeg.
     */
    public void updateLocation(@Nullable Location loc) {
        if (loc == null) { magneticField = null; return; }
        magneticField = new GeomagneticField(
                (float) loc.getLatitude(),
                (float) loc.getLongitude(),
                (float) loc.getAltitude(),
                System.currentTimeMillis());
    }

    private void handleRotation(SensorEvent event) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

        // Compensate for screen rotation so the bearing always reflects the
        // physical top of the phone, regardless of portrait/landscape.
        int xAxis = SensorManager.AXIS_X;
        int yAxis = SensorManager.AXIS_Y;
        Display display = windowManager != null ? windowManager.getDefaultDisplay() : null;
        if (display != null) {
            switch (display.getRotation()) {
                case Surface.ROTATION_90:
                    xAxis = SensorManager.AXIS_Y;
                    yAxis = SensorManager.AXIS_MINUS_X;
                    break;
                case Surface.ROTATION_180:
                    xAxis = SensorManager.AXIS_MINUS_X;
                    yAxis = SensorManager.AXIS_MINUS_Y;
                    break;
                case Surface.ROTATION_270:
                    xAxis = SensorManager.AXIS_MINUS_Y;
                    yAxis = SensorManager.AXIS_X;
                    break;
                default: break;
            }
        }
        SensorManager.remapCoordinateSystem(rotationMatrix, xAxis, yAxis, remapped);
        SensorManager.getOrientation(remapped, orientation);

        float magneticDeg = (float) Math.toDegrees(orientation[0]);
        magneticDeg = (magneticDeg + 360f) % 360f;

        if (!Float.isNaN(lastMagneticDeg)
                && Math.abs(shortestAngularDelta(lastMagneticDeg, magneticDeg)) < MIN_DEGREE_CHANGE) {
            return;
        }
        lastMagneticDeg = magneticDeg;

        float trueDeg = applyDeclination(magneticDeg);
        if (listener != null) {
            listener.onBearingChanged(magneticDeg, trueDeg, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        }
    }

    private float applyDeclination(float magneticDeg) {
        if (magneticField == null) return magneticDeg;
        float t = magneticDeg + magneticField.getDeclination();
        return (t + 360f) % 360f;
    }

    /** Shortest signed delta between two bearings, in degrees in [-180, 180]. */
    private static float shortestAngularDelta(float a, float b) {
        float d = (b - a + 540f) % 360f - 180f;
        return d;
    }

    /** Compass-rose label for a bearing, e.g. "N", "NE", "ESE". */
    public static String cardinal(float deg) {
        String[] cards = {"N","NNE","NE","ENE","E","ESE","SE","SSE",
                          "S","SSW","SW","WSW","W","WNW","NW","NNW"};
        int idx = (int) Math.round(((deg % 360f) + 360f) % 360f / 22.5f) % 16;
        return cards[idx];
    }
}
