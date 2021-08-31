package com.wokconns.wokconns.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cocosw.bottomsheet.BottomSheet
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wokconns.wokconns.R
import com.wokconns.wokconns.databinding.DialogDetailsInfoBinding
import com.wokconns.wokconns.databinding.FragmentArtistProfileNewBinding
import com.wokconns.wokconns.dto.ArtistDetailsDTO
import com.wokconns.wokconns.dto.CategoryDTO
import com.wokconns.wokconns.dto.UserDTO
import com.wokconns.wokconns.https.HttpsRequest
import com.wokconns.wokconns.interfacess.Const
import com.wokconns.wokconns.interfacess.Helper
import com.wokconns.wokconns.network.NetworkManager.isConnectToInternet
import com.wokconns.wokconns.preferences.SharedPrefs
import com.wokconns.wokconns.preferences.SharedPrefs.Companion.getInstance
import com.wokconns.wokconns.ui.activity.*
import com.wokconns.wokconns.utils.ImageCompression
import com.wokconns.wokconns.utils.MainFragment
import com.wokconns.wokconns.utils.ProjectUtils.*
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs

class ArtistProfileNew : Fragment(), View.OnClickListener, OnOffsetChangedListener {
    private lateinit var binding: FragmentArtistProfileNewBinding
    private var prefrence: SharedPrefs? = null
    private var userDTO: UserDTO? = null
    private var artistDetailsDTO: ArtistDetailsDTO? = null
    private val parms = HashMap<String, String?>()
    private val paramsCategory = HashMap<String, String?>()
    private var bundle = Bundle()
    private var mMaxScrollSize = 0
    private var mIsAvatarShown = true
    private var categoryDTOS = ArrayList<CategoryDTO?>()
    private var paramsUpdate = HashMap<String, String?>()
    private var paramsFile = HashMap<String, File?>()
    private lateinit var builder: BottomSheet.Builder
    private var picUri: Uri? = null
    private var imageName: String? = null
    private var pathOfImage: String? = null
    private var bm: Bitmap? = null
    private var imageCompression: ImageCompression? = null
    private var file: File? = null
    private var params: HashMap<String, String?> = HashMap()
    private lateinit var baseActivity: BaseActivity
    private val paramsDeleteImg = HashMap<String, String?>()
    private lateinit var dialogBinding: DialogDetailsInfoBinding
    private lateinit var dialog: Dialog
    private var genderString = "0"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_artist_profile_new,
            container,
            false
        )
        prefrence = getInstance(requireActivity())
        userDTO = prefrence?.getParentUser(Const.USER_DTO)
        baseActivity.headerNameTV.text = resources.getString(R.string.my_profile)
        paramsCategory[Const.USER_ID] = userDTO?.user_id
        parms[Const.ARTIST_ID] = userDTO?.user_id
        parms[Const.USER_ID] = userDTO?.user_id
        setUiAction()
        return binding.root
    }

    @SuppressLint("NonConstantResourceId")
    fun setUiAction() {
        binding.back.setOnClickListener(this)
        binding.llGallery.setOnClickListener(this)
        binding.llServices.setOnClickListener(this)
        binding.llWorks.setOnClickListener(this)
        binding.llReview.setOnClickListener(this)
        binding.tvEdit.setOnClickListener(this)
        binding.ivEdit.setOnClickListener(this)
        binding.llProfilePhoto.setOnClickListener(this)
        binding.swOnOff.setOnClickListener {
            if (artistDetailsDTO != null) {
                if (isConnectToInternet(requireActivity())) {
                    paramsUpdate = HashMap()
                    paramsUpdate[Const.USER_ID] = userDTO?.user_id
                    if (artistDetailsDTO?.is_online.equals("1", ignoreCase = true)) {
                        paramsUpdate[Const.IS_ONLINE] = "0"
                        isOnline
                    } else {
                        paramsUpdate[Const.IS_ONLINE] = "1"
                        isOnline
                    }
                } else {
                    showToast(
                        requireActivity(),
                        resources.getString(R.string.internet_concation)
                    )
                }
            } else {
                showToast(
                    requireActivity(),
                    resources.getString(R.string.incomplete_profile_msg)
                )
            }
        }
        builder = BottomSheet.Builder(requireActivity()).sheet(R.menu.menu_cards)
        builder.title(resources.getString(R.string.select_img))
        builder.listener { _: DialogInterface?, which: Int ->
            when (which) {
                R.id.camera_cards -> if (hasPermissionInManifest(
                        requireActivity(),
                        PICK_FROM_CAMERA,
                        Manifest.permission.CAMERA
                    )
                ) {
                    if (hasPermissionInManifest(
                            requireActivity(),
                            PICK_FROM_GALLERY,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        try {
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            val file = getOutputMediaFile(1)
                            if (file != null) {
                                if (!file.exists()) {
                                    try {
                                        pauseProgressDialog()
                                        file.createNewFile()
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                }
                                picUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    //Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.asd", newFile);
                                    FileProvider.getUriForFile(
                                        requireActivity().applicationContext,
                                        requireActivity().applicationContext.packageName + ".fileprovider",
                                        file
                                    )
                                } else {
                                    Uri.fromFile(file) // create
                                }

                                prefrence?.setValue(Const.IMAGE_URI_CAMERA, picUri.toString())
                                intent.putExtra(
                                    MediaStore.EXTRA_OUTPUT,
                                    picUri
                                ) // set the image file
                                startActivityForResult(intent, PICK_FROM_CAMERA)
                            } else showLong(
                                context, "Error: Could not fetch image from device storage; " +
                                        "please check your permissions!"
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                R.id.gallery_cards -> if (hasPermissionInManifest(
                        requireActivity(),
                        PICK_FROM_CAMERA, Manifest.permission.CAMERA
                    )
                ) {
                    if (hasPermissionInManifest(
                            requireActivity(),
                            PICK_FROM_GALLERY, Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        val file = getOutputMediaFile(1)

                        if (file != null) {
                            if (!file.exists()) {
                                try {
                                    file.createNewFile()
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }

                            picUri = Uri.fromFile(file)
                            val intent = Intent()
                            intent.type = "image/*"
                            intent.action = Intent.ACTION_GET_CONTENT
                            startActivityForResult(
                                Intent.createChooser(
                                    intent,
                                    resources.getString(R.string.select_pic)
                                ), PICK_FROM_GALLERY
                            )
                        } else showLong(
                            context, "Error: Could not fetch image from device storage; " +
                                    "please check your permissions!"
                        )
                    }
                }
                R.id.cancel_cards -> builder.setOnDismissListener { dialog1: DialogInterface -> dialog1.dismiss() }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isConnectToInternet(requireActivity())) {
            category
            artist
        } else {
            showToast(
                requireActivity(),
                resources.getString(R.string.internet_concation)
            )
        }
    }

    val category: Unit
        get() {
            HttpsRequest(Const.GET_ALL_CATEGORY_API, paramsCategory, requireActivity()).stringPost(
                TAG,
                object : Helper {
                    override fun backResponse(flag: Boolean, msg: String?, response: JSONObject?) {
                        if (flag) {
                            try {
                                categoryDTOS = ArrayList()
                                val getpetDTO = object : TypeToken<List<CategoryDTO?>?>() {}.type
                                categoryDTOS = Gson().fromJson<Any>(
                                    response?.getJSONArray("data").toString(),
                                    getpetDTO
                                ) as ArrayList<CategoryDTO?>
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                        }
                    }
                })
        }
    val artist: Unit
        get() {
            HttpsRequest(Const.GET_ARTIST_BY_ID_API, parms, requireActivity()).stringPost(
                TAG,
                object : Helper {
                    override fun backResponse(flag: Boolean, msg: String?, response: JSONObject?) {
                        if (flag) {
                            try {
                                artistDetailsDTO = Gson().fromJson(
                                    response?.getJSONObject("data").toString(),
                                    ArtistDetailsDTO::class.java
                                )
                                showData()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                        }
                    }
                })
        }

    @SuppressLint("UseCompatTextViewDrawableApis")
    fun showData() {
        bundle = Bundle()
        bundle.putSerializable(Const.ARTIST_DTO, artistDetailsDTO)

        Glide.with(requireActivity()).load(artistDetailsDTO?.image)
            .placeholder(R.drawable.dummyuser_image)
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.ivArtist)
        Glide.with(requireActivity()).load(artistDetailsDTO?.banner_image)
            .placeholder(R.drawable.banner_img)
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.ivBanner)

        binding.tvCategory.text = artistDetailsDTO?.category_name

        binding.tvJobCompleted.text = String.format(
            "%s %s",
            artistDetailsDTO?.jobDone, resources.getString(R.string.jobs_comleted)
        )

        binding.tvName.text = userDTO?.name

        binding.tvAbout.text = artistDetailsDTO?.about_us

        if (!userDTO?.email_id.equals("", ignoreCase = true))
            binding.tvEmail.text = userDTO?.email_id
        else
            binding.tvEmail.text = resources.getString(R.string.NA)
        if (!userDTO?.mobile.equals("", ignoreCase = true))
            binding.tvPhone.text = userDTO?.mobile
        else
            binding.tvPhone.text = resources.getString(R.string.NA)
        if (!userDTO?.gender.equals("", ignoreCase = true)) when (userDTO?.gender) {
            "0" -> binding.tvGender.text = resources.getString(R.string.female)
            "1" -> binding.tvGender.text = resources.getString(R.string.male)
            "2" -> binding.tvGender.text = resources.getString(R.string.other)
        } else binding.tvGender.text = resources.getString(R.string.NA)

        binding.simpleRatingBarOver.rating = artistDetailsDTO?.ava_rating?.toFloat() ?: 1F
        binding.tvHourlyRateValue.text =
            String.format("%s %s", artistDetailsDTO?.currency_symbol, artistDetailsDTO?.price)

        if (artistDetailsDTO?.is_online.equals("1", ignoreCase = true)) {
            binding.tvOnlineOffline.text = resources.getString(R.string.online)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.tvOnlineOffline.compoundDrawableTintList =
                    AppCompatResources.getColorStateList(
                        baseActivity, R.color.green
                    )
            }
            binding.swOnOff.isChecked = true
        } else {
            binding.tvOnlineOffline.text = resources.getString(R.string.offline)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.tvOnlineOffline.compoundDrawableTintList =
                    AppCompatResources.getColorStateList(
                        baseActivity, R.color.red
                    )
            }
            binding.swOnOff.isChecked = false
        }

        log("Text view ==> ${binding.tvEmail.text}")
    }

    @SuppressLint("NonConstantResourceId")
    override fun onClick(v: View) {
        when (v.id) {
            R.id.ivEditPersonal -> if (isConnectToInternet(requireActivity())) {
                if (categoryDTOS.size > 0) {
                    val intent = Intent(requireActivity(), EditPersonalInfo::class.java)
                    intent.putExtra(Const.ARTIST_DTO, artistDetailsDTO)
                    intent.putExtra(Const.CATEGORY_list, categoryDTOS)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(R.anim.slide_up, R.anim.stay)
                } else {
                    showLong(
                        requireActivity(),
                        resources.getString(R.string.try_after)
                    )
                }
            } else {
                showToast(
                    requireActivity(),
                    resources.getString(R.string.internet_concation)
                )
            }
            R.id.btnDelete -> if (artistDetailsDTO != null) {
                if (isConnectToInternet(requireActivity())) {
                    if (!artistDetailsDTO?.image.equals("", ignoreCase = true)) {
                        deleteImage()
                    } else {
                        showToast(
                            requireActivity(),
                            resources.getString(R.string.upload_image_first)
                        )
                    }
                } else {
                    showToast(
                        requireActivity(),
                        resources.getString(R.string.internet_concation)
                    )
                }
            } else {
                showToast(
                    requireActivity(),
                    resources.getString(R.string.incomplete_profile_msg)
                )
            }
            R.id.btnChange -> if (artistDetailsDTO != null) builder.show() else showToast(
                requireActivity(),
                resources.getString(R.string.incomplete_profile_msg)
            )
            R.id.back -> if (baseActivity.drawer.isDrawerVisible(GravityCompat.START)) baseActivity.drawer.closeDrawer(
                GravityCompat.START
            ) else baseActivity.drawer.openDrawer(GravityCompat.START)
            R.id.ll_gallery -> {
                val intent = Intent(baseActivity, ImageGallery::class.java)
                intent.putExtras(bundle)
                baseActivity.startActivity(intent)
            }
            R.id.ll_services -> {
//                val user = prefrence?.getParentUser(Const.USER_DTO)
//                log("User Name ===> ${user?.name}")
//                log("User Email ===> ${user?.email_id}")
//                log("Gender ===> ${user?.gender}")
//                log("User Mobile ===> ${user?.mobile}")
                val intent1 = Intent(baseActivity, Services::class.java)
                intent1.putExtras(bundle)
                baseActivity.startActivity(intent1)
            }
            R.id.ll_works -> {
                val intent2 = Intent(baseActivity, PreviousWork::class.java)
                intent2.putExtras(bundle)
                baseActivity.startActivity(intent2)
            }
            R.id.ll_review -> {
                val intent3 = Intent(baseActivity, Reviews::class.java)
                intent3.putExtras(bundle)
                baseActivity.startActivity(intent3)
            }
            R.id.tv_edit -> {
                val intent4 = Intent(requireActivity(), EditPersonalInfo::class.java)
                intent4.putExtra(Const.ARTIST_DTO, artistDetailsDTO)
                intent4.putExtra(Const.CATEGORY_list, categoryDTOS)
                startActivity(intent4)
                requireActivity().overridePendingTransition(R.anim.slide_up, R.anim.stay)
            }
            R.id.iv_edit -> dialogForSubmit()
            R.id.ll_profile_photo -> {
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    fun dialogForSubmit() {
        dialog = Dialog(baseActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(baseActivity),
            R.layout.dialog_details_info, null, false
        )
        dialog.setContentView(dialogBinding.root)
        dialog.show()
        dialog.setCancelable(true)
        dialogBinding.etNameSelfD.setText(userDTO?.name)
        dialogBinding.etEmailD.setText(userDTO?.email_id)
        dialogBinding.etMobileD.setText(userDTO?.mobile)
        dialogBinding.etEmailD.setOnClickListener {
            showLong(
                requireContext(),
                "Please write to support if you need to change " +
                        "this email address;\n\nUse title - \"Change E-mail Address\""
            )
        }
        dialogBinding.etMobileD.setOnClickListener {
            showLong(
                requireContext(),
                "Please write to support if you need to update " +
                        "your mobile number;\n\nUse title - \"Change Mobile Number\""
            )
        }
        try {
            dialogBinding.ccp.setDefaultCountryUsingNameCode("NG")
            dialogBinding.ccp.resetToDefaultCountry()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        when (userDTO?.gender) {
            "0" -> {
                (dialogBinding.rgGenderOptions.getChildAt(0) as RadioButton).isChecked = true
                genderString = "0"
            }
            "1" -> {
                (dialogBinding.rgGenderOptions.getChildAt(1) as RadioButton).isChecked = true
                genderString = "1"
            }
            "2" -> {
                (dialogBinding.rgGenderOptions.getChildAt(2) as RadioButton).isChecked = true
                genderString = "2"
            }
        }
        dialogBinding.tvSubmit.setOnClickListener {
            if (isConnectToInternet(baseActivity)) {
                when {
                    !isEditTextFilled(dialogBinding.etNameSelfD) -> {
                        showToast(baseActivity, resources.getString(R.string.val_name))
                        return@setOnClickListener
                    }
                    !isEditTextFilled(dialogBinding.etEmailD) -> {
                        showToast(baseActivity, resources.getString(R.string.val_email))
                        return@setOnClickListener
                    }
                    !isEditTextFilled(dialogBinding.etMobileD) -> {
                        showToast(baseActivity, resources.getString(R.string.val_phone))
                        return@setOnClickListener
                    }
                    else -> updateProfile()
                }
            } else {
            }
        }
        dialogBinding.ivClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.rgGenderOptions.setOnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
            when (checkedId) {
                R.id.rb_gender_female -> genderString = "0"
                R.id.rb_gender_male -> genderString = "1"
                R.id.rb_other -> genderString = "2"
            }
        }
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, i: Int) {
        if (mMaxScrollSize == 0) mMaxScrollSize = appBarLayout.totalScrollRange
        val percentage = abs(i) * 100 / mMaxScrollSize
        if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && mIsAvatarShown) mIsAvatarShown = false
        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !mIsAvatarShown) mIsAvatarShown = true
    }

    private val isOnline: Unit
        get() {
            HttpsRequest(Const.ONLINE_OFFLINE_API, paramsUpdate, requireActivity()).stringPost(TAG,
                object : Helper {
                    override fun backResponse(flag: Boolean, msg: String?, response: JSONObject?) {
                        if (flag) {
                            showToast(requireActivity(), msg)
                            artist
                        } else showToast(requireActivity(), msg)
                    }
                })
        }

    private fun getOutputMediaFile(type: Int): File? {
        val root = Environment.getExternalStorageDirectory().toString()
        val mediaStorageDir = File(root, Const.APP_NAME)
        /**Create the storage directory if it does not exist */
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }
        /**Create a media file name */
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(
            Date()
        )
        val mediaFile: File
        if (type == 1) {
            mediaFile = File(
                mediaStorageDir.path + File.separator +
                        Const.APP_NAME + timeStamp + ".png"
            )
            imageName = Const.APP_NAME + timeStamp + ".png"
        } else {
            return null
        }
        return mediaFile
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CROP_CAMERA_IMAGE) {
            if (data != null) {
                picUri = Uri.parse(data.extras?.getString("resultUri"))
                try {
                    //bitmap = MediaStore.Images.Media.getBitmap(SaveDetailsActivityNew.this.getContentResolver(), resultUri);
                    pathOfImage = picUri?.path
                    imageCompression = ImageCompression(requireActivity())
                    imageCompression?.execute(pathOfImage)
                    imageCompression?.setOnTaskFinishedEvent(
                        object : ImageCompression.AsyncResponse {
                            override fun processFinish(imagePath: String?) {
                                try {
                                    file = File(imagePath.toString())
                                    paramsFile = HashMap()
                                    paramsFile[Const.IMAGE] = file
                                    Log.e("image", imagePath.toString())
                                    params = HashMap()
                                    params[Const.USER_ID] = userDTO?.user_id
                                    if (isConnectToInternet(requireActivity())) {
                                        updateProfileSelf()
                                    } else {
                                        showToast(
                                            requireActivity(),
                                            resources.getString(R.string.internet_concation)
                                        )
                                    }
                                    Log.e("image", imagePath.toString())
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        if (requestCode == CROP_GALLERY_IMAGE) {
            if (data != null) {
                picUri = Uri.parse(data.extras?.getString("resultUri"))
                try {
                    bm =
                        MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, picUri)
                    pathOfImage = picUri?.path
                    imageCompression = ImageCompression(requireActivity())
                    imageCompression?.execute(pathOfImage)
                    imageCompression?.setOnTaskFinishedEvent(
                        object : ImageCompression.AsyncResponse {
                            override fun processFinish(imagePath: String?) {
                                Log.e("image", imagePath.toString())
                                try {
                                    file = File(imagePath.toString())
                                    paramsFile = HashMap()
                                    paramsFile[Const.IMAGE] = file
                                    Log.e("image", imagePath.toString())
                                    params = HashMap()
                                    params[Const.USER_ID] = userDTO?.user_id
                                    if (isConnectToInternet(requireActivity())) {
                                        updateProfileSelf()
                                    } else {
                                        showToast(
                                            requireActivity(),
                                            resources.getString(R.string.internet_concation)
                                        )
                                    }
                                    Log.e("image", imagePath.toString())
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        if (requestCode == PICK_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            if (picUri != null) {
                picUri = Uri.parse(prefrence?.getValue(Const.IMAGE_URI_CAMERA))
                startCropping(picUri, CROP_CAMERA_IMAGE)
            } else {
                picUri = Uri.parse(
                    prefrence?.getValue(Const.IMAGE_URI_CAMERA)
                )
                startCropping(picUri, CROP_CAMERA_IMAGE)
            }
        }
        if (requestCode == PICK_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            try {
                val tempUri = data?.data
                Log.e("front tempUri", "" + tempUri)
                if (tempUri != null) {
                    startCropping(tempUri, CROP_GALLERY_IMAGE)
                } else {
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        }
    }

    private fun startCropping(uri: Uri?, requestCode: Int) {
        val intent = Intent(requireActivity(), MainFragment::class.java)
        intent.putExtra("imageUri", uri.toString())
        intent.putExtra("requestCode", requestCode)
        startActivityForResult(intent, requestCode)
    }

    private fun updateProfileSelf() {
        HttpsRequest(Const.UPDATE_PROFILE_API, params, paramsFile, requireActivity()).imagePost(TAG,
            object : Helper {
                override fun backResponse(flag: Boolean, msg: String?, response: JSONObject?) {
                    if (flag) {
                        try {
                            showToast(requireActivity(), msg)
                            userDTO = Gson().fromJson(
                                response?.getJSONObject("data").toString(),
                                UserDTO::class.java
                            )
                            prefrence?.setParentUser(userDTO, Const.USER_DTO)
                            baseActivity.updateBaseActivityInfo()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        showToast(requireActivity(), msg)
                    }
                }
            })
    }

    override fun onAttach(activity: Context) {
        super.onAttach(activity)
        baseActivity = activity as BaseActivity
    }

    private fun deleteImage() {
        paramsDeleteImg[Const.USER_ID] = userDTO?.user_id
        HttpsRequest(Const.DELETE_PROFILE_IMAGE_API, paramsDeleteImg, requireActivity())
            .stringPost(TAG, object : Helper {
                override fun backResponse(flag: Boolean, msg: String?, response: JSONObject?) {
                    if (flag) {
                        userDTO?.image = ""
                        artistDetailsDTO?.image = ""
                        prefrence?.setParentUser(userDTO, Const.USER_DTO)
                        showData()
                    } else {
                        showToast(requireActivity(), msg)
                    }
                }
            })
    }

    private fun updateProfile() {
        paramsUpdate[Const.USER_ID] = userDTO?.user_id
        paramsUpdate[Const.NAME] = dialogBinding.etNameSelfD.text.toString()
        paramsUpdate[Const.EMAIL_ID] = dialogBinding.etEmailD.text.toString()
        paramsUpdate[Const.MOBILE] = dialogBinding.etMobileD.text.toString()
        paramsUpdate[Const.GENDER] = genderString
        paramsUpdate[Const.COUNTRY_CODE] = dialogBinding.ccp.selectedCountryCode
        if (artistDetailsDTO != null) paramsUpdate[Const.ID] = artistDetailsDTO?.currency_id

        showProgressDialog(baseActivity, false, resources.getString(R.string.please_wait))

        HttpsRequest(Const.UPDATE_PROFILE_ARTIST_API, paramsUpdate, baseActivity).imagePost(TAG,
            object : Helper {
                override fun backResponse(flag: Boolean, msg: String?, response: JSONObject?) {
                    pauseProgressDialog()

                    dialog.dismiss()

                    if (flag) {
                        try {
                            showToast(baseActivity, msg)
                            artistDetailsDTO = Gson().fromJson(
                                response?.getJSONObject("data").toString(),
                                ArtistDetailsDTO::class.java
                            )
                            userDTO = UserDTO.fromArtist(userDTO, artistDetailsDTO)

                            prefrence?.setParentUser(userDTO, Const.USER_DTO)

                            baseActivity.updateBaseActivityInfo()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        showData()
                    } else {
                        showToast(baseActivity, msg)
                    }
                }
            })
    }

    companion object {
        private const val PERCENTAGE_TO_ANIMATE_AVATAR = 20
        private val TAG = ArtistProfileNew::class.java.simpleName
        private var PICK_FROM_CAMERA = 1
        private var PICK_FROM_GALLERY = 2
        private var CROP_CAMERA_IMAGE = 3
        private var CROP_GALLERY_IMAGE = 4
    }
}