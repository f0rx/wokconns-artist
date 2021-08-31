package com.wokconns.wokconns;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;

import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

//import com.crashlytics.android.Crashlytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.wokconns.wokconns.databinding.ActivitySplashBinding;
import com.wokconns.wokconns.interfacess.Const;
import com.wokconns.wokconns.preferences.SharedPrefs;
import com.wokconns.wokconns.ui.activity.AppIntro;
import com.wokconns.wokconns.ui.activity.BaseActivity;
import com.wokconns.wokconns.utils.ProjectUtils;

//import io.fabric.sdk.android.Fabric;


public class SplashActivity extends AppCompatActivity {

    private SharedPrefs prefference;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 1003;
    private String[] permissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    private boolean cameraAccepted, storageAccepted, accessNetState, fineLoc, corasLoc;
    private Handler handler = new Handler();
    private static int SPLASH_TIME_OUT = 2000;
    Context mContext;
    ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fabric.with(this, new Crashlytics());

        ProjectUtils.Fullscreen(SplashActivity.this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        mContext = SplashActivity.this;
        prefference = SharedPrefs.getInstance(SplashActivity.this);

        FirebaseMessaging.getInstance().subscribeToTopic(Const.TOPIC_ARTIST)
                .addOnCompleteListener(task -> {

                });
    }

    Runnable mTask = new Runnable() {
        @Override
        public void run() {
            if (prefference.getBooleanValue(Const.IS_REGISTERED)) {
                Intent in = new Intent(mContext, BaseActivity.class);
                startActivity(in);
            } else {
                startActivity(new Intent(SplashActivity.this, AppIntro.class));
            }

            finish();
            overridePendingTransition(R.anim.anim_slide_in_left,
                    R.anim.anim_slide_out_left);
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasPermissions(SplashActivity.this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        } else {
            handler.postDelayed(mTask, SPLASH_TIME_OUT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
            try {

                cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                prefference.setBooleanValue(Const.CAMERA_ACCEPTED, cameraAccepted);

                storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                prefference.setBooleanValue(Const.STORAGE_ACCEPTED, storageAccepted);

                accessNetState = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                prefference.setBooleanValue(Const.MODIFY_AUDIO_ACCEPTED, accessNetState);

                fineLoc = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                prefference.setBooleanValue(Const.FINE_LOC, fineLoc);

                corasLoc = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                prefference.setBooleanValue(Const.CORAS_LOC, corasLoc);
                handler.postDelayed(mTask, 2000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}


