package com.nv.nvgeowatch;

import androidx.annotation.NonNull;

/**
 * One saved location. Immutable.
 *
 * Stored as a JSON object in SharedPreferences (see {@link WaypointStore}).
 * No DB / no ORM — keeps the app a single APK with zero schema migrations.
 */
public final class Waypoint {
    public final long   id;            // wall-clock when saved (also primary key)
    public final String name;
    public final double latitude;
    public final double longitude;
    public final double altitudeMeters; // may be NaN if not captured
    public final long   savedAtEpochMs;

    public Waypoint(long id, @NonNull String name,
                    double latitude, double longitude,
                    double altitudeMeters, long savedAtEpochMs) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitudeMeters = altitudeMeters;
        this.savedAtEpochMs = savedAtEpochMs;
    }

    @NonNull
    public Waypoint withName(@NonNull String newName) {
        return new Waypoint(id, newName, latitude, longitude,
                altitudeMeters, savedAtEpochMs);
    }
}
