package com.wokconns.wokconns.ui.activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.cocosw.bottomsheet.BottomSheet
import com.google.gson.Gson
import com.wokconns.wokconns.R
import com.wokconns.wokconns.databinding.ActivityImageGalleryBinding
import com.wokconns.wokconns.databinding.DailogArGallryBinding
import com.wokconns.wokconns.dto.ArtistDetailsDTO
import com.wokconns.wokconns.dto.GalleryDTO
import com.wokconns.wokconns.dto.UserDTO
import com.wokconns.wokconns.https.HttpsRequest
import com.wokconns.wokconns.interfacess.Const
import com.wokconns.wokconns.interfacess.Helper
import com.wokconns.wokconns.network.NetworkManager
import com.wokconns.wokconns.preferences.SharedPrefs
import com.wokconns.wokconns.preferences.SharedPrefs.Companion.getInstance
import com.wokconns.wokconns.ui.adapter.AdapterGallery
import com.wokconns.wokconns.utils.ImageCompression
import com.wokconns.wokconns.utils.MainFragment
import com.wokconns.wokconns.utils.ProjectUtils
import com.wokconns.wokconns.utils.ProjectUtils.showLong
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ImageGallery : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityImageGalleryBinding
    private lateinit var context: Context
    private lateinit var builder: BottomSheet.Builder
    private var picUri: Uri? = null
    private var imageName: String? = null
    private var pathOfImage: String? = null
    private lateinit var bm: Bitmap
    private lateinit var imageCompression: ImageCompression
    private lateinit var resultByteArray: ByteArray
    private lateinit var file: File
    private lateinit var bitmap: Bitmap
    private lateinit var binding1: DailogArGallryBinding
    private lateinit var view: View
    private var artistDetailsDTO: ArtistDetailsDTO? = null
    private var galleryList: ArrayList<GalleryDTO?>? = arrayListOf()
    private lateinit var adapterGallery: AdapterGallery
    private var bundle: Bundle? = null
    private lateinit var gridLayoutManager: GridLayoutManager
    private var paramsUpdate: HashMap<String, String?> = HashMap()
    private lateinit var dialogEditGallery: Dialog
    private var paramsFile: HashMap<String, File?> = HashMap()
    private var userDTO: UserDTO? = null
    private var prefrence: SharedPrefs? = null
    private val params = HashMap<String, String?>()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_gallery)
        context = this@ImageGallery
        prefrence = getInstance(context)
        userDTO = prefrence?.getParentUser(Const.USER_DTO)
        params[Const.ARTIST_ID] = userDTO?.user_id
        params[Const.USER_ID] = userDTO?.user_id
        bundle = intent.extras
        if (bundle != null) {
            artistDetailsDTO = bundle?.getSerializable(Const.ARTIST_DTO) as ArtistDetailsDTO?
        }
        showUiAction()
    }

    override fun onResume() {
        super.onResume()
        try {
            artist
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showUiAction() {
        binding.ivClose.setOnClickListener(this)
        binding.llGalleryAdd.setOnClickListener(this)
        binding.llBack.setOnClickListener(this)
        builder = BottomSheet.Builder((context as Activity)).sheet(R.menu.menu_cards)
        builder.title(resources.getString(R.string.select_img))
        builder.listener { _: DialogInterface?, which: Int ->
            when (which) {
                R.id.camera_cards -> if (ProjectUtils.hasPermissionInManifest(
                        context,
                        PICK_FROM_CAMERA,
                        Manifest.permission.CAMERA
                    )
                ) {
                    if (ProjectUtils.hasPermissionInManifest(
                            context,
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
                                        ProjectUtils.pauseProgressDialog()
                                        file.createNewFile()
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                }

                                picUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    //Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.asd", newFile);
                                    FileProvider.getUriForFile(
                                        context.applicationContext,
                                        context.applicationContext.packageName + ".fileprovider",
                                        file
                                    )
                                } else {
                                    Uri.fromFile(file) // create
                                }

                                prefrence?.setValue(Const.IMAGE_URI_CAMERA, picUri.toString())
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, picUri)
                                // set the image file
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
                R.id.gallery_cards -> if (ProjectUtils.hasPermissionInManifest(
                        context,
                        PICK_FROM_CAMERA,
                        Manifest.permission.CAMERA
                    )
                ) {
                    if (ProjectUtils.hasPermissionInManifest(
                            context,
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
                            context, "Error: Could not fetch image from device storage; " +
                                    "please check your permissions!"
                        )
                    }
                }
                R.id.cancel_cards -> builder.setOnDismissListener(DialogInterface.OnDismissListener { dialog1: DialogInterface -> dialog1.dismiss() })
            }
        }

//        showData();
    }

    fun showData() {
        gridLayoutManager = GridLayoutManager(context, 2)
        galleryList = ArrayList()
        galleryList = artistDetailsDTO?.gallery
        adapterGallery = AdapterGallery(this@ImageGallery, galleryList, "gallery")
        binding.rvGallery.layoutManager = gridLayoutManager
        binding.rvGallery.adapter = adapterGallery
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ivClose -> binding.rlZoomImg.visibility = View.GONE
            R.id.ll_gallery_add -> builder.show()
            R.id.llBack -> finish()
        }
    }

    fun showImg(imgURL: String?) {
        binding.rlZoomImg.visibility = View.VISIBLE
        Glide
            .with(context)
            .load(imgURL)
            .placeholder(R.drawable.dummyuser_image)
            .into(binding.ivZoom)
    }

    val parentData: Unit
        get() {
            artist
        }

    fun addGalleryClick() {
        dialogGallery()
    }

    fun dialogGallery() {
        paramsUpdate = HashMap()
        paramsFile = HashMap()
        dialogEditGallery = Dialog(context /*, android.R.style.Theme_Dialog*/)
        dialogEditGallery.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogEditGallery.requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding1 = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dailog_ar_gallry,
            null,
            false
        )
        dialogEditGallery.setContentView(binding1.root)
        dialogEditGallery.show()
        dialogEditGallery.setCancelable(false)
        binding1.etImageGallD.setOnClickListener { v: View? -> builder.show() }
        binding1.tvNoGall.setOnClickListener { v: View? -> dialogEditGallery.dismiss() }
        binding1.tvYesGall.setOnClickListener { v: View? ->
            paramsUpdate[Const.USER_ID] = userDTO?.user_id
            paramsFile[Const.IMAGE] = file
            if (NetworkManager.isConnectToInternet(context)) {
                if (binding1.etImageGallD.text.toString().length > 0) {
                    addGallery()
                } else {
                    showLong(context, resources.getString(R.string.val_iamg_ad))
                }
            } else {
                ProjectUtils.showToast(context, resources.getString(R.string.internet_concation))
            }
        }
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

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CROP_CAMERA_IMAGE) {
            if (data != null) {
                picUri = Uri.parse(data.extras?.getString("resultUri"))
                try {
                    //bitmap = MediaStore.Images.Media.getBitmap(SaveDetailsActivityNew.this.getContentResolver(), resultUri);
                    pathOfImage = picUri?.path
                    imageCompression = ImageCompression(context)
                    imageCompression.execute(pathOfImage)
                    imageCompression.setOnTaskFinishedEvent(
                        object : ImageCompression.AsyncResponse {
                            override fun processFinish(imagePath: String?) {
                                try {
                                    // bitmap = MediaStore.Images.Media.getBitmap(SaveDetailsActivityNew.this.getContentResolver(), resultUri);
                                    file = File(imagePath.toString())
                                    //                                binding1.etImageGallD.setText(imagePath);
                                    Log.e("image", imagePath.toString())
                                    paramsUpdate = HashMap()
                                    paramsFile = HashMap()
                                    addGalleryImage()
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
                    bm = MediaStore.Images.Media.getBitmap(context.contentResolver, picUri)
                    pathOfImage = picUri?.path
                    imageCompression = ImageCompression(context)
                    imageCompression.execute(pathOfImage)
                    imageCompression.setOnTaskFinishedEvent(
                        object : ImageCompression.AsyncResponse {
                            override fun processFinish(imagePath: String?) {
                                Log.e("image", imagePath.toString())
                                try {
                                    file = File(imagePath.toString())
//                                    binding1.etImageGallD.setText(imagePath);
                                    Log.e("image", imagePath.toString())
                                    paramsUpdate = HashMap()
                                    paramsFile = HashMap()
                                    addGalleryImage()
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
                picUri = Uri.parse(prefrence?.getValue(Const.IMAGE_URI_CAMERA))
                startCropping(picUri, CROP_CAMERA_IMAGE)
            } else {
                picUri = Uri.parse(
                    prefrence?.getValue(Const.IMAGE_URI_CAMERA)
                )
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
    }

    fun startCropping(uri: Uri?, requestCode: Int) {
        val intent = Intent(context, MainFragment::class.java)
        intent.putExtra("imageUri", uri.toString())
        intent.putExtra("requestCode", requestCode)
        startActivityForResult(intent, requestCode)
    }

    fun addGalleryImage() {
        paramsUpdate[Const.USER_ID] = userDTO?.user_id
        paramsFile[Const.IMAGE] = file
        if (NetworkManager.isConnectToInternet(context)) {
            addGallery()
        } else {
            ProjectUtils.showToast(context, resources.getString(R.string.internet_concation))
        }
    }

    fun addGallery() {
        ProjectUtils.showProgressDialog(context, true, resources.getString(R.string.please_wait))
        HttpsRequest(Const.ADD_GALLERY_API, paramsUpdate, paramsFile, context).imagePost(
            TAG,
            object : Helper {
                override fun backResponse(flag: Boolean, msg: String?, response: JSONObject?) {
                    ProjectUtils.pauseProgressDialog()
                    if (flag) {
                        ProjectUtils.showToast(context, msg)
                        artist
                    } else {
                        ProjectUtils.showToast(context, msg)
                    }
                }
            })
    }

    val artist: Unit
        get() {
            HttpsRequest(Const.GET_ARTIST_BY_ID_API, params, context).stringPost(
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

    companion object {
        private val TAG = ImageGallery::class.java.simpleName
        var PICK_FROM_CAMERA = 1
        var PICK_FROM_GALLERY = 2
        var CROP_CAMERA_IMAGE = 3
        var CROP_GALLERY_IMAGE = 4
    }
}