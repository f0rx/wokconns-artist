package com.wokconns.wokconns.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cocosw.bottomsheet.BottomSheet
import com.google.android.gms.location.places.Place
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.schibstedspain.leku.LocationPickerActivity
import com.wokconns.wokconns.R
import com.wokconns.wokconns.databinding.ActivityEditPersnoalInfoBinding
import com.wokconns.wokconns.dto.ArtistDetailsDTO
import com.wokconns.wokconns.dto.CategoryDTO
import com.wokconns.wokconns.dto.CurrencyDTO
import com.wokconns.wokconns.dto.UserDTO
import com.wokconns.wokconns.https.HttpsRequest
import com.wokconns.wokconns.interfacess.*
import com.wokconns.wokconns.interfacess.Const.Companion.LATITUDE
import com.wokconns.wokconns.interfacess.Const.Companion.LONGITUDE
import com.wokconns.wokconns.network.NetworkManager
import com.wokconns.wokconns.preferences.SharedPrefs
import com.wokconns.wokconns.utils.ImageCompression
import com.wokconns.wokconns.utils.MainFragment
import com.wokconns.wokconns.utils.ProjectUtils.*
import com.wokconns.wokconns.utils.SpinnerDialog
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class EditPersonalInfo : LocationActivityManager(), View.OnClickListener {
    private val TAG = EditPersonalInfo::class.java.simpleName
    private lateinit var binding: ActivityEditPersnoalInfoBinding
    private lateinit var mContext: Context
    private var categoryDTOS: ArrayList<CategoryDTO>? = ArrayList()
    private var currencyDTOArrayList = ArrayList<CurrencyDTO>()
    private lateinit var spinnerDialogCate: SpinnerDialog
    private var artistDetailsDTO: ArtistDetailsDTO? = null
    private val place: Place? = null
    private var lats = 0.0
    private var longs = 0.0
    private val paramsUpdate = HashMap<String, String?>()
    private var userDTO: UserDTO? = null
    private var preferences: SharedPrefs? = null

    //    private ImageView ivBanner;
    lateinit var builder: BottomSheet.Builder
    var picUri: Uri? = null
    var PICK_FROM_CAMERA = 1
    var PICK_FROM_GALLERY = 2
    var CROP_CAMERA_IMAGE = 3
    var CROP_GALLERY_IMAGE = 4
    var imageName: String? = null
    var pathOfImage: String? = null
    var bm: Bitmap? = null
    var imageCompression: ImageCompression? = null
    var resultByteArray: ByteArray? = null
    var file: File? = null
    var fileProfile: File? = null
    var bitmap: Bitmap? = null
    private val paramsFile = HashMap<String, File?>()
    private val paramsFileProfile = HashMap<String, File?>()
    private var params: HashMap<String, String?> = HashMap()
    var currencyId: String? = ""
    var type: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_persnoal_info)
        mContext = this@EditPersonalInfo
        preferences = SharedPrefs.getInstance(mContext)
        userDTO = preferences?.getParentUser(Const.USER_DTO)
        if (intent.hasExtra(Const.CATEGORY_list)) {
            categoryDTOS =
                intent.getSerializableExtra(Const.CATEGORY_list) as ArrayList<CategoryDTO>?
            artistDetailsDTO = intent.getSerializableExtra(Const.ARTIST_DTO) as ArtistDetailsDTO?
        }
        setUiAction()
    }

    @SuppressLint("NonConstantResourceId")
    fun setUiAction() {
        binding.etCategoryD.setOnClickListener(this)
        binding.etLocationD.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)
        binding.llBack.setOnClickListener(this)
        binding.llBanner.setOnClickListener(this)
        binding.llProfilePhoto.setOnClickListener(this)
        binding.etBioD.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                binding.bioLength.text = String.format("%s/40", s.length)
            }
        })
        binding.etAboutD.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                binding.aboutLength.text = String.format(Locale.getDefault(), "%d/200", s.length)
            }
        })
        builder = BottomSheet.Builder(this@EditPersonalInfo).sheet(R.menu.menu_cards)
        builder.title(resources.getString(R.string.select_img))
        builder.listener { dialog: DialogInterface?, which: Int ->
            when (which) {
                R.id.camera_cards -> if (hasPermissionInManifest(
                        this@EditPersonalInfo,
                        PICK_FROM_CAMERA,
                        Manifest.permission.CAMERA
                    )
                ) {
                    if (hasPermissionInManifest(
                            this@EditPersonalInfo,
                            PICK_FROM_GALLERY,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        try {
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            val file = getOutputMediaFile(1)
                            if (file != null) {
                                if (file.exists()) {
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
                                        mContext.applicationContext,
                                        mContext.applicationContext.packageName + ".fileprovider",
                                        file
                                    )
                                } else {
                                    Uri.fromFile(file) // create
                                }

                                preferences?.setValue(Const.IMAGE_URI_CAMERA, picUri.toString())
                                intent.putExtra(
                                    MediaStore.EXTRA_OUTPUT,
                                    picUri
                                ) // set the image file
                                startActivityForResult(intent, PICK_FROM_CAMERA)
                            } else showLong(
                                mContext, "Error: Could not fetch image from device storage; " +
                                        "please check your permissions!"
                            )

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                R.id.gallery_cards -> if (hasPermissionInManifest(
                        this@EditPersonalInfo,
                        PICK_FROM_CAMERA,
                        Manifest.permission.CAMERA
                    )
                ) {
                    if (hasPermissionInManifest(
                            this@EditPersonalInfo,
                            PICK_FROM_GALLERY,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
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
                            mContext, "Error: Could not fetch image from device storage; " +
                                    "please check your permissions!"
                        )
                    }
                }
                R.id.cancel_cards -> builder.setOnDismissListener { obj: DialogInterface -> obj.dismiss() }
            }
        }
        spinnerDialogCate = SpinnerDialog(
            mContext as Activity?,
            categoryDTOS,
            resources.getString(R.string.select_cate)
        ) // With 	Animation
        spinnerDialogCate.bindOnSpinerListener { item: String?, id: String?, position: Int ->
            binding.etCategoryD.setText(item)
            paramsUpdate[Const.CATEGORY_ID] = id
        }
        if (artistDetailsDTO != null) {
            showData()
        }
        binding.etRateD.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.toString().length == 1 && s.toString().startsWith("0")) {
                    s.clear()
                }
            }
        })
        binding.etCurrency.setOnClickListener { v: View? -> binding.etCurrency.showDropDown() }
        binding.etCurrency.onItemClickListener =
            OnItemClickListener { parent: AdapterView<*>, view: View?, position: Int, id: Long ->
                binding.etCurrency.showDropDown()
                val currencyDTO = parent.getItemAtPosition(position) as CurrencyDTO
                Log.e(TAG, "onItemClick: " + currencyDTO.currency_symbol)
                currencyId = currencyDTO.id
                paramsUpdate[Const.ID] = currencyId
            }
        try {
            setCurrencyValue()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getOutputMediaFile(type: Int): File? {
        val root = externalCacheDir?.path
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

    fun showData() {
        if (categoryDTOS != null) {
            for (j in categoryDTOS!!.indices) {
                if (categoryDTOS!![j].id.equals(artistDetailsDTO?.category_id, ignoreCase = true)) {
                    categoryDTOS!![j].isSelected = true
                    binding.etCategoryD.setText(categoryDTOS!![j].cat_name)
                    //                binding.tvText.setText(String.format("%s%s%s", getResources().getString(R.string.commis_msg),
//                        categoryDTOS.get(j).getCurrency_type(), categoryDTOS.get(j).getPrice()));
                }
            }
        }
        spinnerDialogCate = SpinnerDialog(
            mContext as Activity?,
            categoryDTOS,
            resources.getString(R.string.select_cate)
        ) // With 	Animation
        spinnerDialogCate.bindOnSpinerListener { item: String?, id: String?, position: Int ->
            binding.etCategoryD.setText(item)
            paramsUpdate[Const.CATEGORY_ID] = id
        }
        binding.etCategoryD.setText(artistDetailsDTO?.category_name)
        binding.etNameD.setText(artistDetailsDTO?.name)
        binding.etBioD.setText(artistDetailsDTO?.bio)
        binding.etAboutD.setText(artistDetailsDTO?.about_us)
        binding.etCityD.setText(artistDetailsDTO?.city)
        binding.etCountry.setText(artistDetailsDTO?.country)
        binding.etLocationD.setText(artistDetailsDTO?.location)
        binding.etRateD.setText(artistDetailsDTO?.price)

        Glide.with(mContext).load(artistDetailsDTO?.banner_image)
            .placeholder(R.drawable.banner_img)
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.ivBanner)
        Glide.with(mContext).load(artistDetailsDTO?.image)
            .placeholder(R.drawable.banner_img)
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.civProfile)

        binding.etCurrency.setText(
            String.format(
                "(%s)%s",
                artistDetailsDTO?.currency_symbol,
                artistDetailsDTO?.currency_name
            )
        )
        currencyId = artistDetailsDTO?.currency_id
        paramsUpdate[Const.ID] = currencyId
    }

    private fun setCurrencyValue() {
        HttpsRequest(Const.GET_CURRENCY_API, mContext).stringGet(
            TAG,
            object : Helper {
                override fun backResponse(flag: Boolean, msg: String?, response: JSONObject?) {
                    if (flag) {
//                    ProjectUtils.showToast(mContext, msg);
                        try {
                            currencyDTOArrayList = ArrayList()
                            val getCurrencyDTO = object : TypeToken<List<CurrencyDTO?>?>() {}.type
                            currencyDTOArrayList = Gson().fromJson(
                                response?.getJSONArray("data").toString(),
                                getCurrencyDTO
                            )
                            try {
                                val currencyAdapter = ArrayAdapter(
                                    mContext,
                                    android.R.layout.simple_list_item_1, currencyDTOArrayList
                                )
                                binding.etCurrency.setAdapter(currencyAdapter)
                                binding.etCurrency.isCursorVisible = false

                                // Initialize value with a default
                                var naira: CurrencyDTO? = null
                                // Loop thru then find Naira (NGN)
                                for (el in currencyDTOArrayList) if (el.code == "NGN")
                                    naira = el

                                binding.etCurrency.postDelayed({
//                                    binding.etCurrency.showDropDown()
                                    binding.etCurrency.setText(
                                        String.format("%s", naira), false
                                    )
                                    binding.etCurrency.setSelection(binding.etCurrency.text.length)

                                    currencyId = naira?.id
                                    paramsUpdate[Const.ID] = currencyId
                                }, 500)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        showToast(mContext, msg)
                    }
                }
            })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.etCategoryD -> if (NetworkManager.isConnectToInternet(mContext)) {
                if (categoryDTOS != null && categoryDTOS!!.size > 0)
                    spinnerDialogCate.showSpinerDialog()
            } else {
                showToast(mContext, resources.getString(R.string.internet_concation))
            }
            R.id.etLocationD -> {
                requestLocationPermissions { isGranted: Boolean -> if (isGranted) showGPSRationale() }
                if (NetworkManager.isConnectToInternet(mContext)) {
                    findPlace()
                } else {
                    showToast(
                        mContext,
                        resources.getString(R.string.internet_concation)
                    )
                }
            }
            R.id.btnSubmit -> if (NetworkManager.isConnectToInternet(mContext)) {
                submitPersonalProfile()
            } else {
                showToast(mContext, resources.getString(R.string.internet_concation))
            }
            R.id.ll_banner -> {
                type = "banner"
                builder.show()
            }
            R.id.llBack -> {
                finish()
                overridePendingTransition(R.anim.stay, R.anim.slide_down)
            }
            R.id.ll_profile_photo -> {
                type = "profile"
                builder.show()
            }
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed();
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CROP_CAMERA_IMAGE) {
            if (data != null) {
                picUri = Uri.parse(data.extras?.getString("resultUri"))
                try {
                    //bitmap = MediaStore.Images.Media.getBitmap(SaveDetailsActivityNew.this.getContentResolver(), resultUri);
                    pathOfImage = picUri?.path
                    imageCompression = ImageCompression(this@EditPersonalInfo)
                    imageCompression?.execute(pathOfImage)
                    imageCompression?.setOnTaskFinishedEvent(
                        object : ImageCompression.AsyncResponse {
                            override fun processFinish(imagePath: String?) {
                                try {
                                    // bitmap = MediaStore.Images.Media.getBitmap(SaveDetailsActivityNew.this.getContentResolver(), resultUri);
                                    if (type.equals("profile", ignoreCase = true)) {
                                        fileProfile = File(imagePath.toString())
                                        Glide.with(mContext).load("file://$imagePath")
                                            .thumbnail(0.5f)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .into(binding.civProfile)
                                        Log.e("image", imagePath.toString())
                                        params = HashMap()
                                        params[Const.USER_ID] = userDTO?.user_id
                                        paramsFileProfile[Const.IMAGE] = fileProfile!!
                                        if (NetworkManager.isConnectToInternet(mContext)) {
                                            updateProfileSelf()
                                        } else {
                                            showToast(
                                                mContext,
                                                resources.getString(R.string.internet_concation)
                                            )
                                        }
                                    } else if (type.equals("banner", ignoreCase = true)) {
                                        file = File(imagePath.toString())
                                        Glide.with(mContext).load("file://$imagePath")
                                            .thumbnail(0.5f)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .into(binding.ivBanner)
                                        Log.e("image", imagePath.toString())
                                    }
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
                    bm = MediaStore.Images.Media.getBitmap(mContext.contentResolver, picUri)
                    pathOfImage = picUri?.path
                    imageCompression = ImageCompression(this@EditPersonalInfo)
                    imageCompression?.execute(pathOfImage)
                    imageCompression?.setOnTaskFinishedEvent(
                        object : ImageCompression.AsyncResponse {
                            override fun processFinish(imagePath: String?) {
                                Log.e("image", imagePath.toString())
                                try {
                                    if (type.equals("profile", ignoreCase = true)) {
                                        fileProfile = File(imagePath.toString())
                                        Glide.with(mContext).load("file://$imagePath")
                                            .thumbnail(0.5f)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .into(binding.civProfile)
                                        Log.e("image", imagePath.toString())
                                        params = HashMap()
                                        params[Const.USER_ID] = userDTO?.user_id
                                        paramsFileProfile[Const.IMAGE] = fileProfile!!
                                        if (NetworkManager.isConnectToInternet(mContext)) {
                                            updateProfileSelf()
                                        } else {
                                            showToast(
                                                mContext,
                                                resources.getString(R.string.internet_concation)
                                            )
                                        }
                                    } else if (type.equals("banner", ignoreCase = true)) {
                                        file = File(imagePath.toString())
                                        Glide.with(mContext).load("file://$imagePath")
                                            .thumbnail(0.5f)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .into(binding.ivBanner)
                                        Log.e("image", imagePath.toString())
                                    }
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
        if (requestCode == PICK_FROM_CAMERA && resultCode == RESULT_OK) {
            if (picUri != null) {
                picUri = Uri.parse(preferences?.getValue(Const.IMAGE_URI_CAMERA))
                startCropping(picUri, CROP_CAMERA_IMAGE)
            } else {
                picUri = Uri.parse(preferences?.getValue(Const.IMAGE_URI_CAMERA))
                startCropping(picUri, CROP_CAMERA_IMAGE)
            }
        }
        if (requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK) {
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
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                try {
                    getAddress(
                        data?.getDoubleExtra(LATITUDE, 0.0),
                        data?.getDoubleExtra(LONGITUDE, 0.0)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun startCropping(uri: Uri?, requestCode: Int) {
        val intent = Intent(mContext, MainFragment::class.java)
        intent.putExtra("imageUri", uri.toString())
        intent.putExtra("requestCode", requestCode)
        startActivityForResult(intent, requestCode)
    }

    private fun findPlace() {
        val locationPickerIntent: Intent = LocationPickerActivity.Builder()
            .withGooglePlacesEnabled() //.withLocation(41.4036299, 2.1743558)
            .build(mContext)
        startActivityForResult(locationPickerIntent, 101)
    }

    fun getAddress(lat: Double?, lng: Double?) {
        val geocoder = Geocoder(this@EditPersonalInfo, Locale.getDefault())

        if (lat == null || lng == null) return

        try {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            val obj = addresses[0]
            var add = obj.getAddressLine(0)
            add = """
                $add
                ${obj.countryName}
                """.trimIndent()
            add = """
                $add
                ${obj.countryCode}
                """.trimIndent()
            add = """
                $add
                ${obj.adminArea}
                """.trimIndent()
            add = """
                $add
                ${obj.postalCode}
                """.trimIndent()
            add = """
                $add
                ${obj.subAdminArea}
                """.trimIndent()
            add = """
                $add
                ${obj.locality}
                """.trimIndent()
            add = """
                $add
                ${obj.subThoroughfare}
                """.trimIndent()
            Log.e("IGA", "Address$add")
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);
            binding.etLocationD.setText(obj.getAddressLine(0))
            lats = lat
            longs = lng
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    private fun submitPersonalProfile() {
        if (!validation(binding.etCategoryD, resources.getString(R.string.val_cat_sele))) {
            return
        } else if (!validation(binding.etNameD, resources.getString(R.string.val_name))) {
            return
        } else if (!validation(binding.etBioD, resources.getString(R.string.val_bio))) {
            return
        } else if (!validation(binding.etAboutD, resources.getString(R.string.val_about))) {
            return
        } else if (!validation(binding.etCityD, resources.getString(R.string.val_city))) {
            return
        } else if (!validation(binding.etCountry, resources.getString(R.string.val_country))) {
            return
        } else if (!validation(binding.etLocationD, resources.getString(R.string.val_location))) {
            return
        } else if (!validation(binding.etRateD, resources.getString(R.string.val_rate))) {
            return
        } else {
            if (NetworkManager.isConnectToInternet(mContext)) {
                paramsUpdate[Const.USER_ID] = userDTO?.user_id
                paramsUpdate[Const.NAME] = getEditTextValue(
                    binding.etNameD
                )
                paramsUpdate[Const.BIO] = getEditTextValue(
                    binding.etBioD
                )
                paramsUpdate[Const.ABOUT_US] = getEditTextValue(
                    binding.etAboutD
                )
                paramsUpdate[Const.CITY] = getEditTextValue(
                    binding.etCityD
                )
                paramsUpdate[Const.COUNTRY] = getEditTextValue(
                    binding.etCountry
                )
                paramsUpdate[Const.LOCATION] = getEditTextValue(
                    binding.etLocationD
                )
                paramsUpdate[Const.PRICE] = getEditTextValue(
                    binding.etRateD
                )
                paramsFile[Const.BANNER_IMAGE] = file
                if (lats != 0.0) paramsUpdate[LATITUDE] = lats.toString()
                if (longs != 0.0) paramsUpdate[LONGITUDE] = longs.toString()
                updateProfile()
            } else {
                showToast(mContext, resources.getString(R.string.internet_concation))
            }
        }
    }

    private fun validation(editText: EditText?, msg: String?): Boolean {
        return if (!isEditTextFilled(editText)) {
            showLong(mContext, msg)
            false
        } else {
            true
        }
    }

    private fun updateProfile() {
        showProgressDialog(mContext, true, resources.getString(R.string.please_wait))

        val callback = object : Helper {
            override fun backResponse(flag: Boolean, msg: String?, response: JSONObject?) {
                pauseProgressDialog()
                if (flag) {
                    try {
                        showToast(mContext, msg)
                        artistDetailsDTO = Gson().fromJson(
                            response?.getJSONObject("data").toString(),
                            ArtistDetailsDTO::class.java
                        )
                        userDTO?.is_profile = 1
                        preferences?.setParentUser(userDTO, Const.USER_DTO)
                        finish()
                        overridePendingTransition(R.anim.stay, R.anim.slide_down)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    showToast(mContext, msg)
                }
            }
        }

        HttpsRequest(
            Const.UPDATE_PROFILE_ARTIST_API, paramsUpdate, paramsFile, mContext
        ).imagePost(TAG, callback)
    }

    private fun updateProfileSelf() {
        HttpsRequest(Const.ARTIST_IMAGE_API, params, paramsFileProfile, mContext).imagePost(
            TAG,
            object : Helper {
                override fun backResponse(flag: Boolean, msg: String?, response: JSONObject?) {
                    if (flag) {
                        try {
                            showToast(mContext, msg)
                            var temp = 0
                            if (userDTO?.is_profile == 1) {
                                temp = 1
                            }
                            userDTO = Gson().fromJson(
                                response?.getJSONObject("data").toString(),
                                UserDTO::class.java
                            )
                            userDTO?.is_profile = temp
                            preferences?.setParentUser(userDTO, Const.USER_DTO)
                            Glide.with(mContext).load(userDTO?.image)
                                .placeholder(R.drawable.dummyuser_image)
                                .dontAnimate()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(binding.civProfile)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        showToast(mContext, msg)
                    }
                }
            })
    }
}