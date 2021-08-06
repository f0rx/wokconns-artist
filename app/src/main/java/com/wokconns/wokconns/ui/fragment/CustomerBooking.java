package com.wokconns.wokconns.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.FragmentCustomerBookingBinding;
import com.wokconns.wokconns.dto.ArtistBooking;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.interfacess.LocationFragmentManager;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.preferences.SharedPrefrence;
import com.wokconns.wokconns.ui.activity.BaseActivity;
import com.wokconns.wokconns.utils.ProjectUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Callable;

public class CustomerBooking extends LocationFragmentManager implements View.OnClickListener {
    private final String TAG = CustomerBooking.class.getSimpleName();
    private SharedPrefrence prefrence;
    private UserDTO userDTO;
    private final HashMap<String, String> paramsGetBooking = new HashMap<>();
    private ArtistBooking artistBooking;
    private MapView mMapView;
    private GoogleMap googleMap;
    private BaseActivity baseActivity;

    FragmentCustomerBookingBinding binding;
    View mCustomMarkerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_customer_booking, container, false);
        View view = binding.getRoot();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        baseActivity.headerNameTV.setText(getResources().getString(R.string.customer_booking));
        prefrence = SharedPrefrence.getInstance(getActivity());
        userDTO = prefrence.getParentUser(Consts.USER_DTO);
        paramsGetBooking.put(Consts.ARTIST_ID, userDTO.getUser_id());
        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(requireActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        panGoogleMap(prefrence.getValue(Consts.LATITUDE), prefrence.getValue(Consts.LONGITUDE),
                userDTO.getName(), userDTO.getAddress());
        setUiAction();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if (NetworkManager.isConnectToInternet(getActivity())) {
            if (!isShowingRationale) getBooking();
        } else {
            ProjectUtils.showToast(requireActivity(), getResources().getString(R.string.internet_concation));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void setUiAction() {
        binding.llAccept.setOnClickListener(this);
        binding.llDecline.setOnClickListener(this);
        binding.llStart.setOnClickListener(this);
        binding.llCancel.setOnClickListener(this);
        binding.llFinishJob.setOnClickListener(this);
    }

    void panGoogleMap(String latitude, String longitude, String markerTitle, String snippet) {
        panGoogleMap(latitude, longitude, markerTitle, snippet, true);
    }

    void panGoogleMap(String latitude, String longitude, String markerTitle, String snippet, boolean animate) {
        panGoogleMap(latitude, longitude, markerTitle, snippet, animate, null);
    }

    @SuppressLint("MissingPermission")
    void panGoogleMap(String latitude, String longitude, String markerTitle,
                      String snippet, boolean animate, Callable<Object> callback) {
        mMapView.getMapAsync(mMap -> {
            googleMap = mMap;

            requestLocationPermissions(isGranted -> {
                if (isGranted) {
                    showGPSRationale();

                    googleMap.setMyLocationEnabled(true);

                    // For dropping a marker at a point on the Map
                    LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                    googleMap.addMarker(new MarkerOptions().position(latLng)
                            .title(markerTitle == null ? "My Location" : markerTitle).snippet(snippet));

                    if (animate) {
                        // For zooming automatically to the location of the marker
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(10).build();
                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                }
            }, callback);
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llAccept:
                if (NetworkManager.isConnectToInternet(getActivity())) {
                    booking("1");
                } else {
                    ProjectUtils.showToast(requireActivity(), getResources().getString(R.string.internet_concation));
                }
                break;
            case R.id.llDecline:
                ProjectUtils.showDialog(requireActivity(), getResources().getString(R.string.dec_cpas),
                        getResources().getString(R.string.decline_msg),
                        (dialog, which) -> decline(), (dialog, which) -> {
                        }, false);

                break;
            case R.id.llStart:
                if (NetworkManager.isConnectToInternet(requireActivity())) {
                    booking("2");
                } else {
                    ProjectUtils.showToast(requireActivity(), getResources().getString(R.string.internet_concation));
                }
                break;
            case R.id.llCancel:
                ProjectUtils.showDialog(requireActivity(), getResources().getString(R.string.cancel), getResources().getString(R.string.cancel_msg),
                        (dialog, which) -> decline(), (dialog, which) -> {
                        }, false);
                break;
            case R.id.llFinishJob:
                ProjectUtils.showDialog(requireActivity(), getResources().getString(R.string.finish_the_job),
                        getResources().getString(R.string.finish_msg), (dialog, which) -> {
                            if (NetworkManager.isConnectToInternet(getActivity())) {
                                booking("3");
                            } else {
                                ProjectUtils.showToast(requireActivity(),
                                        getResources().getString(R.string.internet_concation));
                            }

                        }, (dialog, which) -> {
                        }, false);
                break;
        }

    }


    public void getBooking() {
        new HttpsRequest(Consts.CURRENT_BOOKING_API, paramsGetBooking, getActivity()).stringPost(TAG, (flag, msg, response) -> {
            if (flag) {
                binding.cardData.setVisibility(View.VISIBLE);
                binding.cardNoRequest.setVisibility(View.GONE);
                try {

                    artistBooking = new Gson().fromJson(response.getJSONObject("data").toString(), ArtistBooking.class);
                    showData();

                } catch (Exception e) {
                    e.printStackTrace();
                    binding.cardData.setVisibility(View.GONE);
                    binding.cardNoRequest.setVisibility(View.VISIBLE);
                }

            } else {
                binding.cardData.setVisibility(View.GONE);
                binding.cardNoRequest.setVisibility(View.VISIBLE);
                panGoogleMap(prefrence.getValue(Consts.LATITUDE), prefrence.getValue(Consts.LONGITUDE),
                        userDTO.getName(), userDTO.getAddress());
            }


        });
    }

    public Void showData() {
        binding.tvName.setText(artistBooking.getUserName());
        binding.tvLocation.setText(artistBooking.getAddress());
        binding.tvDate.setText(String.format("%s %s",
                ProjectUtils.changeDateFormate1(artistBooking.getBooking_date()),
                artistBooking.getBooking_time()));

        Glide.with(requireActivity()).
                load(artistBooking.getUserImage())
                .placeholder(R.drawable.dummyuser_image)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivArtist);

        binding.tvDescription.setText(artistBooking.getDescription());
        if (artistBooking.getBooking_type().equalsIgnoreCase("0") || artistBooking.getBooking_type().equalsIgnoreCase("3")) {

            if (artistBooking.getBooking_flag().equalsIgnoreCase("0")) {
                binding.llACDE.setVisibility(View.VISIBLE);
                binding.llTime.setVisibility(View.GONE);
                binding.llSt.setVisibility(View.GONE);
                binding.llFinishJob.setVisibility(View.GONE);

                binding.tvTxt.setText(String.format("%s %s[%s]",
                        getResources().getString(R.string.booking),
                        getResources().getString(R.string.pending),
                        artistBooking.getId()));
            } else if (artistBooking.getBooking_flag().equalsIgnoreCase("1")) {
                binding.llSt.setVisibility(View.VISIBLE);
                binding.llACDE.setVisibility(View.GONE);
                binding.llTime.setVisibility(View.GONE);
                binding.llFinishJob.setVisibility(View.GONE);

                binding.tvTxt.setText(String.format("%s %s[%s]",
                        getResources().getString(R.string.booking),
                        getResources().getString(R.string.acc),
                        artistBooking.getId()));
            } else if (artistBooking.getBooking_flag().equalsIgnoreCase("3")) {

                binding.llSt.setVisibility(View.GONE);
                binding.llACDE.setVisibility(View.GONE);
                binding.llTime.setVisibility(View.VISIBLE);
                binding.llFinishJob.setVisibility(View.VISIBLE);
                binding.llWork.setVisibility(View.GONE);

                SimpleDateFormat sdf = new SimpleDateFormat("mm.ss", Locale.getDefault());

                try {
                    Date dt;

                    if (artistBooking.getWorking_min().equalsIgnoreCase("0")) {
                        dt = sdf.parse("0.1");

                    } else {
                        dt = sdf.parse(artistBooking.getWorking_min());

                    }
                    sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                    System.out.println(sdf.format(dt));
                    int min = dt.getHours() * 60 + dt.getMinutes();
                    int sec = dt.getSeconds();
                    binding.chronometer.setBase(SystemClock.elapsedRealtime() - (min * 60000 + sec * 1000));
                    binding.chronometer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                binding.tvTxt.setText(String.format("%s %s[%s]",
                        getResources().getString(R.string.booking),
                        getResources().getString(R.string.acc),
                        artistBooking.getId()));
            }

            panGoogleMap(artistBooking.getC_latitude(), artistBooking.getC_longitude(),
                    artistBooking.getUserName(), artistBooking.getAddress(), false, this::showData);

        } else if (artistBooking.getBooking_type().equalsIgnoreCase("2")) {

            if (artistBooking.getBooking_flag().equalsIgnoreCase("0")) {
                binding.llACDE.setVisibility(View.VISIBLE);
                binding.llTime.setVisibility(View.GONE);
                binding.llSt.setVisibility(View.GONE);
                binding.llFinishJob.setVisibility(View.GONE);
                binding.llWork.setVisibility(View.GONE);

                binding.tvTxt.setText(getResources().getString(R.string.booking) + " " + getResources().getString(R.string.pending) + "[" + artistBooking.getId() + "]");
            } else if (artistBooking.getBooking_flag().equalsIgnoreCase("1")) {
                binding.llSt.setVisibility(View.VISIBLE);
                binding.llACDE.setVisibility(View.GONE);
                binding.llTime.setVisibility(View.GONE);
                binding.llFinishJob.setVisibility(View.GONE);
                binding.llWork.setVisibility(View.GONE);

                binding.tvTxt.setText(getResources().getString(R.string.booking) + " " + getResources().getString(R.string.acc) + "[" + artistBooking.getId() + "]");
            } else if (artistBooking.getBooking_flag().equalsIgnoreCase("3")) {

                binding.llSt.setVisibility(View.GONE);
                binding.llACDE.setVisibility(View.GONE);
                binding.llTime.setVisibility(View.VISIBLE);
                binding.llFinishJob.setVisibility(View.VISIBLE);
                binding.llWork.setVisibility(View.GONE);

                SimpleDateFormat sdf = new SimpleDateFormat("mm.ss", Locale.getDefault());

                try {
                    Date dt;

                    if (artistBooking.getWorking_min().equalsIgnoreCase("0")) {
                        dt = sdf.parse("0.1");

                    } else {
                        dt = sdf.parse(artistBooking.getWorking_min());

                    }
                    sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                    System.out.println(sdf.format(dt));
                    int min = dt.getHours() * 60 + dt.getMinutes();
                    int sec = dt.getSeconds();
                    binding.chronometer.setBase(SystemClock.elapsedRealtime() - (min * 60000 + sec * 1000));
                    binding.chronometer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                binding.tvTxt.setText(String.format("%s %s[%s]",
                        getResources().getString(R.string.booking),
                        getResources().getString(R.string.acc),
                        artistBooking.getId()));

            }

            panGoogleMap(artistBooking.getC_latitude(), artistBooking.getC_longitude(),
                    artistBooking.getUserName(), artistBooking.getAddress(), false);
        }

        return null;
    }

    public void booking(String req) {
        HashMap<String, String> paramsBookingOp = new HashMap<>();
        paramsBookingOp.put(Consts.BOOKING_ID, artistBooking.getId());
        paramsBookingOp.put(Consts.REQUEST, req);
        paramsBookingOp.put(Consts.USER_ID, artistBooking.getUser_id());
        ProjectUtils.showProgressDialog(baseActivity, true, getResources().getString(R.string.please_wait));
        new HttpsRequest(Consts.BOOKING_OPERATION_API, paramsBookingOp, getActivity()).stringPost(TAG, (flag, msg, response) -> {
            ProjectUtils.pauseProgressDialog();
            if (flag) {
                ProjectUtils.showToast(baseActivity, msg);
                getBooking();


                try {
                    baseActivity.ivSearch.setVisibility(View.GONE);
                    baseActivity.rlheader.setVisibility(View.VISIBLE);

                    BaseActivity.navItemIndex = 9;
                    BaseActivity.CURRENT_TAG = BaseActivity.TAG_HISTORY;
                    baseActivity.loadHomeFragment(new HistoryFragment(), BaseActivity.CURRENT_TAG);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ProjectUtils.showToast(baseActivity, msg);
            }


        });
    }

    public void decline() {
        HashMap<String, String> paramsDecline = new HashMap<>();
        paramsDecline.put(Consts.USER_ID, userDTO.getUser_id());
        paramsDecline.put(Consts.BOOKING_ID, artistBooking.getId());
        paramsDecline.put(Consts.DECLINE_BY, "1");
        paramsDecline.put(Consts.DECLINE_REASON, "Busy");
        ProjectUtils.showProgressDialog(getActivity(), true, getResources().getString(R.string.please_wait));
        new HttpsRequest(Consts.DECLINE_BOOKING_API, paramsDecline, getActivity()).stringPost(TAG, (flag, msg, response) -> {
            ProjectUtils.pauseProgressDialog();
            if (flag) {
                ProjectUtils.showToast(getActivity(), msg);
                getBooking();

            } else {
                ProjectUtils.showToast(getActivity(), msg);
            }


        });
    }

    @Override
    public void onAttach(@NonNull Context activity) {
        super.onAttach(activity);
        baseActivity = (BaseActivity) activity;
    }

    /**
     * @param view   is custom marker layout which we will convert into bitmap.
     * @param bitmap is the image which you want to show in marker.
     * @return
     */
    private Bitmap getMarkerBitmapFromView(View view, Bitmap bitmap) {

//        mMarkerImageView.setImageBitmap(bitmap);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = view.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        view.draw(canvas);
        return returnedBitmap;

    }

    private void addCustomMarkerFromURL() {
        mCustomMarkerView = ((LayoutInflater) baseActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
        if (googleMap == null) {
            return;
        }
        // adding a marker with image from URL using glide image loading library
        try {
            Glide
                    .with(baseActivity)
                    .asBitmap()
                    .load(userDTO.getImage())
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {

                            // For dropping a marker at a point on the Map
                            LatLng sydney = new LatLng(Double.parseDouble(artistBooking.getC_latitude()), Double.parseDouble(artistBooking.getC_longitude()));
                            googleMap.addMarker(new MarkerOptions().position(sydney)
                                    .title(artistBooking.getUserName()).snippet(artistBooking.getAddress())
                                    .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomMarkerView, bitmap))));

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Bitmap getBitmapFromLink(String link) {
        try {
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try {
                connection.connect();
            } catch (Exception e) {
                Log.v("asfwqeds", e.getMessage());
            }
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            Log.v("asfwqeds", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
