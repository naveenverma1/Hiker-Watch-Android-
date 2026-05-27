package com.nv.nvgeowatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Host-side unit tests for {@link LocationFormatter}.
 *
 * Locked to {@code Locale.US} formatting because the formatter itself uses
 * {@code Locale.US} for decimal points / digit grouping.
 */
public class LocationFormatterTest {

    @Test
    public void formatLatitude_decimal_positiveNorthern() {
        assertEquals("+36.578600°",
                LocationFormatter.formatLatitude(36.5786, LocationFormatter.CoordFormat.DECIMAL));
    }

    @Test
    public void formatLatitude_decimal_negativeSouthern() {
        assertEquals("-33.868820°",
                LocationFormatter.formatLatitude(-33.86882, LocationFormatter.CoordFormat.DECIMAL));
    }

    @Test
    public void formatLatitude_dms_northern() {
        // 36.5786° = 36° 34' 43.0" N
        String s = LocationFormatter.formatLatitude(36.5786, LocationFormatter.CoordFormat.DMS);
        assertEquals("36° 34' 43.0\" N", s);
    }

    @Test
    public void formatLatitude_dms_southern_hemisphereIsS() {
        String s = LocationFormatter.formatLatitude(-33.86882, LocationFormatter.CoordFormat.DMS);
        assertTrue("expected S hemisphere, got " + s, s.endsWith(" S"));
    }

    @Test
    public void formatLongitude_dms_westernHemisphere() {
        String s = LocationFormatter.formatLongitude(-118.292298, LocationFormatter.CoordFormat.DMS);
        assertTrue("expected W hemisphere, got " + s, s.endsWith(" W"));
    }

    @Test
    public void formatAltitude_meters() {
        assertEquals("4421.0 m",
                LocationFormatter.formatAltitude(4421.0, LocationFormatter.AltitudeUnit.METERS));
    }

    @Test
    public void formatAltitude_feet_convertsCorrectly() {
        // 4421 m = 14_504.6 ft (rounded to 1 decimal)
        assertEquals("14504.6 ft",
                LocationFormatter.formatAltitude(4421.0, LocationFormatter.AltitudeUnit.FEET));
    }

    @Test
    public void formatAltitude_negativeMeters_belowSeaLevel() {
        // Dead Sea is ~ -430 m
        assertEquals("-430.0 m",
                LocationFormatter.formatAltitude(-430.0, LocationFormatter.AltitudeUnit.METERS));
    }
}
