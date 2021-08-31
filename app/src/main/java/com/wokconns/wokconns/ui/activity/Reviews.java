package com.wokconns.wokconns.ui.activity;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.View;

import com.wokconns.wokconns.dto.ArtistDetailsDTO;
import com.wokconns.wokconns.dto.ReviewsDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.ActivityReviewsBinding;
import com.wokconns.wokconns.interfacess.Const;
import com.wokconns.wokconns.ui.adapter.ReviewAdapter;

import java.util.ArrayList;

public class Reviews extends AppCompatActivity implements View.OnClickListener {
    ActivityReviewsBinding binding;
    private Context context;
    private ArtistDetailsDTO artistDetailsDTO;
    private ReviewAdapter reviewAdapter;
    private LinearLayoutManager mLayoutManagerReview;
    private ArrayList<ReviewsDTO> reviewsDTOList;
    private Bundle bundle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reviews);
        context = Reviews.this;
        bundle = getIntent().getExtras();
        artistDetailsDTO = (ArtistDetailsDTO) bundle.getSerializable(Const.ARTIST_DTO);
        showUiAction();

    }

    public void showUiAction() {
        binding.llBack.setOnClickListener(this);
        mLayoutManagerReview = new LinearLayoutManager(context);
        binding.rvReviews.setLayoutManager(mLayoutManagerReview);
        showData();
    }


    public void showData() {
        reviewsDTOList = new ArrayList<>();
        reviewsDTOList = artistDetailsDTO.getReviews();

        if (reviewsDTOList.size() > 0) {
            binding.tvNotFound.setVisibility(View.GONE);
            binding.llList.setVisibility(View.VISIBLE);
            reviewAdapter = new ReviewAdapter(context, reviewsDTOList);
            binding.rvReviews.setAdapter(reviewAdapter);
            binding.tvReviewsText.setText(getString(R.string.reviews) + reviewsDTOList.size() + ")");
        } else {
            binding.tvNotFound.setVisibility(View.VISIBLE);
            binding.llList.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.llBack:
                finish();
                break;
        }
    }
}
