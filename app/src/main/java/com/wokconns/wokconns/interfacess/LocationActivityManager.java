package com.wokconns.wokconns.interfacess;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStates;
import com.wokconns.wokconns.R;

import java.util.Map;
import java.util.concurrent.Callable;

public class LocationActivityManager extends AppCompatActivity {
    private static final String TAG = "LocationActivity.M.TAG";
    private static final int REQUEST_CODE_CHECK_SETTINGS = 338800;
    private LocationPermissionCallback permissionCallback;
    private Callable<Object> uCallable;
    protected boolean isShowingRationale = false;

    protected final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                    .RequestMultiplePermissions(), activityResultContracts());

//    protected final ActivityResultLauncher<Intent> activityResultLauncher =
//            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
//                    activityResultCallback());

    private ActivityResultCallback<Map<String, Boolean>> activityResultContracts() {
        return result -> {
            Boolean fineLocationGranted = null;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                fineLocationGranted = result.getOrDefault(
                        Manifest.permission.ACCESS_FINE_LOCATION, false);
            }
            Boolean coarseLocationGranted = null;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                coarseLocationGranted = result.getOrDefault(
                        Manifest.permission.ACCESS_COARSE_LOCATION, false);
            }

            if (fineLocationGranted != null && fineLocationGranted) {
                // Precise location access granted.
                showGPSRationale();
                postRequest();
            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                // Only approximate location access granted.
                showGPSRationale();
                postRequest();
            } else {
                // No location access granted.
                setPermissionCallback(false);
                Log.i(TAG, "User declined Location access!");
            }

            isShowingRationale = false;
        };
    }
//
//    private ActivityResultCallback<ActivityResult> activityResultCallback() {
//        return result -> {
//            int resultCode = result.getResultCode();
//            if (resultCode == Activity.RESULT_OK) {
//                Log.i(TAG, "Result code ==>> " + resultCode);
//            }
//        };
//    }


    public void setPermissionCallback(Boolean value) {
        if (permissionCallback != null)
            permissionCallback.permissionResult(value);
    }

    private void postRequest() {
        if (uCallable != null)
            try {
                setPermissionCallback(true);
                uCallable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public void requestLocationPermissions() {
        requestLocationPermissions(null);
    }

    public void requestLocationPermissions(LocationPermissionCallback callback) {
        requestLocationPermissions(callback, null);
    }

    public void requestLocationPermissions(LocationPermissionCallback callback, Callable<Object> runner) {
        permissionCallback = callback;
        uCallable = runner;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setPermissionCallback(true);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                setPermissionCallback(false);

                if (!isShowingRationale)
                    showInContextUI();

                isShowingRationale = true;
            }
        } else {
            launchPermissionRationale();
            setPermissionCallback(false);
        }
    }

    private void launchPermissionRationale() {
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void showInContextUI() {
        // Get the context assoc with this fragment
        // Will throw if none found!
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Grant Location Permission");

        alertDialog
                .setMessage(String.format("%s needs device location to function properly." +
                                "\nDo you want to enable it now?",
                        getResources().getString(R.string.app_name)));

        alertDialog.setPositiveButton("Sure",
                (dialog, which) -> launchPermissionRationale());

        alertDialog.setNegativeButton("No thanks",
                (dialog, which) -> {
                    dialog.cancel();
                    isShowingRationale = false;
                });

        alertDialog.show();
    }

    public void showGPSRationale() {
        try {
            final android.location.LocationManager manager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (!manager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER))
                enableLocationSettings();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enableLocationSettings() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(20000)
                .setFastestInterval(16000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        LocationServices
                .getSettingsClient(this)
                .checkLocationSettings(builder.build())
                .addOnSuccessListener(this, (response) -> {
                    LocationSettingsStates states = response.getLocationSettingsStates();
                    if (states != null) {
                        Log.i(TAG, "Is GPS present" + states.isGpsPresent());
                        Log.i(TAG, "Is GPS Usable" + states.isGpsUsable());
                        Log.i(TAG, "Is Location present" + states.isLocationPresent());
                        Log.i(TAG, "Is Location usable" + states.isLocationUsable());
                    }
                })
                .addOnFailureListener(this, ex -> {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) ex;
                        resolvable.startResolutionForResult(this, REQUEST_CODE_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                });
    }
}
