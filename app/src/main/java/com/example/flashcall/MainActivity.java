package com.example.flashcall;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 100;
    private Button btn;
    private Camera camera;
    private Camera.Parameters parameters;
    private CameraManager cameraManager;
    private String cameraId;
    private boolean status = false;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        final List<String> reqPermissions = Arrays.asList(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_PHONE_STATE);
        final ArrayList<String> permissionsNeeded = getPermissionNeeded(new ArrayList<String>(reqPermissions));

        if (!permissionsNeeded.isEmpty()) {
            requestForPermission(permissionsNeeded.toArray(new String[permissionsNeeded.size()]));
        } else {
            try {
                init();
                broadcast();
            } catch (CameraAccessException e) {
                e.toString();
            }
        }
    }

    private void broadcast() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            ScheduleUtils.ScheduleUtils(getBaseContext());
        }else {
            Intent intent = new Intent(this,flash.class);
            startService(intent);
        }
    }

    private void init() throws CameraAccessException {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            cameraId = cameraManager.getCameraIdList()[0];
        }else {
            if(camera == null){
                camera = Camera.open();
                parameters = camera.getParameters();
            }
        }
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(status == false){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        try {
                            cameraManager.setTorchMode(cameraId, true);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }else {
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        camera.setParameters(parameters);
                        camera.startPreview();
                    }
                    status = true;
                }else {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        try {
                            cameraManager.setTorchMode(cameraId, false);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }else {
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        camera.setParameters(parameters);
                        camera.startPreview();
                    }
                    status = false;
                }
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        },2000);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Yêu cầu cấp quyền", Toast.LENGTH_SHORT).show();
                return;
            }else {
                try {
                    init();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private ArrayList<String> getPermissionNeeded( final ArrayList<String> reqPermissions) {
        final ArrayList<String> permissionNeeded = new ArrayList<String>(reqPermissions.size());
        for (String reqPermission : reqPermissions) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, reqPermission) != PackageManager.PERMISSION_GRANTED) {
                permissionNeeded.add(reqPermission);
            }
        }
        return permissionNeeded;
    }

    private void requestForPermission(final String[] permissions) {
        ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_CODE);
    }

}
