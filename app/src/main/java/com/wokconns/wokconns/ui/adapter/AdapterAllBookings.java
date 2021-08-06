package com.wokconns.wokconns.ui.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.AdapterAllBookingsBinding;
import com.wokconns.wokconns.databinding.DialogEditPriceBindingImpl;
import com.wokconns.wokconns.dto.ArtistBooking;
import com.wokconns.wokconns.dto.ArtistDetailsDTO;
import com.wokconns.wokconns.dto.ChatListDTO;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.https.HttpsRequest;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.interfacess.DisclaimerWarning;
import com.wokconns.wokconns.network.NetworkManager;
import com.wokconns.wokconns.ui.activity.OneTwoOneChat;
import com.wokconns.wokconns.ui.fragment.NewBookings;
import com.wokconns.wokconns.utils.CustomTextView;
import com.wokconns.wokconns.utils.ProjectUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdapterAllBookings extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements DisclaimerWarning {
    private final String TAG = AdapterAllBookings.class.getSimpleName();
    private final NewBookings newBookings;
    private final ArrayList<ArtistBooking> artistBookingsList;
    private final UserDTO userDTO;
    private final ArtistDetailsDTO artistDetails;
    private LayoutInflater myInflater;
    private final Context context;
    private HashMap<String, String> paramsPricing;
    private ArrayList<ChatListDTO> chatHistory;
    private final int VIEW_ITEM = 1;

    public AdapterAllBookings(NewBookings newBookings, ArrayList<ArtistBooking> artistBookingsList,
                              UserDTO userDTO, ArtistDetailsDTO artistDetails, LayoutInflater myInflater) {
        this.newBookings = newBookings;
        this.artistBookingsList = artistBookingsList;
        this.userDTO = userDTO;
        this.artistDetails = artistDetails;
        this.myInflater = myInflater;
        context = newBookings.requireActivity();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int viewType) {
        RecyclerView.ViewHolder vh;
        if (myInflater == null) {
            myInflater = LayoutInflater.from(viewGroup.getContext());
        }
        if (viewType == VIEW_ITEM) {
            AdapterAllBookingsBinding binding =
                    DataBindingUtil.inflate(myInflater, R.layout.adapter_all_bookings, viewGroup, false);
            vh = new MyViewHolder(binding);
        } else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_section, viewGroup, false);
            vh = new MyViewHolderSection(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holderMain, final int position) {
        if (holderMain instanceof MyViewHolder) {
            MyViewHolder holder = (MyViewHolder) holderMain;

            holder.binding.tvName.setText(artistBookingsList.get(position).getUserName());
            holder.binding.tvBookingTitle.setText(artistBookingsList.get(position).getTitle());
            holder.binding.tvLocation.setText(artistBookingsList.get(position).getAddress());
            holder.binding.tvDate.setText(String.format("%s %s", ProjectUtils.changeDateFormate1(artistBookingsList.get(position).getBooking_date()),
                    artistBookingsList.get(position).getBooking_time()));

            Glide.with(context).
                    load(artistBookingsList.get(position).getUserImage())
//                    .placeholder(R.drawable.dummyuser_image)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.binding.ivArtist);

            holder.binding.tvDescription.setText(artistBookingsList.get(position).getDescription());

            if (artistBookingsList.get(position).getBooking_type().equalsIgnoreCase("0") || artistBookingsList.get(position).getBooking_type().equalsIgnoreCase("3")) {

                if (artistBookingsList.get(position).getBooking_flag().equalsIgnoreCase("0")) {
                    holder.binding.llACDE.setVisibility(View.VISIBLE);
                    holder.binding.llSt.setVisibility(View.GONE);
                    holder.binding.tvCompleted.setVisibility(View.GONE);
                    holder.binding.tvRejected.setVisibility(View.GONE);

                    holder.binding.tvTxt.setText(String.format("%s %s[%s]", context.getResources().getString(R.string.booking),
                            context.getResources().getString(R.string.pending), artistBookingsList.get(position).getId()));
                } else if (artistBookingsList.get(position).getBooking_flag().equalsIgnoreCase("1")) {
                    holder.binding.llSt.setVisibility(View.VISIBLE);
                    holder.binding.llACDE.setVisibility(View.GONE);
                    holder.binding.tvCompleted.setVisibility(View.GONE);
                    holder.binding.tvRejected.setVisibility(View.GONE);

                    holder.binding.tvTxt.setText(String.format("%s %s[%s]", context.getResources().getString(R.string.booking),
                            context.getResources().getString(R.string.acc), artistBookingsList.get(position).getId()));
                } else if (artistBookingsList.get(position).getBooking_flag().equalsIgnoreCase("2")) {
                    holder.binding.llSt.setVisibility(View.GONE);
                    holder.binding.llACDE.setVisibility(View.GONE);
                    holder.binding.tvCompleted.setVisibility(View.GONE);
                    holder.binding.tvRejected.setVisibility(View.VISIBLE);

                    holder.binding.tvTxt.setText(String.format("%s %s[%s]", context.getResources().getString(R.string.booking),
                            context.getResources().getString(R.string.rejected1), artistBookingsList.get(position).getId()));
                } else if (artistBookingsList.get(position).getBooking_flag().equalsIgnoreCase("4")) {
                    holder.binding.llSt.setVisibility(View.GONE);
                    holder.binding.llACDE.setVisibility(View.GONE);
                    holder.binding.tvCompleted.setVisibility(View.VISIBLE);
                    holder.binding.tvRejected.setVisibility(View.GONE);

                    holder.binding.tvTxt.setText(String.format("%s %s[%s]", context.getResources().getString(R.string.booking),
                            context.getResources().getString(R.string.completed1), artistBookingsList.get(position).getId()));
                }


            } else if (artistBookingsList.get(position).getBooking_type().equalsIgnoreCase("2")) {

                if (artistBookingsList.get(position).getBooking_flag().equalsIgnoreCase("0")) {
                    holder.binding.llACDE.setVisibility(View.VISIBLE);
                    holder.binding.llSt.setVisibility(View.GONE);
                    holder.binding.tvCompleted.setVisibility(View.GONE);
                    holder.binding.tvRejected.setVisibility(View.GONE);

                    holder.binding.tvTxt.setText(String.format("%s %s[%s]", context.getResources().getString(R.string.booking),
                            context.getResources().getString(R.string.pending), artistBookingsList.get(position).getId()));
                } else if (artistBookingsList.get(position).getBooking_flag().equalsIgnoreCase("1")) {
                    holder.binding.llSt.setVisibility(View.VISIBLE);
                    holder.binding.llACDE.setVisibility(View.GONE);
                    holder.binding.tvCompleted.setVisibility(View.GONE);
                    holder.binding.tvRejected.setVisibility(View.GONE);

                    holder.binding.tvTxt.setText(String.format("%s %s[%s]", context.getResources().getString(R.string.booking),
                            context.getResources().getString(R.string.acc), artistBookingsList.get(position).getId()));
                } else if (artistBookingsList.get(position).getBooking_flag().equalsIgnoreCase("2")) {
                    holder.binding.llSt.setVisibility(View.GONE);
                    holder.binding.llACDE.setVisibility(View.GONE);
                    holder.binding.tvCompleted.setVisibility(View.GONE);
                    holder.binding.tvRejected.setVisibility(View.VISIBLE);

                    holder.binding.tvTxt.setText(String.format("%s %s[%s]", context.getResources().getString(R.string.booking),
                            context.getResources().getString(R.string.rejected1), artistBookingsList.get(position).getId()));
                } else if (artistBookingsList.get(position).getBooking_flag().equalsIgnoreCase("4")) {
                    holder.binding.llSt.setVisibility(View.GONE);
                    holder.binding.llACDE.setVisibility(View.GONE);
                    holder.binding.tvCompleted.setVisibility(View.VISIBLE);
                    holder.binding.tvRejected.setVisibility(View.GONE);

                    holder.binding.tvTxt.setText(String.format("%s %s[%s]", context.getResources().getString(R.string.booking),
                            context.getResources().getString(R.string.completed1), artistBookingsList.get(position).getId()));
                }
            }

            ArtistBooking currentBooking = artistBookingsList.get(position);

//            if (currentBooking.getStatus().equals("1")) {
//                holder.binding.discussBtn.setVisibility(View.GONE);
//            } else {
                getChat(successful -> {
                    if (!successful) {
                        holder.binding.discussBtn.setVisibility(View.INVISIBLE);
                    }

                    holder.binding.discussBtn.setVisibility(View.VISIBLE);

                    holder.binding.discussBtn.setOnClickListener(v -> {
                        Intent in = new Intent(context, OneTwoOneChat.class);

                        try {
                            in.putExtra(Consts.CHAT_LIST_DTO, chatHistory.get(0));
                            showDisclaimerDialog(context, in);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                });
//            }

            holder.binding.llAccept.setOnClickListener(v -> {
                if (NetworkManager.isConnectToInternet(context)) {
                    booking("1", position, false);
                } else {
                    ProjectUtils.showToast(context, context.getResources().getString(R.string.internet_concation));
                }
            });
            holder.binding.llDecline.setOnClickListener(v -> ProjectUtils.showDialog(context, context.getResources().getString(R.string.dec_cpas), context.getResources().getString(R.string.decline_msg), (dialog, which) -> decline(position), (dialog, which) -> {

            }, false));
            holder.binding.llStart.setOnClickListener(v -> {
                if (NetworkManager.isConnectToInternet(context)) {
                    booking("2", position, true);
                } else {
                    ProjectUtils.showToast(context, context.getResources().getString(R.string.internet_concation));
                }
            });
            holder.binding.llCancel.setOnClickListener(v -> ProjectUtils.showDialog(context, context.getResources().getString(R.string.dec_cpas), context.getResources().getString(R.string.decline_msg), (dialog, which) -> decline(position), (dialog, which) -> {

            }, false));
        } else {
            MyViewHolderSection view = (MyViewHolderSection) holderMain;
            view.tvSection.setText(artistBookingsList.get(position).getSection_name());
        }
    }

    @Override
    public int getItemViewType(int position) {
        int VIEW_SECTION = 0;
        return this.artistBookingsList.get(position).isSection() ? VIEW_SECTION : VIEW_ITEM;
    }

    @Override
    public int getItemCount() {
        return artistBookingsList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        AdapterAllBookingsBinding binding;

        public MyViewHolder(AdapterAllBookingsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void booking(String req, int pos, boolean hasAcceptedJob) {
        if (!hasAcceptedJob) {
            Dialog dialog = new Dialog(context);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            final DialogEditPriceBindingImpl binding1 = DataBindingUtil.inflate(
                    LayoutInflater.from(context), R.layout.dialog_edit_price, null, false
            );
            dialog.setContentView(binding1.getRoot());

            dialog.show();

            dialog.setCancelable(true);

            // Set the default price here
            binding1.CETEditPrice.setText(artistBookingsList.get(pos).getPrice());

            binding1.proceedButton.setOnClickListener(v -> {
                if (!isValidField(binding1.CETEditPrice, context.getResources().getString(R.string.val_price))) {
                    return;
                }
                // Update price here
                paramsPricing = new HashMap<>();
                paramsPricing.put(Consts.BOOKING_ID, artistBookingsList.get(pos).getId());
                paramsPricing.put(Consts.PRICE, binding1.CETEditPrice.getText().toString());
                updateArtisanRate(successful -> {
                    if (successful) sendBookingRequest(req, pos);
                }, dialog);
            });
        } else {
            sendBookingRequest(req, pos);
        }
    }

    private void sendBookingRequest(String req, int pos) {
        HashMap<String, String> paramsBookingOp = new HashMap<>();
        paramsBookingOp.put(Consts.BOOKING_ID, artistBookingsList.get(pos).getId());
        paramsBookingOp.put(Consts.REQUEST, req);
        paramsBookingOp.put(Consts.USER_ID, artistBookingsList.get(pos).getUser_id());
        ProjectUtils.showProgressDialog(context, true, context.getResources().getString(R.string.please_wait));
        new HttpsRequest(Consts.BOOKING_OPERATION_API, paramsBookingOp, context)
                .stringPost(TAG, (flag, msg, response) -> {
                    ProjectUtils.pauseProgressDialog();
                    if (flag) {
                        ProjectUtils.showToast(context, msg);
                        newBookings.getBookings();
                    } else {
                        ProjectUtils.showToast(context, msg);
                    }
                });
    }

    public void decline(int pos) {
        HashMap<String, String> paramsDecline = new HashMap<>();
        paramsDecline.put(Consts.USER_ID, userDTO.getUser_id());
        paramsDecline.put(Consts.BOOKING_ID, artistBookingsList.get(pos).getId());
        paramsDecline.put(Consts.DECLINE_BY, "1");
        paramsDecline.put(Consts.DECLINE_REASON, "Busy");
        ProjectUtils.showProgressDialog(context, true, context.getResources().getString(R.string.please_wait));
        new HttpsRequest(Consts.DECLINE_BOOKING_API, paramsDecline, context).stringPost(TAG, (flag, msg, response) -> {
            ProjectUtils.pauseProgressDialog();
            if (flag) {
                ProjectUtils.showToast(context, msg);
                newBookings.getBookings();

            } else {
                ProjectUtils.showToast(context, msg);
            }
        });
    }

    private void updateArtisanRate(NetworkCallback callback, Dialog dialog) {
        dialog.dismiss();

        ProjectUtils.showProgressDialog(context, false, context.getResources().getString(R.string.please_wait));

        new HttpsRequest(Consts.UPDATE_ARTISAN_RATE, paramsPricing, context)
                .stringPost(TAG, (flag, msg, response) -> {
                    ProjectUtils.pauseProgressDialog();

                    if (flag) {
                        callback.onDone(true);
                    } else {
                        ProjectUtils.showToast(context, msg);
                        callback.onDone(false);
                    }
                });
    }

    public void getChat(NetworkCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put(Consts.ARTIST_ID, userDTO.getUser_id());

        ProjectUtils.showProgressDialog(context, true, context.getResources().getString(R.string.please_wait));

        new HttpsRequest(Consts.GET_CHAT_HISTORY_API, params, context).stringPost(TAG, (flag, msg, response) -> {
            ProjectUtils.pauseProgressDialog();
            if (flag) {
                try {
                    chatHistory = new ArrayList<>();
                    Type getpetDTO = new TypeToken<List<ChatListDTO>>() {
                    }.getType();
                    chatHistory = new Gson().fromJson(response.getJSONArray("my_chat").toString(), getpetDTO);

                    callback.onDone(!chatHistory.isEmpty());
                } catch (Exception e) {
                    callback.onDone(false);
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean isValidField(EditText editText, String msg) {
        if (!ProjectUtils.isEditTextFilled(editText)) {
            ProjectUtils.showToast(context, msg);
            return false;
        } else {
            return true;
        }
    }

    public static class MyViewHolderSection extends RecyclerView.ViewHolder {
        public CustomTextView tvSection;

        public MyViewHolderSection(View view) {
            super(view);
            tvSection = view.findViewById(R.id.tvSection);
        }
    }

    public interface NetworkCallback {
        void onDone(boolean successful);
    }
}
