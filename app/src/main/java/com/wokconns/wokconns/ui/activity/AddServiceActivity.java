package com.wokconns.wokconns.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cocosw.bottomsheet.BottomSheet;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.ActivityAddServiceBinding;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.preferences.SharedPrefrence;
import com.wokconns.wokconns.utils.ImageCompression;
import com.wokconns.wokconns.utils.MainFragment;
import com.wokconns.wokconns.utils.ProjectUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class AddServiceActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = AddServiceActivity.class.getCanonicalName();
    ActivityAddServiceBinding addServiceBinding;
    Context context;
    SharedPrefrence sharedPrefrence;
    UserDTO userDTO;
    String user_id = "";

    public static final int PICK_IMAGE_GALLERY_SERVICE = 1, CROP_IMAGE_GALLERY_SERVICE = 2;
    public static final int PICK_FROM_CAMERA = 3, CROP_CAMERA_IMAGE = 4;
    Uri picUri;
    File file_service;
    ImageCompression imageCompression;
    String pathOfImage;

    BottomSheet.Builder builder;
    String imageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addServiceBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_service);
        init();
    }

    public void init() {
        context = AddServiceActivity.this;
        sharedPrefrence = SharedPrefrence.getInstance(context);
        userDTO = sharedPrefrence.getParentUser(Consts.USER_DTO);
        user_id = userDTO.getUser_id();

        imagePickerSheet();

        addServiceBinding.llBack.setOnClickListener(this);
        addServiceBinding.cardViewServiceImage.setOnClickListener(this);
        addServiceBinding.btnServicesSave.setOnClickListener(this);
    }

    public void addService() {
        HashMap<String, File> fileParams = new HashMap<>();
        if (file_service != null) {
            fileParams.put(Consts.PRODUCT_IMAGE, file_service);
        }
        ProjectUtils.showProgressDialog(context, true, getResources().getString(R.string.please_wait));
        new HttpsRequest(Consts.ADD_PRODUCT_API, (getParamsAddService()), fileParams, context).imagePost(TAG, (flag, msg, response) -> {
            ProjectUtils.pauseProgressDialog();
            if (flag) {
                ProjectUtils.showToast(context, msg);

                finish();
//                    arrayList = new Gson().fromJson(response.getJSONArray("data").toString(), getUserImage);

            } else {
                ProjectUtils.showToast(context, msg);
            }
        });
    }

    protected HashMap<String, String> getParamsAddService() {
        HashMap<String, String> paramsAddService = new HashMap<>();
        paramsAddService.put(Consts.USER_ID, userDTO.getUser_id());
        paramsAddService.put(Consts.PRODUCT_NAME, ProjectUtils.getEditTextValue(addServiceBinding.etProductName));
        paramsAddService.put(Consts.PRICE, ProjectUtils.getEditTextValue(addServiceBinding.etProductPrice));
        return paramsAddService;
    }

    public boolean validateServiceName() {
        if (!ProjectUtils.isEditTextFilled(addServiceBinding.etProductName)) {
            addServiceBinding.etProductName.setError(getResources().getString(R.string.your_product_name_msg));
            addServiceBinding.etProductName.requestFocus();
            return false;
        } else {
            addServiceBinding.etProductName.setError(null);
            addServiceBinding.etProductName.clearFocus();
            return true;
        }
    }

    public boolean validateServicePrice() {
        if (!ProjectUtils.isEditTextFilled(addServiceBinding.etProductPrice)) {
            addServiceBinding.etProductPrice.setError(getResources().getString(R.string.your_product_price_msg));
            addServiceBinding.etProductPrice.requestFocus();
            return false;
        } else {
            addServiceBinding.etProductPrice.setError(null);
            addServiceBinding.etProductPrice.clearFocus();
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.btnServicesSave:
                submit();
                break;
            case R.id.cardViewServiceImage:
                builder.show();
                break;
            case R.id.ll_delete:
                file_service = new File("");
                addServiceBinding.rlContainer.setVisibility(View.GONE);
                addServiceBinding.cardViewServiceImage.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void submit() {
        if (!validateServiceName()) {
            return;
        } else if (!validateServicePrice()) {
            return;
        } else if (file_service == null) {
            ProjectUtils.showToast(context, getResources().getString(R.string.service_image_val));
            return;
        } else {
            if (NetworkManager.isConnectToInternet(context)) {
                addService();
            } else {
                ProjectUtils.showToast(context, context.getResources().getString(R.string.internet_concation));
            }
        }
    }

    public void profilePhotoGallery() {
        if (ProjectUtils.hasPermissionInManifest(context, PICK_FROM_CAMERA, Manifest.permission.CAMERA)) {
            if (ProjectUtils.hasPermissionInManifest(context, PICK_IMAGE_GALLERY_SERVICE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Intent intent1 = new Intent();
                intent1.setType("image/*");
                intent1.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent1, "Select Picture"), PICK_IMAGE_GALLERY_SERVICE);
            }
        }
    }

    public void profilePhotoCamera() {
        if (ProjectUtils.hasPermissionInManifest(context, PICK_FROM_CAMERA, Manifest.permission.CAMERA)) {
            if (ProjectUtils.hasPermissionInManifest(context, PICK_IMAGE_GALLERY_SERVICE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                try {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File file = getOutputMediaFile(1);
                    if (!file.exists()) {
                        try {
                            ProjectUtils.pauseProgressDialog();
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        picUri = FileProvider.getUriForFile(context.getApplicationContext(), context.getApplicationContext().getPackageName() + ".fileprovider", file);
                    } else {
                        picUri = Uri.fromFile(file); // create
                    }

                    sharedPrefrence.setValue(Consts.IMAGE_URI_CAMERA, picUri.toString());
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, picUri); // set the image file
                    startActivityForResult(intent, PICK_FROM_CAMERA);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void imagePickerSheet() {
        builder = new BottomSheet.Builder(AddServiceActivity.this).sheet(R.menu.menu_cards);
        builder.title(getResources().getString(R.string.select_img));
        builder.listener((dialog, which) -> {
            switch (which) {
                case R.id.camera_cards:
                    profilePhotoCamera();
                    break;
                case R.id.gallery_cards:
                    profilePhotoGallery();
                    break;
                case R.id.cancel_cards:
                    builder.setOnDismissListener(dialog1 -> dialog1.dismiss());
                    break;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == PICK_IMAGE_GALLERY_SERVICE) && resultCode == RESULT_OK) {
            try {
                Uri tempUri = data.getData();
                Log.e("front tempUri", "" + tempUri);
                if (tempUri != null) {
                    startCropping(tempUri, CROP_IMAGE_GALLERY_SERVICE);
                } else {
                    ProjectUtils.showToast(context, "Format not supported!!");
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == CROP_IMAGE_GALLERY_SERVICE && data != null) {
            picUri = Uri.parse(data.getExtras().getString("resultUri"));
            try {
                String pathOfImage = picUri.getPath();
                imageCompression = new ImageCompression(context);
                imageCompression.execute(pathOfImage);
                imageCompression.setOnTaskFinishedEvent(imagePath -> {
                    addServiceBinding.rlContainer.setVisibility(View.VISIBLE);
                    addServiceBinding.ivServiceImage.setVisibility(View.VISIBLE);
                    addServiceBinding.cardViewServiceImage.setVisibility(View.GONE);

                    Glide.with(context).load("file://" + imagePath)
                            .thumbnail(0.5f)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(addServiceBinding.ivServiceImage);
                    Log.e("image", imagePath);
                    try {
                        file_service = new File(imagePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (requestCode == PICK_FROM_CAMERA && resultCode == RESULT_OK) {
            if (picUri != null) {
                picUri = Uri.parse(sharedPrefrence.getValue(Consts.IMAGE_URI_CAMERA));
                startCropping(picUri, CROP_CAMERA_IMAGE);
            } else {
                picUri = Uri.parse(sharedPrefrence
                        .getValue(Consts.IMAGE_URI_CAMERA));
                startCropping(picUri, CROP_CAMERA_IMAGE);
            }
        }
        if (requestCode == CROP_CAMERA_IMAGE) {
            if (data != null) {
                picUri = Uri.parse(data.getExtras().getString("resultUri"));
                try {
                    //bitmap = MediaStore.Images.Media.getBitmap(SaveDetailsActivityNew.this.getContentResolver(), resultUri);
                    pathOfImage = picUri.getPath();
                    imageCompression = new ImageCompression(context);
                    imageCompression.execute(pathOfImage);
                    imageCompression.setOnTaskFinishedEvent(imagePath -> {
                        addServiceBinding.rlContainer.setVisibility(View.VISIBLE);
                        addServiceBinding.ivServiceImage.setVisibility(View.VISIBLE);
                        addServiceBinding.cardViewServiceImage.setVisibility(View.GONE);

                        Glide.with(context).load("file://" + imagePath)
                                .thumbnail(0.5f)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(addServiceBinding.ivServiceImage);
                        try {
                            file_service = new File(imagePath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startCropping(Uri uri, int requestCode) {
        Intent intent = new Intent(context, MainFragment.class);
        intent.putExtra("imageUri", uri.toString());
        intent.putExtra("requestCode", requestCode);
        startActivityForResult(intent, requestCode);
    }

    private File getOutputMediaFile(int type) {
        String root = Environment.getExternalStorageDirectory().toString();
        File mediaStorageDir = new File(root, Consts.APP_NAME);
        /**Create the storage directory if it does not exist*/
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        /**Create a media file name*/
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == 1) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    Consts.APP_NAME + timeStamp + ".png");

            imageName = Consts.APP_NAME + timeStamp + ".png";
        } else {
            return null;
        }
        return mediaFile;
    }
}
