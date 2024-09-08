package com.example.disasternotifyapp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionCheck {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private final Activity activity;

    public PermissionCheck(Activity activity) {
        this.activity = activity;
    }

    public void requestPermissions() {
        List<String> permissions = new ArrayList<>();

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.BLUETOOTH);
        permissions.add(Manifest.permission.BLUETOOTH_ADMIN);

        // Android 12 이상에서 BLUETOOTH_CONNECT 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        }

        if (!hasPermissions(permissions.toArray(new String[0]))) {
            ActivityCompat.requestPermissions(activity, permissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            // 권한이 이미 허용된 경우 처리
            onAllPermissionsGranted();
        }
    }

    private boolean hasPermissions(String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    // 권한이 거부된 경우 처리 (예: Toast 메시지)
                    Toast.makeText(activity, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
                    break;
                }
            }

            if (allGranted) {
                // 모든 권한이 허용된 경우 처리
                onAllPermissionsGranted();
            }
        }
    }

    private void onAllPermissionsGranted() {
        // 모든 권한이 허용된 경우의 추가 로직
        Toast.makeText(activity, "모든 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
    }
}


