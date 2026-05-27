package com.nv.nvgeowatch;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Persisted list of {@link Waypoint}s.
 *
 * Stored as a JSON array string in a dedicated SharedPreferences file,
 * separate from app preferences so corruption in one can't take out the
 * other. A maximum of {@link #MAX_WAYPOINTS} entries is enforced — beyond
 * that the user should use a dedicated GPS app, not a one-screen utility.
 *
 * All methods are synchronous and main-thread-safe (file is tiny).
 */
public final class WaypointStore {
    public  static final int MAX_WAYPOINTS = 500;
    private static final String PREFS = "hiker_waypoints";
    private static final String KEY_LIST = "list";

    private WaypointStore() {}

    /** Returns the saved waypoints, most-recent first (by savedAt). */
    @NonNull
    public static List<Waypoint> load(@NonNull Context context) {
        SharedPreferences p = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = p.getString(KEY_LIST, null);
        if (raw == null || raw.isEmpty()) return new ArrayList<>();
        ArrayList<Waypoint> out = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                out.add(new Waypoint(
                        o.getLong("id"),
                        o.optString("name", ""),
                        o.getDouble("lat"),
                        o.getDouble("lon"),
                        o.optDouble("alt", Double.NaN),
                        o.optLong("ts", o.getLong("id"))
                ));
            }
        } catch (JSONException e) {
            // Corrupt file — drop it rather than crash on every launch.
            return new ArrayList<>();
        }
        Collections.sort(out, (a, b) -> Long.compare(b.savedAtEpochMs, a.savedAtEpochMs));
        return out;
    }

    /**
     * Append a waypoint. Returns null on success, or an error string if the
     * limit is reached.
     */
    @Nullable
    public static String add(@NonNull Context context, @NonNull Waypoint w) {
        List<Waypoint> all = load(context);
        if (all.size() >= MAX_WAYPOINTS) {
            return "Waypoint limit reached (" + MAX_WAYPOINTS + ")";
        }
        all.add(0, w);
        save(context, all);
        return null;
    }

    public static void delete(@NonNull Context context, long id) {
        List<Waypoint> all = load(context);
        List<Waypoint> next = new ArrayList<>(all.size());
        for (Waypoint w : all) if (w.id != id) next.add(w);
        save(context, next);
    }

    public static void rename(@NonNull Context context, long id, @NonNull String newName) {
        List<Waypoint> all = load(context);
        for (int i = 0; i < all.size(); i++) {
            Waypoint w = all.get(i);
            if (w.id == id) {
                all.set(i, w.withName(newName));
                save(context, all);
                return;
            }
        }
    }

    /** Replace the entire list. Used by the activity after edits. */
    public static void save(@NonNull Context context, @NonNull List<Waypoint> waypoints) {
        SharedPreferences p = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        JSONArray arr = new JSONArray();
        try {
            for (Waypoint w : waypoints) {
                JSONObject o = new JSONObject();
                o.put("id", w.id);
                o.put("name", w.name);
                o.put("lat", w.latitude);
                o.put("lon", w.longitude);
                if (!Double.isNaN(w.altitudeMeters)) o.put("alt", w.altitudeMeters);
                o.put("ts", w.savedAtEpochMs);
                arr.put(o);
            }
            p.edit().putString(KEY_LIST, arr.toString()).apply();
        } catch (JSONException e) {
            // Shouldn't happen — all keys are valid.
        }
    }
}
