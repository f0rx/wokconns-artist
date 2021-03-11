package com.wokconns.wokconns.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.bumptech.glide.Glide;
import com.cocosw.bottomsheet.BottomSheet;
import com.google.gson.Gson;
import com.wokconns.wokconns.dto.ArtistDetailsDTO;
import com.wokconns.wokconns.dto.GalleryDTO;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.ActivityImageGalleryBinding;
import com.wokconns.wokconns.databinding.DailogArGallryBinding;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.interfacess.Helper;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.preferences.SharedPrefrence;
import com.wokconns.wokconns.ui.adapter.AdapterGallery;
import com.wokconns.wokconns.utils.ImageCompression;
import com.wokconns.wokconns.utils.MainFragment;
import com.wokconns.wokconns.utils.ProjectUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ImageGallery extends AppCompatActivity implements View.OnClickListener {
    private String TAG = ImageGallery.class.getSimpleName();
    ActivityImageGalleryBinding binding;
    Context context;
    private View view;
    private ArtistDetailsDTO artistDetailsDTO;
    private ArrayList<GalleryDTO> galleryList;
    private AdapterGallery adapterGallery;
    private Bundle bundle;
    private GridLayoutManager gridLayoutManager;
    private HashMap<String, String> paramsUpdate;
    private Dialog dialogEditGallery;
    private HashMap<String, File> paramsFile;
    private UserDTO userDTO;
    private SharedPrefrence prefrence;
    BottomSheet.Builder builder;
    Uri picUri;
    int PICK_FROM_CAMERA = 1, PICK_FROM_GALLERY = 2;
    int CROP_CAMERA_IMAGE = 3, CROP_GALLERY_IMAGE = 4;
    String imageName;
    String pathOfImage;
    Bitmap bm;
    ImageCompression imageCompression;
    byte[] resultByteArray;
    File file;
    Bitmap bitmap = null;
    DailogArGallryBinding binding1;
    private HashMap<String, String> params = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_gallery);
        context = ImageGallery.this;

        prefrence = SharedPrefrence.getInstance(context);
        userDTO = prefrence.getParentUser(Consts.USER_DTO);

        params.put(Consts.ARTIST_ID, userDTO.getUser_id());
        params.put(Consts.USER_ID, userDTO.getUser_id());

        bundle = getIntent().getExtras();
        if (bundle != null) {
            artistDetailsDTO = (ArtistDetailsDTO) bundle.getSerializable(Consts.ARTIST_DTO);
        }
        showUiAction();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            getArtist();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showUiAction() {
        binding.ivClose.setOnClickListener(this);
        binding.llGalleryAdd.setOnClickListener(this);
        binding.llBack.setOnClickListener(this);

        builder = new BottomSheet.Builder((Activity) context).sheet(R.menu.menu_cards);
        builder.title(getResources().getString(R.string.select_img));
        builder.listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case R.id.camera_cards:
                        if (ProjectUtils.hasPermissionInManifest(context, PICK_FROM_CAMERA, Manifest.permission.CAMERA)) {
                            if (ProjectUtils.hasPermissionInManifest(context, PICK_FROM_GALLERY, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
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
                                        //Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.asd", newFile);
                                        picUri = FileProvider.getUriForFile(context.getApplicationContext(), context.getApplicationContext().getPackageName() + ".fileprovider", file);
                                    } else {
                                        picUri = Uri.fromFile(file); // create
                                    }

                                    prefrence.setValue(Consts.IMAGE_URI_CAMERA, picUri.toString());
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, picUri); // set the image file
                                    startActivityForResult(intent, PICK_FROM_CAMERA);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        break;
                    case R.id.gallery_cards:
                        if (ProjectUtils.hasPermissionInManifest(context, PICK_FROM_CAMERA, Manifest.permission.CAMERA)) {
                            if (ProjectUtils.hasPermissionInManifest(context, PICK_FROM_GALLERY, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                                File file = getOutputMediaFile(1);
                                if (!file.exists()) {
                                    try {
                                        file.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                picUri = Uri.fromFile(file);

                                Intent intent = new Intent();
                                intent.setType("image/*");
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_pic)), PICK_FROM_GALLERY);

                            }
                        }
                        break;
                    case R.id.cancel_cards:
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                dialog.dismiss();
                            }
                        });
                        break;
                }
            }
        });

//        showData();
    }


    public void showData() {
        gridLayoutManager = new GridLayoutManager(context, 2);
        galleryList = new ArrayList<>();
        galleryList = artistDetailsDTO.getGallery();

        adapterGallery = new AdapterGallery(ImageGallery.this, galleryList, "gallery");
        binding.rvGallery.setLayoutManager(gridLayoutManager);
        binding.rvGallery.setAdapter(adapterGallery);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivClose:
                binding.rlZoomImg.setVisibility(View.GONE);
                break;
            case R.id.ll_gallery_add:
                builder.show();
                break;
            case R.id.llBack:
                finish();
                break;

        }
    }

    public void showImg(String imgURL) {
        binding.rlZoomImg.setVisibility(View.VISIBLE);
        Glide
                .with(context)
                .load(imgURL)
                .placeholder(R.drawable.dummyuser_image)
                .into(binding.ivZoom);
    }

    public void getParentData() {
        getArtist();
    }

    public void addGalleryClick() {
        dialogGallery();
    }

    public void dialogGallery() {
        paramsUpdate = new HashMap<>();
        paramsFile = new HashMap<>();
        dialogEditGallery = new Dialog(context/*, android.R.style.Theme_Dialog*/);
        dialogEditGallery.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogEditGallery.requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding1 = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dailog_ar_gallry, null, false);
        dialogEditGallery.setContentView(binding1.getRoot());
        dialogEditGallery.show();
        dialogEditGallery.setCancelable(false);

        binding1.etImageGallD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.show();
            }
        });
        binding1.tvNoGall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogEditGallery.dismiss();

            }
        });
        binding1.tvYesGall.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        paramsUpdate.put(Consts.USER_ID, userDTO.getUser_id());
                        paramsFile.put(Consts.IMAGE, file);

                        if (NetworkManager.isConnectToInternet(context)) {
                            if (binding1.etImageGallD.getText().toString().length() > 0) {
                                addGallery();

                            } else {
                                ProjectUtils.showLong(context, getResources().getString(R.string.val_iamg_ad));
                            }
                        } else {
                            ProjectUtils.showToast(context, getResources().getString(R.string.internet_concation));
                        }
                    }
                });

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CROP_CAMERA_IMAGE) {
            if (data != null) {
                picUri = Uri.parse(data.getExtras().getString("resultUri"));
                try {
                    //bitmap = MediaStore.Images.Media.getBitmap(SaveDetailsActivityNew.this.getContentResolver(), resultUri);
                    pathOfImage = picUri.getPath();
                    imageCompression = new ImageCompression(context);
                    imageCompression.execute(pathOfImage);
                    imageCompression.setOnTaskFinishedEvent(new ImageCompression.AsyncResponse() {
                        @Override
                        public void processFinish(String imagePath) {
                            try {
                                // bitmap = MediaStore.Images.Media.getBitmap(SaveDetailsActivityNew.this.getContentResolver(), resultUri);
                                file = new File(imagePath);
//                                binding1.etImageGallD.setText(imagePath);
                                Log.e("image", imagePath);

                                paramsUpdate = new HashMap<>();
                                paramsFile = new HashMap<>();

                                addGalleryImage();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (requestCode == CROP_GALLERY_IMAGE) {
            if (data != null) {
                picUri = Uri.parse(data.getExtras().getString("resultUri"));
                try {
                    bm = MediaStore.Images.Media.getBitmap(context.getContentResolver(), picUri);
                    pathOfImage = picUri.getPath();
                    imageCompression = new ImageCompression(context);
                    imageCompression.execute(pathOfImage);
                    imageCompression.setOnTaskFinishedEvent(new ImageCompression.AsyncResponse() {
                        @Override
                        public void processFinish(String imagePath) {
                            Log.e("image", imagePath);
                            try {
                                file = new File(imagePath);
//                                binding1.etImageGallD.setText(imagePath);
                                Log.e("image", imagePath);

                                paramsUpdate = new HashMap<>();
                                paramsFile = new HashMap<>();

                                addGalleryImage();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (requestCode == PICK_FROM_CAMERA && resultCode == RESULT_OK) {
            if (picUri != null) {
                picUri = Uri.parse(prefrence.getValue(Consts.IMAGE_URI_CAMERA));
                startCropping(picUri, CROP_CAMERA_IMAGE);
            } else {
                picUri = Uri.parse(prefrence
                        .getValue(Consts.IMAGE_URI_CAMERA));
                startCropping(picUri, CROP_CAMERA_IMAGE);
            }
        }
        if (requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK) {
            try {
                Uri tempUri = data.getData();
                Log.e("front tempUri", "" + tempUri);
                if (tempUri != null) {
                    startCropping(tempUri, CROP_GALLERY_IMAGE);
                } else {

                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public void startCropping(Uri uri, int requestCode) {
        Intent intent = new Intent(context, MainFragment.class);
        intent.putExtra("imageUri", uri.toString());
        intent.putExtra("requestCode", requestCode);
        startActivityForResult(intent, requestCode);
    }

    public void addGalleryImage() {
        paramsUpdate.put(Consts.USER_ID, userDTO.getUser_id());
        paramsFile.put(Consts.IMAGE, file);

        if (NetworkManager.isConnectToInternet(context)) {
            addGallery();
        } else {
            ProjectUtils.showToast(context, getResources().getString(R.string.internet_concation));
        }
    }

    public void addGallery() {
        ProjectUtils.showProgressDialog(context, true, getResources().getString(R.string.please_wait));
        new HttpsRequest(Consts.ADD_GALLERY_API, paramsUpdate, paramsFile, context).imagePost(TAG, new Helper() {
            @Override
            public void backResponse(boolean flag, String msg, JSONObject response) {
                ProjectUtils.pauseProgressDialog();
                if (flag) {
                    ProjectUtils.showToast(context, msg);
                    getArtist();
                } else {
                    ProjectUtils.showToast(context, msg);
                }
            }
        });
    }

    public void getArtist() {
        new HttpsRequest(Consts.GET_ARTIST_BY_ID_API, params, context).stringPost(TAG, new Helper() {
            @Override
            public void backResponse(boolean flag, String msg, JSONObject response) {
                if (flag) {
                    try {

                        artistDetailsDTO = new Gson().fromJson(response.getJSONObject("data").toString(), ArtistDetailsDTO.class);
                        showData();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                }
            }
        });
    }
}
