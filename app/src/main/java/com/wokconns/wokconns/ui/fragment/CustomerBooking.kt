package com.wokconns.wokconns.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.*
import android.os.Bundle
import android.os.StrictMode
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.wokconns.wokconns.R
import com.wokconns.wokconns.databinding.FragmentCustomerBookingBinding
import com.wokconns.wokconns.dto.ArtistBooking
import com.wokconns.wokconns.dto.UserDTO
import com.wokconns.wokconns.https.HttpsRequest
import com.wokconns.wokconns.interfacess.Const
import com.wokconns.wokconns.interfacess.Helper
import com.wokconns.wokconns.interfacess.LocationFragmentManager
import com.wokconns.wokconns.network.NetworkManager
import com.wokconns.wokconns.preferences.SharedPrefs
import com.wokconns.wokconns.preferences.SharedPrefs.Companion.getInstance
import com.wokconns.wokconns.ui.activity.BaseActivity
import com.wokconns.wokconns.utils.ProjectUtils
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Callable

class CustomerBooking : LocationFragmentManager(), View.OnClickListener {
    private var prefrence: SharedPrefs? = null
    private var userDTO: UserDTO? = null
    private val paramsGetBooking = HashMap<String, String?>()
    private var artistBooking: ArtistBooking? = null
    private lateinit var mMapView: MapView
    private var googleMap: GoogleMap? = null
    private lateinit var baseActivity: BaseActivity
    private lateinit var binding: FragmentCustomerBookingBinding
    var mCustomMarkerView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_customer_booking, container, false)
        val view = binding.root
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        baseActivity.headerNameTV.text = resources.getString(R.string.customer_booking)
        prefrence = getInstance(activity)
        userDTO = prefrence?.getParentUser(Const.USER_DTO)
        paramsGetBooking[Const.ARTIST_ID] = userDTO?.user_id
        mMapView = view.findViewById(R.id.mapView)
        mMapView.onCreate(savedInstanceState)
        mMapView.onResume() // needed to get the map to display immediately
        try {
            MapsInitializer.initialize(requireActivity().applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        panGoogleMap(
            prefrence?.getValue(Const.LATITUDE), prefrence?.getValue(Const.LONGITUDE),
            userDTO?.name, userDTO?.address
        )
        setUiAction()
        return view
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
        if (NetworkManager.isConnectToInternet(activity)) {
            if (!isShowingRationale) booking
        } else {
            ProjectUtils.showToast(
                requireActivity(),
                resources.getString(R.string.internet_concation)
            )
        }
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }

    fun setUiAction() {
        binding.llAccept.setOnClickListener(this)
        binding.llDecline.setOnClickListener(this)
        binding.llStart.setOnClickListener(this)
        binding.llCancel.setOnClickListener(this)
        binding.llFinishJob.setOnClickListener(this)
    }

    @JvmOverloads
    fun panGoogleMap(
        latitude: String?,
        longitude: String?,
        markerTitle: String?,
        snippet: String?,
        animate: Boolean = true
    ) {
        panGoogleMap(latitude, longitude, markerTitle, snippet, animate, null)
    }

    @SuppressLint("MissingPermission")
    fun panGoogleMap(
        latitude: String?, longitude: String?, markerTitle: String?,
        snippet: String?, animate: Boolean, callback: Callable<Any?>?
    ) {
        mMapView.getMapAsync { mMap: GoogleMap? ->
            googleMap = mMap
            requestLocationPermissions({ isGranted: Boolean ->
                if (isGranted) {
                    showGPSRationale()
                    googleMap?.isMyLocationEnabled = true

                    // For dropping a marker at a point on the Map
                    if (latitude != null && longitude != null) {
                        val latLng = LatLng(latitude.toDouble(), longitude.toDouble())
                        googleMap?.addMarker(
                            MarkerOptions().position(latLng)
                                .title(markerTitle ?: "My Location").snippet(snippet)
                        )
                        if (animate) {
                            // For zooming automatically to the location of the marker
                            val cameraPosition =
                                CameraPosition.Builder().target(latLng).zoom(10f).build()
                            googleMap?.animateCamera(
                                CameraUpdateFactory.newCameraPosition(
                                    cameraPosition
                                )
                            )
                        }
                    }
                }
            }, callback)
        }
    }

    @SuppressLint("NonConstantResourceId")
    override fun onClick(v: View) {
        when (v.id) {
            R.id.llAccept -> if (NetworkManager.isConnectToInternet(activity)) {
                booking("1")
            } else {
                ProjectUtils.showToast(
                    requireActivity(),
                    resources.getString(R.string.internet_concation)
                )
            }
            R.id.llDecline -> ProjectUtils.showDialog(
                requireActivity(),
                resources.getString(R.string.dec_cpas),
                resources.getString(R.string.decline_msg),
                { dialog: DialogInterface?, which: Int -> decline() },
                { dialog: DialogInterface?, which: Int -> },
                false
            )
            R.id.llStart -> if (NetworkManager.isConnectToInternet(requireActivity())) {
                booking("2")
            } else {
                ProjectUtils.showToast(
                    requireActivity(),
                    resources.getString(R.string.internet_concation)
                )
            }
            R.id.llCancel -> ProjectUtils.showDialog(
                requireActivity(),
                resources.getString(R.string.cancel),
                resources.getString(R.string.cancel_msg),
                { dialog: DialogInterface?, which: Int -> decline() },
                { dialog: DialogInterface?, which: Int -> },
                false
            )
            R.id.llFinishJob -> ProjectUtils.showDialog(
                requireActivity(),
                resources.getString(R.string.finish_the_job),
                resources.getString(R.string.finish_msg),
                { dialog: DialogInterface?, which: Int ->
                    if (NetworkManager.isConnectToInternet(
                            activity
                        )
                    ) {
                        booking("3")
                    } else {
                        ProjectUtils.showToast(
                            requireActivity(),
                            resources.getString(R.string.internet_concation)
                        )
                    }
                },
                { dialog: DialogInterface?, which: Int -> },
                false
            )
        }
    }

    val booking: Unit
        get() {
            HttpsRequest(Const.CURRENT_BOOKING_API, paramsGetBooking, requireActivity()).stringPost(
                Companion.TAG,
                object : Helper {
                    override fun backResponse(flag: Boolean, msg: String?, response: JSONObject?) {
                        if (flag) {
                            binding.cardData.visibility = View.VISIBLE
                            binding.cardNoRequest.visibility = View.GONE
                            try {
                                artistBooking = Gson().fromJson(
                                    response?.getJSONObject("data").toString(),
                                    ArtistBooking::class.java
                                )
                                showData()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                binding.cardData.visibility = View.GONE
                                binding.cardNoRequest.setVisibility(View.VISIBLE)
                            }
                        } else {
                            binding.cardData.visibility = View.GONE
                            binding.cardNoRequest.visibility = View.VISIBLE
                            panGoogleMap(
                                prefrence?.getValue(Const.LATITUDE),
                                prefrence?.getValue(Const.LONGITUDE),
                                userDTO?.name,
                                userDTO?.address
                            )
                        }
                    }
                })
        }

    fun showData(): Void? {
        binding.tvName.text = artistBooking?.userName
        binding.tvLocation.text = artistBooking?.address
        binding.tvDate.text = String.format(
            "%s %s",
            ProjectUtils.changeDateFormate1(artistBooking?.booking_date),
            artistBooking?.booking_time
        )
        Glide.with(requireActivity()).load(artistBooking?.userImage)
            .placeholder(R.drawable.dummyuser_image)
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.ivArtist)
        binding.tvDescription.text = artistBooking?.description
        if (artistBooking?.booking_type.equals(
                "0",
                ignoreCase = true
            ) || artistBooking?.booking_type.equals("3", ignoreCase = true)
        ) {
            if (artistBooking?.booking_flag.equals("0", ignoreCase = true)) {
                binding.llACDE.visibility = View.VISIBLE
                binding.llTime.visibility = View.GONE
                binding.llSt.visibility = View.GONE
                binding.llFinishJob.visibility = View.GONE
                binding.tvTxt.text = String.format(
                    "%s %s[%s]",
                    resources.getString(R.string.booking),
                    resources.getString(R.string.pending),
                    artistBooking?.id
                )
            } else if (artistBooking?.booking_flag.equals("1", ignoreCase = true)) {
                binding.llSt.visibility = View.VISIBLE
                binding.llACDE.visibility = View.GONE
                binding.llTime.visibility = View.GONE
                binding.llFinishJob.visibility = View.GONE
                binding.tvTxt.text = String.format(
                    "%s %s[%s]",
                    resources.getString(R.string.booking),
                    resources.getString(R.string.acc),
                    artistBooking?.id
                )
            } else if (artistBooking?.booking_flag.equals("3", ignoreCase = true)) {
                binding.llSt.visibility = View.GONE
                binding.llACDE.visibility = View.GONE
                binding.llTime.visibility = View.VISIBLE
                binding.llFinishJob.visibility = View.VISIBLE
                binding.llWork.visibility = View.GONE
                var sdf = SimpleDateFormat("mm.ss", Locale.getDefault())
                try {
                    val dt = if (artistBooking?.working_min.equals("0", ignoreCase = true)) {
                        sdf.parse("0.1")
                    } else {
                        sdf.parse(artistBooking?.working_min)
                    }
                    sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    println(sdf.format(dt))
                    val min = dt.hours * 60 + dt.minutes
                    val sec = dt.seconds
                    binding.chronometer.base =
                        SystemClock.elapsedRealtime() - (min * 60000 + sec * 1000)
                    binding.chronometer.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                binding.tvTxt.text = String.format(
                    "%s %s[%s]",
                    resources.getString(R.string.booking),
                    resources.getString(R.string.acc),
                    artistBooking?.id
                )
            }
            panGoogleMap(
                artistBooking?.c_latitude, artistBooking?.c_longitude,
                artistBooking?.userName, artistBooking?.address, false
            ) { showData() }
        } else if (artistBooking?.booking_type.equals("2", ignoreCase = true)) {
            if (artistBooking?.booking_flag.equals("0", ignoreCase = true)) {
                binding.llACDE.visibility = View.VISIBLE
                binding.llTime.visibility = View.GONE
                binding.llSt.visibility = View.GONE
                binding.llFinishJob.visibility = View.GONE
                binding.llWork.visibility = View.GONE
                (resources.getString(R.string.booking) + " " +
                        resources.getString(R.string.pending) + "[" + artistBooking?.id + "]").also {
                    binding.tvTxt.text = it
                }
            } else if (artistBooking?.booking_flag.equals("1", ignoreCase = true)) {
                binding.llSt.visibility = View.VISIBLE
                binding.llACDE.visibility = View.GONE
                binding.llTime.visibility = View.GONE
                binding.llFinishJob.visibility = View.GONE
                binding.llWork.visibility = View.GONE
                (resources.getString(R.string.booking) + " " +
                        resources.getString(R.string.acc) + "[" + artistBooking?.id + "]").also {
                    binding.tvTxt.text = it
                }
            } else if (artistBooking?.booking_flag.equals("3", ignoreCase = true)) {
                binding.llSt.visibility = View.GONE
                binding.llACDE.visibility = View.GONE
                binding.llTime.visibility = View.VISIBLE
                binding.llFinishJob.visibility = View.VISIBLE
                binding.llWork.visibility = View.GONE
                var sdf = SimpleDateFormat("mm.ss", Locale.getDefault())
                try {
                    val dt: Date? = if (artistBooking?.working_min.equals("0", ignoreCase = true)) {
                        sdf.parse("0.1")
                    } else {
                        sdf.parse(artistBooking?.working_min)
                    }
                    sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    println(sdf.format(dt))
                    if (dt != null) {
                        val min = dt.hours * 60 + dt.minutes
                        val sec = dt.seconds
                        binding.chronometer.base =
                            SystemClock.elapsedRealtime() - (min * 60000 + sec * 1000)
                        binding.chronometer.start()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                binding.tvTxt.text = String.format(
                    "%s %s[%s]",
                    resources.getString(R.string.booking),
                    resources.getString(R.string.acc),
                    artistBooking?.id
                )
            }
            panGoogleMap(
                artistBooking?.c_latitude, artistBooking?.c_longitude,
                artistBooking?.userName, artistBooking?.address, false
            )
        }
        return null
    }

    fun booking(req: String) {
        val paramsBookingOp = HashMap<String, String?>()
        paramsBookingOp[Const.BOOKING_ID] = artistBooking?.id
        paramsBookingOp[Const.REQUEST] = req
        paramsBookingOp[Const.USER_ID] = artistBooking?.user_id
        ProjectUtils.showProgressDialog(
            baseActivity,
            true,
            resources.getString(R.string.please_wait)
        )
        HttpsRequest(Const.BOOKING_OPERATION_API, paramsBookingOp, requireActivity()).stringPost(
            Companion.TAG,
            object : Helper {
                override fun backResponse(flag: Boolean, msg: String?, response: JSONObject?) {
                    ProjectUtils.pauseProgressDialog()
                    if (flag) {
                        ProjectUtils.showToast(baseActivity, msg)
                        booking
                        try {
                            baseActivity.ivSearch.visibility = View.GONE
                            baseActivity.rlheader.visibility = View.VISIBLE
                            BaseActivity.navItemIndex = 9
                            BaseActivity.CURRENT_TAG = BaseActivity.TAG_HISTORY
                            baseActivity.loadHomeFragment(
                                HistoryFragment(),
                                BaseActivity.CURRENT_TAG
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        ProjectUtils.showToast(baseActivity, msg)
                    }
                }
            })
    }

    private fun decline() {
        val paramsDecline = HashMap<String, String?>()
        paramsDecline[Const.USER_ID] = userDTO?.user_id
        paramsDecline[Const.BOOKING_ID] = artistBooking?.id
        paramsDecline[Const.DECLINE_BY] = "1"
        paramsDecline[Const.DECLINE_REASON] = "Busy"
        ProjectUtils.showProgressDialog(activity, true, resources.getString(R.string.please_wait))
        HttpsRequest(Const.DECLINE_BOOKING_API, paramsDecline, requireActivity()).stringPost(
            Companion.TAG,
            object : Helper {
                override fun backResponse(flag: Boolean, msg: String?, response: JSONObject?) {
                    ProjectUtils.pauseProgressDialog()
                    if (flag) {
                        ProjectUtils.showToast(activity, msg)
                        booking
                    } else {
                        ProjectUtils.showToast(activity, msg)
                    }
                }
            })
    }

    override fun onAttach(activity: Context) {
        super.onAttach(activity)
        baseActivity = activity as BaseActivity
    }

    /**
     * @param view   is custom marker layout which we will convert into bitmap.
     * @param bitmap is the image which you want to show in marker.
     * @return
     */
    private fun getMarkerBitmapFromView(view: View?, bitmap: Bitmap): Bitmap? {

//        mMarkerImageView.setImageBitmap(bitmap);
        view?.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        view?.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view?.buildDrawingCache()
        var returnedBitmap: Bitmap? = null

        if (view != null) {
            returnedBitmap = Bitmap.createBitmap(
                view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(returnedBitmap)
            canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN)
            val drawable = view.background
            drawable?.draw(canvas)
            view.draw(canvas)
        }
        return returnedBitmap
    }

    private fun addCustomMarkerFromURL() {
        mCustomMarkerView =
            (baseActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                R.layout.view_custom_marker,
                null
            )
        if (googleMap == null) {
            return
        }
        // adding a marker with image from URL using glide image loading library
        try {
            Glide
                .with(baseActivity)
                .asBitmap()
                .load(userDTO?.image)
                .into(object : SimpleTarget<Bitmap?>() {
                    override fun onResourceReady(
                        bitmap: Bitmap,
                        transition: Transition<in Bitmap?>?
                    ) {

                        // For dropping a marker at a point on the Map
                        if (artistBooking != null) {
                            val sydney = LatLng(
                                artistBooking!!.c_latitude.toDouble(),
                                artistBooking!!.c_longitude.toDouble()
                            )

                            val bm = getMarkerBitmapFromView(mCustomMarkerView, bitmap)

                            bm?.let {
                                googleMap?.addMarker(
                                    MarkerOptions().position(sydney)
                                        .title(artistBooking?.userName).snippet(artistBooking?.address)
                                        .icon(BitmapDescriptorFactory.fromBitmap(it)))
                            }
                        }
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getBitmapFromLink(link: String?): Bitmap? {
        return try {
            val url = URL(link)
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.connect()
            } catch (e: Exception) {
                Log.v("asfwqeds", e.message.toString())
            }
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            Log.v("asfwqeds", e.message.toString())
            e.printStackTrace()
            null
        }
    }

    companion object {
        private val TAG = CustomerBooking::class.java.simpleName
    }
}