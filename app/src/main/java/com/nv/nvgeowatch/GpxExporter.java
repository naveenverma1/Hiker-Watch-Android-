package com.nv.nvgeowatch;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Serializes a {@link Waypoint} list to GPX 1.1.
 *
 * <p>GPX is the de-facto interchange format every GPS app reads
 * (Garmin, AllTrails, Komoot, OsmAnd, gpsies, Caltopo). One self-contained
 * static method — no external XML library, just StringBuilder.
 *
 * <p>The output is well-formed against the official schema:
 * <a href="https://www.topografix.com/GPX/1/1/gpx.xsd">topografix.com/GPX/1/1/gpx.xsd</a>.
 */
public final class GpxExporter {

    private GpxExporter() {}

    /**
     * @param waypoints list to export; may be empty (returns a valid empty GPX)
     * @return UTF-8 GPX 1.1 document string
     */
    @NonNull
    public static String toGpx(@NonNull List<Waypoint> waypoints) {
        SimpleDateFormat iso = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        iso.setTimeZone(TimeZone.getTimeZone("UTC"));

        StringBuilder sb = new StringBuilder(512 + waypoints.size() * 256);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<gpx version=\"1.1\" creator=\"Hiker's Watch\"")
          .append(" xmlns=\"http://www.topografix.com/GPX/1/1\"")
          .append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")
          .append(" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1")
          .append(" http://www.topografix.com/GPX/1/1/gpx.xsd\">\n");
        sb.append("  <metadata>\n");
        sb.append("    <name>Hiker's Watch waypoints</name>\n");
        sb.append("    <time>").append(iso.format(new Date())).append("</time>\n");
        sb.append("  </metadata>\n");

        for (Waypoint w : waypoints) {
            sb.append(String.format(Locale.US,
                    "  <wpt lat=\"%.6f\" lon=\"%.6f\">\n", w.latitude, w.longitude));
            if (!Double.isNaN(w.altitudeMeters)) {
                sb.append(String.format(Locale.US,
                        "    <ele>%.1f</ele>\n", w.altitudeMeters));
            }
            sb.append("    <time>").append(iso.format(new Date(w.savedAtEpochMs)))
              .append("</time>\n");
            sb.append("    <name>").append(escape(w.name)).append("</name>\n");
            sb.append("  </wpt>\n");
        }
        sb.append("</gpx>\n");
        return sb.toString();
    }

    /** Escape the five XML special chars. */
    @NonNull
    private static String escape(@NonNull String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&':  out.append("&amp;"); break;
                case '<':  out.append("&lt;"); break;
                case '>':  out.append("&gt;"); break;
                case '"':  out.append("&quot;"); break;
                case '\'': out.append("&apos;"); break;
                default:
                    if (c < 0x20 && c != '\n' && c != '\r' && c != '\t') {
                        // Strip control chars that aren't valid in XML.
                        break;
                    }
                    out.append(c);
            }
        }
        return out.toString();
    }
}
