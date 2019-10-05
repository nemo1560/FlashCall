package com.example.flashcall;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class flash extends Service {
    private Camera camera;
    private Camera.Parameters parameters;
    private boolean iFlash;
    private CameraManager cameraManager;
    private String cameraId;
    private int status=0;
    private Thread thread;
    private Handler handler = new Handler();
    private BroadcastReceiver callReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == Intents.call){
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                MyPhoneStateListener myPhoneStateListener = new MyPhoneStateListener();
                telephonyManager.listen(myPhoneStateListener,PhoneStateListener.LISTEN_CALL_STATE);
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SERVICE","!@#$%$%&%^*&^*");
        IntentFilter intentFilter = new IntentFilter(Intents.call);
        registerReceiver(callReceiver,intentFilter);
        try {
            camera();
            startThread();
        } catch (@SuppressLint("NewApi") CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void startThread() {
        if(thread == null){
            thread = new Thread(runnable);
            thread.start();
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            sendBroadcast(new Intent(Intents.call));
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runnable.run();
        }
    };

    private void camera() throws CameraAccessException {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            cameraId = cameraManager.getCameraIdList()[0];
        }else {
            if(camera == null){
                camera = Camera.open();
                parameters = camera.getParameters();
            }
        }
    }

    private void turnOn(){
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
        iFlash = false;
    }

    private void turnOff(){
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
        iFlash = true;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        onCreate();
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            super.onCallStateChanged(state, phoneNumber);
            Log.d("CALLNUMBER",phoneNumber);
            Log.d("FLASH","@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            if(state == TelephonyManager.CALL_STATE_RINGING){
                if(status==0){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(iFlash){
                                turnOn();
                            }else {
                                turnOff();
                            }
                            status = 1;
                            handler.postDelayed(this,500);
                        }
                    });
                }
            }else if(state == TelephonyManager.CALL_STATE_IDLE){
                handler.removeCallbacksAndMessages(null);
                status = 0;
                turnOff();
            }
        }
    }
}
