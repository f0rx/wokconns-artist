package com.wokconns.wokconns.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cocosw.bottomsheet.BottomSheet;
import com.google.android.gms.location.places.Place;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.schibstedspain.leku.LocationPickerActivity;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.ActivityEditPersnoalInfoBinding;
import com.wokconns.wokconns.dto.ArtistDetailsDTO;
import com.wokconns.wokconns.dto.CategoryDTO;
import com.wokconns.wokconns.dto.CurrencyDTO;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.interfacess.LocationActivityManager;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.preferences.SharedPrefrence;
import com.wokconns.wokconns.utils.ImageCompression;
import com.wokconns.wokconns.utils.MainFragment;
import com.wokconns.wokconns.utils.ProjectUtils;
import com.wokconns.wokconns.utils.SpinnerDialog;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.schibstedspain.leku.LocationPickerActivityKt.LATITUDE;
import static com.schibstedspain.leku.LocationPickerActivityKt.LONGITUDE;

public class EditPersonalInfo extends LocationActivityManager implements View.OnClickListener {
    private final String TAG = EditPersonalInfo.class.getSimpleName();
    private ActivityEditPersnoalInfoBinding binding;
    private Context mContext;

    private ArrayList<CategoryDTO> categoryDTOS = new ArrayList<>();
    private ArrayList<CurrencyDTO> currencyDTOArrayList = new ArrayList<>();
    private SpinnerDialog spinnerDialogCate;
    private ArtistDetailsDTO artistDetailsDTO;
    private Place place;
    private double lats = 0;
    private double longs = 0;
    private final HashMap<String, String> paramsUpdate = new HashMap<>();
    private UserDTO userDTO;
    private SharedPrefrence prefrence;
//    private ImageView ivBanner;

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
    File fileProfile;
    Bitmap bitmap = null;
    private final HashMap<String, File> paramsFile = new HashMap<>();
    private final HashMap<String, File> paramsFileProfile = new HashMap<>();
    private HashMap<String, String> params;
    String currencyId = "";
    String type = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_persnoal_info);
        mContext = EditPersonalInfo.this;
        prefrence = SharedPrefrence.getInstance(mContext);
        userDTO = prefrence.getParentUser(Consts.USER_DTO);

        if (getIntent().hasExtra(Consts.CATEGORY_list)) {
            categoryDTOS = (ArrayList<CategoryDTO>) getIntent().getSerializableExtra(Consts.CATEGORY_list);
            artistDetailsDTO = (ArtistDetailsDTO) getIntent().getSerializableExtra(Consts.ARTIST_DTO);
        }
        setUiAction();
    }

    @SuppressLint("NonConstantResourceId")
    public void setUiAction() {

        binding.etCategoryD.setOnClickListener(this);
        binding.etLocationD.setOnClickListener(this);
        binding.btnSubmit.setOnClickListener(this);
        binding.llBack.setOnClickListener(this);
        binding.llBanner.setOnClickListener(this);
        binding.llProfilePhoto.setOnClickListener(this);

        binding.etBioD.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                binding.bioLength.setText(String.format("%s/40", s.length()));

            }
        });
        binding.etAboutD.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                binding.aboutLength.setText(String.format(Locale.getDefault(), "%d/200", s.length()));

            }
        });

        builder = new BottomSheet.Builder(EditPersonalInfo.this).sheet(R.menu.menu_cards);
        builder.title(getResources().getString(R.string.select_img));
        builder.listener((dialog, which) -> {
            switch (which) {
                case R.id.camera_cards:
                    if (ProjectUtils.hasPermissionInManifest(EditPersonalInfo.this, PICK_FROM_CAMERA, Manifest.permission.CAMERA)) {
                        if (ProjectUtils.hasPermissionInManifest(EditPersonalInfo.this, PICK_FROM_GALLERY, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
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
                                    picUri = FileProvider.getUriForFile(mContext.getApplicationContext(), mContext.getApplicationContext().getPackageName() + ".fileprovider", file);
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
                    if (ProjectUtils.hasPermissionInManifest(EditPersonalInfo.this, PICK_FROM_CAMERA, Manifest.permission.CAMERA)) {
                        if (ProjectUtils.hasPermissionInManifest(EditPersonalInfo.this, PICK_FROM_GALLERY, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

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
                    builder.setOnDismissListener(DialogInterface::dismiss);
                    break;
            }
        });

        spinnerDialogCate = new SpinnerDialog((Activity) mContext, categoryDTOS, getResources().getString(R.string.select_cate));// With 	Animation
        spinnerDialogCate.bindOnSpinerListener((item, id, position) -> {
            binding.etCategoryD.setText(item);
            paramsUpdate.put(Consts.CATEGORY_ID, id);
//            binding.tvText.setText(String.format("%s%s%s", getResources().getString(R.string.commis_msg),
//                    categoryDTOS.get(position).getCurrency_type(), categoryDTOS.get(position).getPrice()));
        });

        if (artistDetailsDTO != null) {
            showData();
        }

        binding.etRateD.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 1 && s.toString().startsWith("0")) {
                    s.clear();
                }
            }
        });

        binding.etCurrency.setOnClickListener(v -> binding.etCurrency.showDropDown());

        binding.etCurrency.setOnItemClickListener((parent, view, position, id) -> {
            binding.etCurrency.showDropDown();
            CurrencyDTO currencyDTO = (CurrencyDTO) parent.getItemAtPosition(position);
            Log.e(TAG, "onItemClick: " + currencyDTO.getCurrency_symbol());

            currencyId = currencyDTO.getId();
            paramsUpdate.put(Consts.ID, currencyId);
        });

        try {
            setCurrencyValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getOutputMediaFile(int type) {
        String root = getExternalCacheDir().getPath();
        File mediaStorageDir = new File(root, Consts.APP_NAME);
        /**Create the storage directory if it does not exist*/
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        /**Create a media file name*/
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
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

    public void showData() {
        for (int j = 0; j < categoryDTOS.size(); j++) {
            if (categoryDTOS.get(j).getId().equalsIgnoreCase(artistDetailsDTO.getCategory_id())) {
                categoryDTOS.get(j).setSelected(true);
                binding.etCategoryD.setText(categoryDTOS.get(j).getCat_name());
//                binding.tvText.setText(String.format("%s%s%s", getResources().getString(R.string.commis_msg),
//                        categoryDTOS.get(j).getCurrency_type(), categoryDTOS.get(j).getPrice()));
            }
        }

        spinnerDialogCate = new SpinnerDialog((Activity) mContext, categoryDTOS, getResources().getString(R.string.select_cate));// With 	Animation
        spinnerDialogCate.bindOnSpinerListener((item, id, position) -> {
            binding.etCategoryD.setText(item);
            paramsUpdate.put(Consts.CATEGORY_ID, id);
//            binding.tvText.setText(String.format("%s%s%s", getResources().getString(R.string.commis_msg),
//                    categoryDTOS.get(position).getCurrency_type(), categoryDTOS.get(position).getPrice()));
        });
        binding.etCategoryD.setText(artistDetailsDTO.getCategory_name());
        binding.etNameD.setText(artistDetailsDTO.getName());
        binding.etBioD.setText(artistDetailsDTO.getBio());
        binding.etAboutD.setText(artistDetailsDTO.getAbout_us());
        binding.etCityD.setText(artistDetailsDTO.getCity());
        binding.etCountry.setText(artistDetailsDTO.getCountry());
        binding.etLocationD.setText(artistDetailsDTO.getLocation());
        binding.etRateD.setText(artistDetailsDTO.getPrice());


        Glide.with(mContext).
                load(artistDetailsDTO.getBanner_image())
                .placeholder(R.drawable.banner_img)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivBanner);

        Glide.with(mContext).
                load(artistDetailsDTO.getImage())
                .placeholder(R.drawable.banner_img)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.civProfile);

        binding.etCurrency.setText(String.format("(%s)%s", artistDetailsDTO.getCurrency_symbol(), artistDetailsDTO.getCurrency_name()));

        currencyId = artistDetailsDTO.getCurrency_id();
        paramsUpdate.put(Consts.ID, currencyId);

    }

    public void setCurrencyValue() {

        new HttpsRequest(Consts.GET_CURRENCY_API, mContext).stringGet(TAG, (flag, msg, response) -> {
            if (flag) {
//                    ProjectUtils.showToast(mContext, msg);
                try {
                    currencyDTOArrayList = new ArrayList<>();
                    Type getCurrencyDTO = new TypeToken<List<CurrencyDTO>>() {
                    }.getType();
                    currencyDTOArrayList = new Gson().fromJson(response.getJSONArray("data").toString(), getCurrencyDTO);

                    try {
                        ArrayAdapter<CurrencyDTO> currencyAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, currencyDTOArrayList);
                        binding.etCurrency.setAdapter(currencyAdapter);
                        binding.etCurrency.postDelayed(() -> {
                            // Initalize value with a default
                            CurrencyDTO naira = null;
                            // Loop thru then find Naira (NGN)
                            for (CurrencyDTO el : currencyDTOArrayList)
                                if (Objects.equals(el.getCode(), "NGN")) naira = el;
                            if (naira != null)
                                binding.etCurrency.setText(naira.toString(), false);
                        }, 200);

                        binding.etCurrency.setCursorVisible(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                ProjectUtils.showToast(mContext, msg);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.etCategoryD:
                if (NetworkManager.isConnectToInternet(mContext)) {
                    if (categoryDTOS.size() > 0)
                        spinnerDialogCate.showSpinerDialog();
                } else {
                    ProjectUtils.showToast(mContext, getResources().getString(R.string.internet_concation));
                }

                break;
            case R.id.etLocationD:
                requestLocationPermissions(isGranted -> {
                    if (isGranted) showGPSRationale();
                });

                if (NetworkManager.isConnectToInternet(mContext)) {
                    findPlace();
                } else {
                    ProjectUtils.showToast(mContext, getResources().getString(R.string.internet_concation));
                }
                break;
            case R.id.btnSubmit:
                if (NetworkManager.isConnectToInternet(mContext)) {
                    submitPersonalProfile();
                } else {
                    ProjectUtils.showToast(mContext, getResources().getString(R.string.internet_concation));
                }
                break;
            case R.id.ll_banner:
                type = "banner";
                builder.show();
                break;
            case R.id.llBack:
                finish();
                overridePendingTransition(R.anim.stay, R.anim.slide_down);
                break;
            case R.id.ll_profile_photo:
                type = "profile";
                builder.show();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.stay, R.anim.slide_down);
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
                    imageCompression = new ImageCompression(EditPersonalInfo.this);
                    imageCompression.execute(pathOfImage);
                    imageCompression.setOnTaskFinishedEvent(imagePath -> {
                        try {
                            // bitmap = MediaStore.Images.Media.getBitmap(SaveDetailsActivityNew.this.getContentResolver(), resultUri);

                            if (type.equalsIgnoreCase("profile")) {
                                fileProfile = new File(imagePath);
                                Glide.with(mContext).load("file://" + imagePath)
                                        .thumbnail(0.5f)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(binding.civProfile);

                                Log.e("image", imagePath);

                                params = new HashMap<>();
                                params.put(Consts.USER_ID, userDTO.getUser_id());

                                paramsFileProfile.put(Consts.IMAGE, fileProfile);

                                if (NetworkManager.isConnectToInternet(mContext)) {
                                    updateProfileSelf();
                                } else {
                                    ProjectUtils.showToast(mContext, getResources().getString(R.string.internet_concation));
                                }

                            } else if (type.equalsIgnoreCase("banner")) {
                                file = new File(imagePath);
                                Glide.with(mContext).load("file://" + imagePath)
                                        .thumbnail(0.5f)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(binding.ivBanner);

                                Log.e("image", imagePath);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
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
                    bm = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), picUri);
                    pathOfImage = picUri.getPath();
                    imageCompression = new ImageCompression(EditPersonalInfo.this);
                    imageCompression.execute(pathOfImage);
                    imageCompression.setOnTaskFinishedEvent(imagePath -> {
                        Log.e("image", imagePath);
                        try {
                            if (type.equalsIgnoreCase("profile")) {
                                fileProfile = new File(imagePath);
                                Glide.with(mContext).load("file://" + imagePath)
                                        .thumbnail(0.5f)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(binding.civProfile);

                                Log.e("image", imagePath);

                                params = new HashMap<>();
                                params.put(Consts.USER_ID, userDTO.getUser_id());

                                paramsFileProfile.put(Consts.IMAGE, fileProfile);

                                if (NetworkManager.isConnectToInternet(mContext)) {
                                    updateProfileSelf();
                                } else {
                                    ProjectUtils.showToast(mContext, getResources().getString(R.string.internet_concation));
                                }
                            } else if (type.equalsIgnoreCase("banner")) {
                                file = new File(imagePath);
                                Glide.with(mContext).load("file://" + imagePath)
                                        .thumbnail(0.5f)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(binding.ivBanner);

                                Log.e("image", imagePath);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
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

        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                try {
                    getAddress(data.getDoubleExtra(LATITUDE, 0.0), data.getDoubleExtra(LONGITUDE, 0.0));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    }

    public void startCropping(Uri uri, int requestCode) {
        Intent intent = new Intent(mContext, MainFragment.class);
        intent.putExtra("imageUri", uri.toString());
        intent.putExtra("requestCode", requestCode);
        startActivityForResult(intent, requestCode);
    }

    private void findPlace() {
        Intent locationPickerIntent = new LocationPickerActivity.Builder()
                .withGooglePlacesEnabled()
                //.withLocation(41.4036299, 2.1743558)
                .build(mContext);

        startActivityForResult(locationPickerIntent, 101);
    }

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(EditPersonalInfo.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();
            Log.e("IGA", "Address" + add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);

            binding.etLocationD.setText(obj.getAddressLine(0));

            lats = lat;
            longs = lng;


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void submitPersonalProfile() {
        if (!validation(binding.etCategoryD, getResources().getString(R.string.val_cat_sele))) {
            return;
        } else if (!validation(binding.etNameD, getResources().getString(R.string.val_name))) {
            return;
        } else if (!validation(binding.etBioD, getResources().getString(R.string.val_bio))) {
            return;
        } else if (!validation(binding.etAboutD, getResources().getString(R.string.val_about))) {
            return;
        } else if (!validation(binding.etCityD, getResources().getString(R.string.val_city))) {
            return;
        } else if (!validation(binding.etCountry, getResources().getString(R.string.val_country))) {
            return;
        } else if (!validation(binding.etLocationD, getResources().getString(R.string.val_location))) {
            return;
        } else if (!validation(binding.etRateD, getResources().getString(R.string.val_rate))) {
            return;
        }
//        else if (paramsFile == null || paramsFile.size() == 0) {
//            ProjectUtils.showToast(mContext, getResources().getString(R.string.select_image));
//        }
        else {
            if (NetworkManager.isConnectToInternet(mContext)) {
                paramsUpdate.put(Consts.USER_ID, userDTO.getUser_id());
                paramsUpdate.put(Consts.NAME, ProjectUtils.getEditTextValue(binding.etNameD));
                paramsUpdate.put(Consts.BIO, ProjectUtils.getEditTextValue(binding.etBioD));
                paramsUpdate.put(Consts.ABOUT_US, ProjectUtils.getEditTextValue(binding.etAboutD));
                paramsUpdate.put(Consts.CITY, ProjectUtils.getEditTextValue(binding.etCityD));
                paramsUpdate.put(Consts.COUNTRY, ProjectUtils.getEditTextValue(binding.etCountry));
                paramsUpdate.put(Consts.LOCATION, ProjectUtils.getEditTextValue(binding.etLocationD));
                paramsUpdate.put(Consts.PRICE, ProjectUtils.getEditTextValue(binding.etRateD));
                paramsFile.put(Consts.BANNER_IMAGE, file);
                if (lats != 0)
                    paramsUpdate.put(Consts.LATITUDE, String.valueOf(lats));

                if (longs != 0)
                    paramsUpdate.put(Consts.LONGITUDE, String.valueOf(longs));

                updateProfile();
            } else {
                ProjectUtils.showToast(mContext, getResources().getString(R.string.internet_concation));
            }
        }
    }

    public boolean validation(EditText editText, String msg) {
        if (!ProjectUtils.isEditTextFilled(editText)) {
            ProjectUtils.showLong(mContext, msg);
            return false;
        } else {
            return true;
        }
    }

    public void updateProfile() {
        ProjectUtils.showProgressDialog(mContext, true, getResources().getString(R.string.please_wait));

        new HttpsRequest(Consts.UPDATE_PROFILE_ARTIST_API, paramsUpdate, paramsFile, mContext).imagePost(TAG, (flag, msg, response) -> {
            ProjectUtils.pauseProgressDialog();
            if (flag) {
                try {
                    ProjectUtils.showToast(mContext, msg);
                    artistDetailsDTO = new Gson().fromJson(response.getJSONObject("data").toString(), ArtistDetailsDTO.class);
                    userDTO.setIs_profile(1);
                    prefrence.setParentUser(userDTO, Consts.USER_DTO);
                    finish();
                    overridePendingTransition(R.anim.stay, R.anim.slide_down);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ProjectUtils.showToast(mContext, msg);
            }
        });
    }

    public void updateProfileSelf() {
        new HttpsRequest(Consts.ARTIST_IMAGE_API, params, paramsFileProfile, mContext).imagePost(TAG, (flag, msg, response) -> {
            if (flag) {
                try {
                    ProjectUtils.showToast(mContext, msg);
                    int temp = 0;
                    if (userDTO.getIs_profile() == 1) {
                        temp = 1;
                    }
                    userDTO = new Gson().fromJson(response.getJSONObject("data").toString(), UserDTO.class);
                    userDTO.setIs_profile(temp);
                    prefrence.setParentUser(userDTO, Consts.USER_DTO);

                    Glide.with(mContext).
                            load(userDTO.getImage())
                            .placeholder(R.drawable.dummyuser_image)
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(binding.civProfile);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                ProjectUtils.showToast(mContext, msg);
            }
        });
    }

}
