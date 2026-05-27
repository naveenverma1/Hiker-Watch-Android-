package com.nv.nvgeowatch;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nv.nvgeowatch.databinding.ActivityWaypointsBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Lists the user's saved waypoints, supports rename, delete, set-as-target,
 * and bulk GPX export.
 *
 * Receives the target-pick result back to {@link MainActivity} via
 * {@link #EXTRA_TARGET_LAT}/{@code _LON} on the returned Intent.
 */
public class WaypointsActivity extends AppCompatActivity {

    public static final String EXTRA_TARGET_LAT = "target_lat";
    public static final String EXTRA_TARGET_LON = "target_lon";
    public static final String EXTRA_TARGET_NAME = "target_name";

    private ActivityWaypointsBinding binding;
    private final List<Waypoint> data = new ArrayList<>();
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityWaypointsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar, (v, ins) -> {
            Insets bars = ins.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return ins;
        });
        ViewCompat.setOnApplyWindowInsetsListener(binding.waypointsList, (v, ins) -> {
            Insets bars = ins.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bars.bottom);
            return ins;
        });

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.waypoints_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.waypointsList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter();
        binding.waypointsList.setAdapter(adapter);

        reload();
    }

    private void reload() {
        data.clear();
        data.addAll(WaypointStore.load(this));
        adapter.notifyDataSetChanged();
        binding.emptyText.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
        binding.waypointsList.setVisibility(data.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.waypoints_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) { finish(); return true; }
        if (id == R.id.menu_export_gpx) { exportGpx(); return true; }
        if (id == R.id.menu_delete_all) { confirmDeleteAll(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void confirmDeleteAll() {
        if (data.isEmpty()) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_all_title)
                .setMessage(R.string.confirm_delete_all_message)
                .setPositiveButton(R.string.action_delete, (d, w) -> {
                    WaypointStore.save(this, new ArrayList<>());
                    reload();
                })
                .setNegativeButton(R.string.dialog_close, null)
                .show();
    }

    /**
     * Writes a GPX file to the app's cache and shares it via FileProvider so
     * the user can save it anywhere (Drive, Files, Gmail, etc.).
     */
    private void exportGpx() {
        if (data.isEmpty()) {
            Toast.makeText(this, R.string.waypoints_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        String gpx = GpxExporter.toGpx(data);
        try {
            File dir = new File(getCacheDir(), "exports");
            if (!dir.exists() && !dir.mkdirs()) throw new IOException("cache mkdir failed");
            String filename = "hikers-watch-waypoints.gpx";
            File out = new File(dir, filename);
            try (FileOutputStream fos = new FileOutputStream(out)) {
                fos.write(gpx.getBytes("UTF-8"));
            }
            android.net.Uri uri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", out);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("application/gpx+xml");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.gpx_share_subject));
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(Intent.createChooser(share, getString(R.string.gpx_share_chooser)));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.error_no_apps_to_handle, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, R.string.error_gpx_write, Toast.LENGTH_LONG).show();
        }
    }

    // -------------------------------------------------------------------------
    //  RecyclerView adapter
    // -------------------------------------------------------------------------

    private final class Adapter extends RecyclerView.Adapter<Holder> {
        @NonNull @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_waypoint, parent, false);
            return new Holder(v);
        }
        @Override public void onBindViewHolder(@NonNull Holder h, int position) {
            h.bind(data.get(position));
        }
        @Override public int getItemCount() { return data.size(); }
    }

    private final class Holder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final TextView coordsView;
        private final TextView savedView;

        Holder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.waypointName);
            coordsView = itemView.findViewById(R.id.waypointCoords);
            savedView = itemView.findViewById(R.id.waypointSavedAt);
            itemView.setOnClickListener(v -> showRowActions());
        }

        private Waypoint current;

        void bind(Waypoint w) {
            current = w;
            nameView.setText(w.name);
            coordsView.setText(String.format(Locale.US, "%.6f°, %.6f°",
                    w.latitude, w.longitude));
            savedView.setText(getString(R.string.waypoint_saved_at_label,
                    DateFormat.getMediumDateFormat(itemView.getContext())
                            .format(new Date(w.savedAtEpochMs))));
        }

        private void showRowActions() {
            if (current == null) return;
            CharSequence[] items = {
                    getString(R.string.action_set_as_target),
                    getString(R.string.action_rename),
                    getString(R.string.action_delete),
            };
            final Waypoint w = current;
            new AlertDialog.Builder(WaypointsActivity.this)
                    .setTitle(w.name)
                    .setItems(items, (d, which) -> {
                        if (which == 0) setAsTarget(w);
                        else if (which == 1) promptRename(w);
                        else if (which == 2) confirmDelete(w);
                    })
                    .show();
        }
    }

    private void setAsTarget(@NonNull Waypoint w) {
        Intent data = new Intent();
        data.putExtra(EXTRA_TARGET_LAT, w.latitude);
        data.putExtra(EXTRA_TARGET_LON, w.longitude);
        data.putExtra(EXTRA_TARGET_NAME, w.name);
        setResult(RESULT_OK, data);
        finish();
    }

    private void promptRename(@NonNull Waypoint w) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(w.name);
        input.setSelection(w.name.length());
        new AlertDialog.Builder(this)
                .setTitle(R.string.action_rename)
                .setView(input)
                .setPositiveButton(R.string.dialog_save, (d, which) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) return;
                    WaypointStore.rename(this, w.id, newName);
                    reload();
                })
                .setNegativeButton(R.string.dialog_close, null)
                .show();
    }

    private void confirmDelete(@NonNull Waypoint w) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_message, w.name))
                .setPositiveButton(R.string.action_delete, (d, which) -> {
                    WaypointStore.delete(this, w.id);
                    reload();
                })
                .setNegativeButton(R.string.dialog_close, null)
                .show();
    }

    /** Launch helper used by {@link MainActivity}. */
    public static Intent newIntent(@NonNull Context ctx) {
        return new Intent(ctx, WaypointsActivity.class);
    }
}
