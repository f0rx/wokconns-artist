package com.wokconns.wokconns.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cocosw.bottomsheet.BottomSheet;
import com.google.android.material.appbar.AppBarLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wokconns.wokconns.dto.ArtistDetailsDTO;
import com.wokconns.wokconns.dto.CategoryDTO;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.DialogDetailsInfoBinding;
import com.wokconns.wokconns.databinding.FragmentArtistProfileNewBinding;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.interfacess.Helper;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.preferences.SharedPrefrence;
import com.wokconns.wokconns.ui.activity.BaseActivity;
import com.wokconns.wokconns.ui.activity.EditPersnoalInfo;
import com.wokconns.wokconns.ui.activity.ImageGallery;
import com.wokconns.wokconns.ui.activity.PreviousWork;
import com.wokconns.wokconns.ui.activity.Reviews;
import com.wokconns.wokconns.ui.activity.Services;
import com.wokconns.wokconns.utils.ImageCompression;
import com.wokconns.wokconns.utils.MainFragment;
import com.wokconns.wokconns.utils.ProjectUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ArtistProfileNew extends Fragment implements View.OnClickListener, AppBarLayout.OnOffsetChangedListener {
    private String TAG = ArtistProfileNew.class.getSimpleName();
    private FragmentArtistProfileNewBinding binding;
    private SharedPrefrence prefrence;
    private UserDTO userDTO;
    private ArtistDetailsDTO artistDetailsDTO;
    private HashMap<String, String> parms = new HashMap<>();
    private HashMap<String, String> parmsCategory = new HashMap<>();
    private Bundle bundle = new Bundle();
    private int mMaxScrollSize;
    private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 20;
    private boolean mIsAvatarShown = true;
    private ArrayList<CategoryDTO> categoryDTOS = new ArrayList<>();
    private HashMap<String, String> paramsUpdate = new HashMap<>();
    private HashMap<String, File> paramsFile;

    BottomSheet.Builder builder;
    Uri picUri;
    int PICK_FROM_CAMERA = 1, PICK_FROM_GALLERY = 2;
    int CROP_CAMERA_IMAGE = 3, CROP_GALLERY_IMAGE = 4;
    String imageName;
    String pathOfImage;
    Bitmap bm;
    ImageCompression imageCompression;
    File file;
    private HashMap<String, String> params;
    private BaseActivity baseActivity;
    private HashMap<String, String> paramsDeleteImg = new HashMap<>();
    DialogDetailsInfoBinding dialogBinding;
    private Dialog dialog;
    String genderString = "female";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_artist_profile_new, container, false);

        prefrence = SharedPrefrence.getInstance(getActivity());
        userDTO = prefrence.getParentUser(Consts.USER_DTO);
        baseActivity.headerNameTV.setText(getResources().getString(R.string.my_profile));
        parmsCategory.put(Consts.USER_ID, userDTO.getUser_id());

        parms.put(Consts.ARTIST_ID, userDTO.getUser_id());
        parms.put(Consts.USER_ID, userDTO.getUser_id());
        setUiAction();
        return binding.getRoot();
    }

    public void setUiAction() {
        binding.back.setOnClickListener(this);
        binding.llGallery.setOnClickListener(this);
        binding.llServices.setOnClickListener(this);
        binding.llWorks.setOnClickListener(this);
        binding.llReview.setOnClickListener(this);
        binding.tvEdit.setOnClickListener(this);
        binding.ivEdit.setOnClickListener(this);
        binding.llProfilePhoto.setOnClickListener(this);

        binding.swOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (artistDetailsDTO != null) {
                    if (NetworkManager.isConnectToInternet(getActivity())) {
                        paramsUpdate = new HashMap<>();
                        paramsUpdate.put(Consts.USER_ID, userDTO.getUser_id());
                        if (artistDetailsDTO.getIs_online().equalsIgnoreCase("1")) {
                            paramsUpdate.put(Consts.IS_ONLINE, "0");
                            isOnline();
                        } else {
                            paramsUpdate.put(Consts.IS_ONLINE, "1");
                            isOnline();
                        }
                    } else {
                        ProjectUtils.showToast(getActivity(), getResources().getString(R.string.internet_concation));
                    }
                } else {
                    ProjectUtils.showToast(getActivity(), getResources().getString(R.string.incomplete_profile_msg));
                }

            }

        });

        builder = new BottomSheet.Builder(getActivity()).sheet(R.menu.menu_cards);
        builder.title(getResources().getString(R.string.select_img));
        builder.listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case R.id.camera_cards:
                        if (ProjectUtils.hasPermissionInManifest(getActivity(), PICK_FROM_CAMERA, Manifest.permission.CAMERA)) {
                            if (ProjectUtils.hasPermissionInManifest(getActivity(), PICK_FROM_GALLERY, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
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
                                        picUri = FileProvider.getUriForFile(getActivity().getApplicationContext(), getActivity().getApplicationContext().getPackageName() + ".fileprovider", file);
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
                        if (ProjectUtils.hasPermissionInManifest(getActivity(), PICK_FROM_CAMERA, Manifest.permission.CAMERA)) {
                            if (ProjectUtils.hasPermissionInManifest(getActivity(), PICK_FROM_GALLERY, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

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

    }

    @Override
    public void onResume() {
        super.onResume();
        if (NetworkManager.isConnectToInternet(getActivity())) {
            getCategory();
            getArtist();

        } else {
            ProjectUtils.showToast(getActivity(), getResources().getString(R.string.internet_concation));
        }
    }

    public void getCategory() {
        new HttpsRequest(Consts.GET_ALL_CATEGORY_API, parmsCategory, getActivity()).stringPost(TAG, new Helper() {
            @Override
            public void backResponse(boolean flag, String msg, JSONObject response) {
                if (flag) {
                    try {
                        categoryDTOS = new ArrayList<>();
                        Type getpetDTO = new TypeToken<List<CategoryDTO>>() {
                        }.getType();
                        categoryDTOS = (ArrayList<CategoryDTO>) new Gson().fromJson(response.getJSONArray("data").toString(), getpetDTO);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                }
            }
        });
    }

    public void getArtist() {
        new HttpsRequest(Consts.GET_ARTIST_BY_ID_API, parms, getActivity()).stringPost(TAG, new Helper() {
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

    public void showData() {
        bundle = new Bundle();
        bundle.putSerializable(Consts.ARTIST_DTO, artistDetailsDTO);

        binding.tvName.setText(artistDetailsDTO.getName());
        Glide.with(getActivity()).
                load(artistDetailsDTO.getImage())
                .placeholder(R.drawable.dummyuser_image)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivArtist);
        Glide.with(getActivity()).
                load(artistDetailsDTO.getBanner_image())
                .placeholder(R.drawable.banner_img)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivBanner);
        binding.tvCategory.setText(artistDetailsDTO.getCategory_name());
        binding.tvJobCompleted.setText(artistDetailsDTO.getJobDone() + " " + getResources().getString(R.string.jobs_comleted));
//        binding.tvJobPercentCompleted.setText(artistDetailsDTO.getCompletePercentages() + "% " + getResources().getString(R.string.jobs_comleted));
        binding.tvAbout.setText(artistDetailsDTO.getAbout_us());

        if (!userDTO.getEmail_id().equalsIgnoreCase("")) {
            binding.tvEmail.setText(userDTO.getEmail_id());
        } else {
            binding.tvEmail.setText(getResources().getString(R.string.NA));
        }
        if (!userDTO.getMobile().equalsIgnoreCase("")) {
            binding.tvPhone.setText(userDTO.getMobile());
        } else {
            binding.tvPhone.setText(getResources().getString(R.string.NA));
        }
        if (!userDTO.getGender().equalsIgnoreCase("")) {
            binding.tvGender.setText(userDTO.getGender());
        } else {
            binding.tvGender.setText(getResources().getString(R.string.NA));
        }

        binding.simpleRatingBarOver.setRating(Float.parseFloat(artistDetailsDTO.getAva_rating()));
        binding.tvHourlyRateValue.setText(artistDetailsDTO.getCurrency_symbol() + " " + artistDetailsDTO.getPrice());

        if (artistDetailsDTO.getIs_online().equalsIgnoreCase("1")) {
            binding.tvOnlineOffline.setText(getResources().getString(R.string.online));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.tvOnlineOffline.setCompoundDrawableTintList(AppCompatResources.getColorStateList(baseActivity, R.color.green));
            }
            binding.swOnOff.setChecked(true);

        } else {
            binding.tvOnlineOffline.setText(getResources().getString(R.string.offline));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.tvOnlineOffline.setCompoundDrawableTintList(AppCompatResources.getColorStateList(baseActivity, R.color.red));
            }
            binding.swOnOff.setChecked(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivEditPersonal:
                if (NetworkManager.isConnectToInternet(getActivity())) {
                    if (categoryDTOS.size() > 0) {
                        Intent intent = new Intent(getActivity(), EditPersnoalInfo.class);
                        intent.putExtra(Consts.ARTIST_DTO, artistDetailsDTO);
                        intent.putExtra(Consts.CATEGORY_list, categoryDTOS);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_up, R.anim.stay);
                    } else {
                        ProjectUtils.showLong(getActivity(), getResources().getString(R.string.try_after));
                    }
                } else {
                    ProjectUtils.showToast(getActivity(), getResources().getString(R.string.internet_concation));
                }


                break;
            case R.id.btnDelete:
                if (artistDetailsDTO != null) {

                    if (NetworkManager.isConnectToInternet(getActivity())) {
                        if (!artistDetailsDTO.getImage().equalsIgnoreCase("")) {
                            deleteImage();
                        } else {
                            ProjectUtils.showToast(getActivity(), getResources().getString(R.string.upload_image_first));
                        }
                    } else {
                        ProjectUtils.showToast(getActivity(), getResources().getString(R.string.internet_concation));
                    }
                } else {
                    ProjectUtils.showToast(getActivity(), getResources().getString(R.string.incomplete_profile_msg));
                }

                break;
            case R.id.btnChange:
                if (artistDetailsDTO != null) {
                    builder.show();

                } else {
                    ProjectUtils.showToast(getActivity(), getResources().getString(R.string.incomplete_profile_msg));
                }
                break;
            case R.id.back:
                if (baseActivity.drawer.isDrawerVisible(GravityCompat.START)) {
                    baseActivity.drawer.closeDrawer(GravityCompat.START);
                } else {
                    baseActivity.drawer.openDrawer(GravityCompat.START);
                }
                break;
            case R.id.ll_gallery:
                Intent intent = new Intent(baseActivity, ImageGallery.class);
                intent.putExtras(bundle);
                baseActivity.startActivity(intent);
                break;
            case R.id.ll_services:
                Intent intent1 = new Intent(baseActivity, Services.class);
                intent1.putExtras(bundle);
                baseActivity.startActivity(intent1);
                break;
            case R.id.ll_works:
                Intent intent2 = new Intent(baseActivity, PreviousWork.class);
                intent2.putExtras(bundle);
                baseActivity.startActivity(intent2);
                break;
            case R.id.ll_review:
                Intent intent3 = new Intent(baseActivity, Reviews.class);
                intent3.putExtras(bundle);
                baseActivity.startActivity(intent3);
                break;
            case R.id.tv_edit:
                Intent intent4 = new Intent(getActivity(), EditPersnoalInfo.class);
                intent4.putExtra(Consts.ARTIST_DTO, artistDetailsDTO);
                intent4.putExtra(Consts.CATEGORY_list, categoryDTOS);
                startActivity(intent4);
                requireActivity().overridePendingTransition(R.anim.slide_up, R.anim.stay);
                break;
            case R.id.iv_edit:
                dialogForSubmit();
                break;
            case R.id.ll_profile_photo:

                break;
        }
    }

    @SuppressLint("NonConstantResourceId")
    public void dialogForSubmit() {
        dialog = new Dialog(baseActivity /*R.style.AlertDialogCustom*/);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialogBinding = DataBindingUtil.inflate(LayoutInflater.from(baseActivity), R.layout.dialog_details_info, null, false);
        dialog.setContentView(dialogBinding.getRoot());
        dialog.show();
        dialog.setCancelable(true);

        dialogBinding.etNameSelfD.setText(userDTO.getName());
        dialogBinding.etEmailD.setText(userDTO.getEmail_id());
        dialogBinding.etMobileD.setText(userDTO.getMobile());

        try {
            dialogBinding.ccp.setDefaultCountryUsingNameCode("NG");
            dialogBinding.ccp.resetToDefaultCountry();
        } catch (Exception e) {
            e.printStackTrace();
        }

        switch (userDTO.getGender()) {
            case "0":
                ((RadioButton) dialogBinding.rgGenderOptions.getChildAt(0)).setChecked(true);
                genderString = "female";
                break;
            case "1":
                ((RadioButton) dialogBinding.rgGenderOptions.getChildAt(1)).setChecked(true);
                genderString = "male";
                break;
            case "2":
                ((RadioButton) dialogBinding.rgGenderOptions.getChildAt(2)).setChecked(true);
                genderString = "other";
                break;
        }

        dialogBinding.tvSubmit.setOnClickListener(v -> {
            if (NetworkManager.isConnectToInternet(baseActivity)) {

                if (!ProjectUtils.isEditTextFilled(dialogBinding.etNameSelfD)) {
                    ProjectUtils.showToast(baseActivity, getResources().getString(R.string.val_name));
                    return;
                }
                if (!ProjectUtils.isEditTextFilled(dialogBinding.etEmailD)) {
                    ProjectUtils.showToast(baseActivity, getResources().getString(R.string.val_email));
                    return;
                }
                if (!ProjectUtils.isEditTextFilled(dialogBinding.etMobileD)) {
                    ProjectUtils.showToast(baseActivity, getResources().getString(R.string.val_phone));
                    return;
                } else {
                    updateProfile();
                }
            } else {

            }
        });

        dialogBinding.ivClose.setOnClickListener(v -> dialog.dismiss());


        dialogBinding.rgGenderOptions.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_gender_female:
                    genderString = "female";
                    break;
                case R.id.rb_gender_male:
                    genderString = "male";
                    break;
                case R.id.rb_other:
                    genderString = "other";
                    break;
            }
        });
    }


    static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();

        int percentage = (Math.abs(i)) * 100 / mMaxScrollSize;

        if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && mIsAvatarShown) {
            mIsAvatarShown = false;

        }

        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !mIsAvatarShown) {
            mIsAvatarShown = true;

        }
    }

    public void isOnline() {
        new HttpsRequest(Consts.ONLINE_OFFLINE_API, paramsUpdate, getActivity()).stringPost(TAG,
                (flag, msg, response) -> {
            if (flag) {
                ProjectUtils.showToast(getActivity(), msg);
                getArtist();

            } else {
                ProjectUtils.showToast(getActivity(), msg);
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
                    imageCompression = new ImageCompression(getActivity());
                    imageCompression.execute(pathOfImage);
                    imageCompression.setOnTaskFinishedEvent(new ImageCompression.AsyncResponse() {
                        @Override
                        public void processFinish(String imagePath) {
                            try {
                                file = new File(imagePath);

                                paramsFile = new HashMap<>();
                                paramsFile.put(Consts.IMAGE, file);
                                Log.e("image", imagePath);
                                params = new HashMap<>();
                                params.put(Consts.USER_ID, userDTO.getUser_id());
                                if (NetworkManager.isConnectToInternet(getActivity())) {
                                    updateProfileSelf();
                                } else {
                                    ProjectUtils.showToast(getActivity(), getResources().getString(R.string.internet_concation));
                                }


                                Log.e("image", imagePath);
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
                    bm = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), picUri);
                    pathOfImage = picUri.getPath();
                    imageCompression = new ImageCompression(getActivity());
                    imageCompression.execute(pathOfImage);
                    imageCompression.setOnTaskFinishedEvent(imagePath -> {
                        Log.e("image", imagePath);
                        try {
                            file = new File(imagePath);
                            paramsFile = new HashMap<>();
                            paramsFile.put(Consts.IMAGE, file);
                            Log.e("image", imagePath);
                            params = new HashMap<>();
                            params.put(Consts.USER_ID, userDTO.getUser_id());
                            if (NetworkManager.isConnectToInternet(getActivity())) {
                                updateProfileSelf();
                            } else {
                                ProjectUtils.showToast(getActivity(), getResources().getString(R.string.internet_concation));
                            }


                            Log.e("image", imagePath);
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

    }

    public void startCropping(Uri uri, int requestCode) {
        Intent intent = new Intent(getActivity(), MainFragment.class);
        intent.putExtra("imageUri", uri.toString());
        intent.putExtra("requestCode", requestCode);
        startActivityForResult(intent, requestCode);
    }

    public void updateProfileSelf() {
        new HttpsRequest(Consts.UPDATE_PROFILE_API, params, paramsFile, getActivity()).imagePost(TAG,
                (flag, msg, response) -> {
            if (flag) {
                try {
                    ProjectUtils.showToast(getActivity(), msg);

                    userDTO = new Gson().fromJson(response.getJSONObject("data").toString(), UserDTO.class);
                    prefrence.setParentUser(userDTO, Consts.USER_DTO);
                    baseActivity.showImage();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                ProjectUtils.showToast(getActivity(), msg);
            }


        });
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        baseActivity = (BaseActivity) activity;
    }

    public void deleteImage() {
        paramsDeleteImg.put(Consts.USER_ID, userDTO.getUser_id());
        new HttpsRequest(Consts.DELETE_PROFILE_IMAGE_API, paramsDeleteImg, getActivity()).stringPost(TAG, new Helper() {
            @Override
            public void backResponse(boolean flag, String msg, JSONObject response) {
                if (flag) {
                    userDTO.setImage("");
                    artistDetailsDTO.setImage("");
                    prefrence.setParentUser(userDTO, Consts.USER_DTO);
                    showData();
                } else {
                    ProjectUtils.showToast(getActivity(), msg);
                }


            }
        });
    }


    public void updateProfile() {
        paramsUpdate.put(Consts.USER_ID, userDTO.getUser_id());
        paramsUpdate.put(Consts.NAME, dialogBinding.etNameSelfD.getText().toString());
        paramsUpdate.put(Consts.EMAIL_ID, dialogBinding.etEmailD.getText().toString());
        paramsUpdate.put(Consts.MOBILE, dialogBinding.etMobileD.getText().toString());
        paramsUpdate.put(Consts.GENDER, genderString);
        paramsUpdate.put(Consts.COUNTRY_CODE, dialogBinding.ccp.getSelectedCountryCode());
        paramsUpdate.put(Consts.ID, artistDetailsDTO.getCurrency_id());

        ProjectUtils.showProgressDialog(baseActivity, true, getResources().getString(R.string.please_wait));
        new HttpsRequest(Consts.UPDATE_PROFILE_ARTIST_API, paramsUpdate, baseActivity).imagePost(TAG,
                (flag, msg, response) -> {
            ProjectUtils.pauseProgressDialog();
            dialog.dismiss();
            if (flag) {
                try {
                    ProjectUtils.showToast(baseActivity, msg);
                    artistDetailsDTO = new Gson().fromJson(response.getJSONObject("data").toString(), ArtistDetailsDTO.class);
                    userDTO.setMobile(dialogBinding.etMobileD.getText().toString());
                    prefrence.setParentUser(userDTO, Consts.USER_DTO);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ProjectUtils.showToast(baseActivity, msg);
            }
        });
    }

}
