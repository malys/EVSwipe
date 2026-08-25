package com.evsuite.swipe;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_OVERLAY_PERMISSION = 1000;
    private static final int REQUEST_CODE_ACCESSIBILITY_PERMISSION = 1001;
    private static final int REQUEST_CODE_NOTIFICATIONS = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermissions();
    }

    private void checkPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission();
        } else if (!isAccessibilityServiceEnabled(this, AccService.class)) {
            requestAccessibilityPermission();
        } else {
            requestNotificationPermission();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Asked for once the two permissions that matter are in place, and never blocked on: the
     * notification is the sign that the overlay service is running, not the overlay itself.
     * It was declared and never requested, which on Android 13 and later means the service
     * runs with no visible trace at all.
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATIONS);
    }

    private void requestOverlayPermission() {
        setContentView(R.layout.activity_permission_overlay);
        Button grantOverlayPermissionButton = findViewById(R.id.buttonGrantOverlayPermission);
        grantOverlayPermissionButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION);
        });
    }

    private void requestAccessibilityPermission() {
        setContentView(R.layout.activity_permission_accessibility);
        Button grantAccessibilityPermissionButton = findViewById(R.id.buttonGrantAccessibilityPermission);
        grantAccessibilityPermissionButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, REQUEST_CODE_ACCESSIBILITY_PERMISSION);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        checkPermissions();
    }

    private boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> service) {
        String serviceId = context.getPackageName() + "/" + service.getName();
        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        if (enabledServices != null) {
            for (String enabledService : enabledServices.split(":")) {
                if (enabledService.equalsIgnoreCase(serviceId)) {
                    return true;
                }
            }
        }
        return false;
    }
}
