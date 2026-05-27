package com.nv.nvgeowatch;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/**
 * Transparent, no-UI activity invoked by the static app shortcut declared in
 * res/xml/shortcuts.xml. Pulls the last known location out of
 * {@link LastKnownLocationStore}, copies it to the clipboard, toasts on
 * older Androids (newer ones show a system clipboard preview), and finishes.
 *
 * The whole flow is sub-100ms so the shortcut feels native — no app launch,
 * no splash, no main screen flash.
 */
public final class CopyLocationShortcutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LastKnownLocationStore.Snapshot snap = LastKnownLocationStore.load(this);
        if (snap == null) {
            Toast.makeText(this, R.string.shortcut_no_fix_yet, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        String text = String.format(Locale.US, "%.6f, %.6f",
                snap.latitude, snap.longitude);
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText(
                    getString(R.string.clipboard_label), text));
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(this, getString(R.string.shortcut_copied, text),
                    Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
