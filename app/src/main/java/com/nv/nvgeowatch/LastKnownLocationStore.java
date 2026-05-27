package com.nv.nvgeowatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import androidx.annotation.Nullable;

/**
 * Tiny persistence for the most recent location fix so peripheral surfaces
 * (Quick Settings tile, "Copy my coords" app shortcut, widget) can hand the
 * user a value within ~100ms of being invoked — even if the main activity
 * has been killed.
 *
 * Stored in plain SharedPreferences (no sqlite, no Room, no Datastore — this
 * is one record, not a dataset). Values are written every time
 * {@code MainActivity#onLocationFix} succeeds and read by:
 *
 *   - {@link LocationCopyTileService} when the user taps the QS tile
 *   - {@link CopyLocationShortcutActivity} when the user taps the app-shortcut
 *
 * Nothing leaves the device. The data is cleared on uninstall.
 */
public final class LastKnownLocationStore {

    private static final String PREFS = "hiker_last_known";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";
    private static final String KEY_TIME = "time";

    private LastKnownLocationStore() {}

    public static void save(Context context, Location loc) {
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit()
                .putFloat(KEY_LAT, (float) loc.getLatitude())
                .putFloat(KEY_LON, (float) loc.getLongitude())
                .putLong(KEY_TIME, System.currentTimeMillis())
                .apply();
    }

    @Nullable
    public static Snapshot load(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!sp.contains(KEY_LAT) || !sp.contains(KEY_LON)) return null;
        return new Snapshot(
                sp.getFloat(KEY_LAT, 0f),
                sp.getFloat(KEY_LON, 0f),
                sp.getLong(KEY_TIME, 0L));
    }

    public static final class Snapshot {
        public final double latitude;
        public final double longitude;
        public final long timestampMs;
        Snapshot(double lat, double lon, long t) {
            this.latitude = lat;
            this.longitude = lon;
            this.timestampMs = t;
        }
        /** Whether this fix is fresh enough to use without re-querying GPS. */
        public boolean isFresh() {
            return System.currentTimeMillis() - timestampMs < 30L * 60 * 1000L; // 30 min
        }
    }
}
