package com.mg4.launcher.swipe;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mg4.launcher.swipe.update.UpdateHook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private PackageManager packageManager;
    private AppListAdapter adapter;
    private List<ApplicationInfo> allApps;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        preferencesManager = new PreferencesManager(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView explanationText = findViewById(R.id.explanation_text);
        explanationText.setText(R.string.explanation_text);

        TextView versionText = findViewById(R.id.version_text);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionText.setText(getString(R.string.version_label, versionName));
        } catch (PackageManager.NameNotFoundException e) {
            versionText.setText("");
        }

        String currentPackageName = getPackageName();
        packageManager = getPackageManager();
        List<ApplicationInfo> userApps = new ArrayList<>();

        for (ApplicationInfo appInfo : packageManager.getInstalledApplications(PackageManager.GET_META_DATA)) {
            if (!appInfo.packageName.equals(currentPackageName)) {
                userApps.add(appInfo);
            }
        }

        Collections.sort(userApps, (app1, app2) -> {
            String label1 = app1.loadLabel(packageManager).toString();
            String label2 = app2.loadLabel(packageManager).toString();
            return label1.compareToIgnoreCase(label2);
        });

        String selectedPackage = preferencesManager.getSelectedPackage();
        if (selectedPackage == null) {
            for (ApplicationInfo appInfo : userApps) {
                if (appInfo.packageName.equals("com.teslacoilsw.launcher")) {
                    selectedPackage = appInfo.packageName;
                    preferencesManager.saveSelectedPackage(selectedPackage);
                    break;
                }
            }
        }

        ListView listView = findViewById(R.id.app_list);
        listView.setEmptyView(findViewById(R.id.app_list_empty));
        adapter = new AppListAdapter(this, userApps, selectedPackage);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            ApplicationInfo selectedApp = adapter.getItem(position);
            if (selectedApp == null) {
                return;
            }
            preferencesManager.saveSelectedPackage(selectedApp.packageName);
            adapter.setSelectedPackage(selectedApp.packageName);
            adapter.notifyDataSetChanged();
            Toast.makeText(MainActivity.this, getString(R.string.selected_app, selectedApp.packageName), Toast.LENGTH_SHORT).show();
        });

        Button toggleSystemAppsButton = findViewById(R.id.toggle_system_apps_button);
        toggleSystemAppsButton.setOnClickListener(v -> {
            adapter.toggleSystemAppsVisibility();
            toggleSystemAppsButton.setText(adapter.isSystemAppsVisible() ? getString(R.string.hide_system_apps) : getString(R.string.show_system_apps));
        });

        CompoundButton switchBackButton = findViewById(R.id.switch_back_button);

        // The switch label reads "Hide the back button", so checked == hidden.
        switchBackButton.setChecked(!preferencesManager.isBackButtonVisible());

        switchBackButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setBackButtonVisible(!isChecked);
            stopSwipeService();
            startSwipeService();
        });

        CompoundButton switchSwapAreas = findViewById(R.id.switch_swap_areas);
        switchSwapAreas.setChecked(preferencesManager.isSwipeAreasSwapped());
        updateHelpLabels(preferencesManager.isSwipeAreasSwapped());

        switchSwapAreas.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setSwipeAreasSwapped(isChecked);
            updateHelpLabels(isChecked);
            stopSwipeService();
            startSwipeService();
        });

        CompoundButton switchShowLoader = findViewById(R.id.switch_show_loader);
        switchShowLoader.setChecked(preferencesManager.isShowLoader());
        // The loader preference is read on every swipe in SwipeService, so no service
        // restart is needed for the change to take effect.
        switchShowLoader.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferencesManager.setShowLoader(isChecked));

        // The manual update check only exists on the unstable channel — a stable build has
        // no updater code and no INTERNET permission.
        Button checkUpdates = findViewById(R.id.check_updates_button);
        if (BuildConfig.OTA_ENABLED) {
            checkUpdates.setOnClickListener(v -> UpdateHook.checkInBackground(this, true));
            // Silent check on open; prompts nothing unless a newer build exists.
            UpdateHook.checkInBackground(this);
        } else {
            checkUpdates.setVisibility(android.view.View.GONE);
        }

        startSwipeService();
    }

    private void updateHelpLabels(boolean swapped) {
        TextView leftHelp = findViewById(R.id.textView);
        TextView rightHelp = findViewById(R.id.textView2);
        leftHelp.setText(swapped
                ? R.string.help_swipe_open
                : R.string.help_swipe_back);
        rightHelp.setText(swapped
                ? R.string.help_swipe_back
                : R.string.help_swipe_open);
        applyHelpStyle(leftHelp, swapped);
        applyHelpStyle(rightHelp, !swapped);
    }

    private void applyHelpStyle(TextView help, boolean opensApp) {
        help.setBackgroundResource(opensApp ? R.color.mg4_accent : R.color.mg4_surface_raised);
        help.setTextColor(ContextCompat.getColor(this,
                opensApp ? R.color.mg4_on_accent : R.color.mg4_text_primary));
    }

    private void stopSwipeService() {
        Intent intent = new Intent(this, SwipeService.class);
        stopService(intent);
    }

    private void startSwipeService() {
        Intent intent = new Intent(this, SwipeService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
