package com.example.disasternotifyapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.disasternotifyapp.blueToothTest.BlueToothActivity;

public class MainActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName(); // log
    private PermissionCheck permissionCheck;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button sendButton = findViewById(R.id.btn1);
        sendButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BlueToothActivity.class);
            startActivity(intent);
        });

        // 권한 요청 및 블루투스 설정
        permissionCheck = new PermissionCheck(this);
        permissionCheck.requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionCheck.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}