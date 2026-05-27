package com.nv.nvgeowatch;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.Locale;

/**
 * Quick Settings tile: pull down the notification shade, tap "Hiker's Watch",
 * the current coordinates are copied to the clipboard.
 *
 * Surfaces a stale-data hint via the tile state, and an explicit "no fix yet"
 * message when the app has never run before. Reads from
 * {@link LastKnownLocationStore} — does not request location itself, since
 * a tile only has a few hundred ms of execution time per tap.
 *
 * Requires API 24 (Nougat). Declared in AndroidManifest with the
 * {@code android.permission.BIND_QUICK_SETTINGS_TILE} permission, which is
 * a system-granted permission (no user prompt).
 */
@RequiresApi(Build.VERSION_CODES.N)
public class LocationCopyTileService extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();
        refreshTileState();
    }

    @Override
    public void onClick() {
        super.onClick();
        LastKnownLocationStore.Snapshot snap = LastKnownLocationStore.load(this);
        if (snap == null) {
            showToast(R.string.tile_no_fix_yet);
            return;
        }
        String text = String.format(Locale.US, "%.6f, %.6f",
                snap.latitude, snap.longitude);
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText(
                    getString(R.string.clipboard_label), text));
        }
        // Android 13+ shows its own clipboard preview, so suppress duplicate toast.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(this, getString(R.string.tile_copied, text),
                    Toast.LENGTH_SHORT).show();
        }
        refreshTileState();
    }

    private void refreshTileState() {
        Tile tile = getQsTile();
        if (tile == null) return;
        LastKnownLocationStore.Snapshot snap = LastKnownLocationStore.load(this);
        if (snap == null) {
            tile.setLabel(getString(R.string.app_name));
            tile.setSubtitle(getString(R.string.tile_no_fix_yet));
            tile.setState(Tile.STATE_INACTIVE);
        } else {
            tile.setLabel(getString(R.string.app_name));
            tile.setSubtitle(String.format(Locale.US, "%.4f, %.4f",
                    snap.latitude, snap.longitude));
            tile.setState(snap.isFresh() ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        }
        tile.updateTile();
    }

    private void showToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }
}
