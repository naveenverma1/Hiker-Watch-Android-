package com.nv.nvgeowatch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.shredzone.commons.suncalc.MoonIllumination;
import org.shredzone.commons.suncalc.SunTimes;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Pure-function astronomy lookup for the user's current location.
 *
 * Wraps <a href="https://github.com/shred/commons-suncalc">commons-suncalc</a> so
 * the rest of the app sees only an immutable {@link Sky} value.
 *
 * Why a thin wrapper?
 *   - Keeps the suncalc API isolated to one file (easy swap later if needed).
 *   - Pre-formats time strings in the device's locale + zone so the activity
 *     doesn't have to think about ZonedDateTime.
 *   - Provides a single "is it always day / always night here" flag so the
 *     UI can show a polar-region message instead of dashes.
 */
public final class AstronomyController {

    /** Immutable result. Any field that's null means "doesn't apply today". */
    public static final class Sky {
        @Nullable public final String sunriseLocal;     // "06:14"
        @Nullable public final String sunsetLocal;      // "20:42"
        @Nullable public final String solarNoonLocal;   // "13:28"
        @Nullable public final String dayLength;        // "14h 28m"
        public final boolean         alwaysUp;          // polar day
        public final boolean         alwaysDown;        // polar night
        public final double          moonPhaseFraction; // 0..1; 0 new, 0.5 full
        public final double          moonIllumination;  // 0..1
        public final String          moonPhaseName;     // "Waxing Gibbous"

        Sky(@Nullable String sunriseLocal,
            @Nullable String sunsetLocal,
            @Nullable String solarNoonLocal,
            @Nullable String dayLength,
            boolean alwaysUp,
            boolean alwaysDown,
            double moonPhaseFraction,
            double moonIllumination,
            String moonPhaseName) {
            this.sunriseLocal = sunriseLocal;
            this.sunsetLocal = sunsetLocal;
            this.solarNoonLocal = solarNoonLocal;
            this.dayLength = dayLength;
            this.alwaysUp = alwaysUp;
            this.alwaysDown = alwaysDown;
            this.moonPhaseFraction = moonPhaseFraction;
            this.moonIllumination = moonIllumination;
            this.moonPhaseName = moonPhaseName;
        }
    }

    private static final DateTimeFormatter HHMM =
            DateTimeFormatter.ofPattern("HH:mm", Locale.US);

    @NonNull
    public static Sky compute(double lat, double lon) {
        return compute(lat, lon, new Date(), TimeZone.getDefault());
    }

    @NonNull
    public static Sky compute(double lat, double lon, @NonNull Date when, @NonNull TimeZone tz) {
        SunTimes st = SunTimes.compute()
                .on(when)
                .timezone(tz)
                .at(lat, lon)
                .execute();

        boolean alwaysUp   = st.isAlwaysUp();
        boolean alwaysDown = st.isAlwaysDown();

        String sunrise  = st.getRise() == null ? null : st.getRise().format(HHMM);
        String sunset   = st.getSet()  == null ? null : st.getSet().format(HHMM);
        String noon     = st.getNoon() == null ? null : st.getNoon().format(HHMM);

        String dayLen = null;
        if (st.getRise() != null && st.getSet() != null) {
            ZonedDateTime rise = st.getRise();
            ZonedDateTime set  = st.getSet();
            if (set.isBefore(rise)) {
                // Sun set this morning, will rise tomorrow — show next day length.
                set = set.plusDays(1);
            }
            Duration d = Duration.between(rise, set);
            long h = d.toHours();
            long m = d.toMinutes() % 60;
            dayLen = String.format(Locale.US, "%dh %02dm", h, m);
        }

        MoonIllumination mi = MoonIllumination.compute()
                .on(when)
                .timezone(tz)
                .execute();
        double phase = mi.getPhase() + 180.0;        // shift -180..180 → 0..360
        double phaseFrac = (phase / 360.0) % 1.0;    // 0..1
        double illum = mi.getFraction();             // 0..1
        String phaseName = nameForPhase(phaseFrac);

        return new Sky(sunrise, sunset, noon, dayLen, alwaysUp, alwaysDown,
                phaseFrac, illum, phaseName);
    }

    /**
     * Map a 0..1 phase fraction to a human-readable name.
     * 0 = new, 0.25 = first quarter, 0.5 = full, 0.75 = last quarter.
     */
    @NonNull
    private static String nameForPhase(double f) {
        f = ((f % 1.0) + 1.0) % 1.0;
        if (f < 0.03 || f > 0.97) return "New Moon";
        if (f < 0.22) return "Waxing Crescent";
        if (f < 0.28) return "First Quarter";
        if (f < 0.47) return "Waxing Gibbous";
        if (f < 0.53) return "Full Moon";
        if (f < 0.72) return "Waning Gibbous";
        if (f < 0.78) return "Last Quarter";
        return "Waning Crescent";
    }

    private AstronomyController() {}
}
