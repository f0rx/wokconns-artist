package com.wokconns.wokconns

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.wokconns.wokconns.databinding.ActivitySplashBinding
import com.wokconns.wokconns.interfacess.Const
import com.wokconns.wokconns.preferences.SharedPrefs
import com.wokconns.wokconns.preferences.SharedPrefs.Companion.getInstance
import com.wokconns.wokconns.ui.activity.AppIntro
import com.wokconns.wokconns.ui.activity.BaseActivity
import com.wokconns.wokconns.utils.ProjectUtils

//import com.crashlytics.android.Crashlytics;
//import io.fabric.sdk.android.Fabric;
class SplashActivity : AppCompatActivity() {
    private var prefference: SharedPrefs? = null
    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private var cameraAccepted = false
    private var storageAccepted = false
    private var accessNetState = false
    private var fineLoc = false
    private var corasLoc = false
    private val handler = Handler()
    private lateinit var mContext: Context
    private lateinit var binding: ActivitySplashBinding
    private lateinit var crashlytics: FirebaseCrashlytics
    private lateinit var anayltics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Fabric.with(this, new Crashlytics());
        ProjectUtils.Fullscreen(this@SplashActivity)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        mContext = this@SplashActivity
        prefference = getInstance(this@SplashActivity)

        crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(true)

        anayltics = FirebaseAnalytics.getInstance(mContext)
        anayltics.setAnalyticsCollectionEnabled(true)

        FirebaseMessaging.getInstance().subscribeToTopic(Const.TOPIC_ARTIST)
            .addOnCompleteListener { task: Task<Void?>? -> }
    }

    var mTask = Runnable {
        if (prefference!!.getBooleanValue(Const.IS_REGISTERED)) {
            val `in` = Intent(mContext, BaseActivity::class.java)
            startActivity(`in`)
        } else {
            startActivity(Intent(this@SplashActivity, AppIntro::class.java))
        }
        finish()
        overridePendingTransition(
            R.anim.anim_slide_in_left,
            R.anim.anim_slide_out_left
        )
    }

    override fun onResume() {
        super.onResume()

        prefference?.getParentUser(Const.USER_DTO)?.let {
            anayltics.setUserProperty("user_id", it.user_id)
            anayltics.setUserProperty("user_name", it.name)
            anayltics.setUserProperty("user_email", it.email_id)
        }

        if (!hasPermissions(this@SplashActivity, *permissions)) {
            ActivityCompat.requestPermissions(
                this,
                permissions,
                REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS
            )
        } else {
            handler.postDelayed(mTask, SPLASH_TIME_OUT.toLong())
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
            try {
                cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                prefference?.setBooleanValue(Const.CAMERA_ACCEPTED, cameraAccepted)
                storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                prefference?.setBooleanValue(Const.STORAGE_ACCEPTED, storageAccepted)
                accessNetState = grantResults[2] == PackageManager.PERMISSION_GRANTED
                prefference?.setBooleanValue(Const.MODIFY_AUDIO_ACCEPTED, accessNetState)
                fineLoc = grantResults[3] == PackageManager.PERMISSION_GRANTED
                prefference?.setBooleanValue(Const.FINE_LOC, fineLoc)
                corasLoc = grantResults[4] == PackageManager.PERMISSION_GRANTED
                prefference?.setBooleanValue(Const.CORAS_LOC, corasLoc)
                handler.postDelayed(mTask, 2000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 1003
        private const val SPLASH_TIME_OUT = 2000

        fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            permission
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return false
                    }
                }
            }
            return true
        }
    }
}