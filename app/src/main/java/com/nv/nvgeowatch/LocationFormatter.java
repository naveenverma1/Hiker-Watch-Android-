package com.nv.nvgeowatch;

import java.util.Locale;

/**
 * Pure functions for converting a raw latitude/longitude/altitude into
 * user-facing strings. Decoupled from the Android framework so it can be
 * unit-tested without an instrumentation runner.
 */
public final class LocationFormatter {

    public enum AltitudeUnit { METERS, FEET }
    public enum CoordFormat  { DECIMAL, DMS }

    private static final double METERS_TO_FEET = 3.28084;

    private LocationFormatter() {}

    public static String formatLatitude(double lat, CoordFormat format) {
        if (format == CoordFormat.DMS) {
            return formatDms(Math.abs(lat), lat >= 0 ? "N" : "S");
        }
        return String.format(Locale.US, "%+.6f°", lat);
    }

    public static String formatLongitude(double lon, CoordFormat format) {
        if (format == CoordFormat.DMS) {
            return formatDms(Math.abs(lon), lon >= 0 ? "E" : "W");
        }
        return String.format(Locale.US, "%+.6f°", lon);
    }

    public static String formatAltitude(double meters, AltitudeUnit unit) {
        if (unit == AltitudeUnit.FEET) {
            return String.format(Locale.US, "%.1f ft", meters * METERS_TO_FEET);
        }
        return String.format(Locale.US, "%.1f m", meters);
    }

    /** Convert a signed decimal degree value into "12° 20' 44.4\" X" form. */
    private static String formatDms(double absDeg, String hemisphere) {
        int degrees = (int) absDeg;
        double minutesFull = (absDeg - degrees) * 60.0;
        int minutes = (int) minutesFull;
        double seconds = (minutesFull - minutes) * 60.0;
        return String.format(Locale.US, "%d° %d' %.1f\" %s",
                degrees, minutes, seconds, hemisphere);
    }
}
