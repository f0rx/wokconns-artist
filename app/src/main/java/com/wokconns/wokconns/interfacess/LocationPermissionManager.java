package com.wokconns.wokconns.interfacess;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;

import com.wokconns.wokconns.R;

import java.util.concurrent.Callable;

public class LocationPermissionManager extends Fragment {
    private Callable<Object> uCallable;
    protected boolean isShowingRationale = false;

    protected final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), requestCallback());

    private ActivityResultCallback<Boolean> requestCallback() {
        return isGranted -> {
            if (isGranted) {
                if (uCallable != null)
                    try {
                        uCallable.call();
                    } catch (Exception ignored) {
//                        ignored.printStackTrace();
                    }
            } else {
                Log.i("LocationPermission", "User declined Location access!");
            }

            isShowingRationale = false;
        };
    }

    protected void requestLocationPermissions() {
        requestLocationPermissions(null);
    }

    protected void requestLocationPermissions(Callable<Object> callable) {
        uCallable = callable;
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    protected void showInContextUI(Context context) {
        isShowingRationale = true;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setTitle("Enable Device GPS!");

        alertDialog
                .setMessage(String.format("GPS is not enabled! \n\n%s needs GPS enabled to function properly." +
                                "\nDo you want to enable it now?",
                        context.getResources().getString(R.string.app_name)));

        alertDialog.setPositiveButton("Alright",
                (dialog, which) -> {
                    requestLocationPermissions();
                });

        alertDialog.setNegativeButton("No thanks",
                (dialog, which) -> {
                    dialog.cancel();
                    isShowingRationale = false;
                });

        alertDialog.show();
    }
}
