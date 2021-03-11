package com.wokconns.wokconns.ui.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.FragmentJobsBinding;
import com.wokconns.wokconns.ui.activity.BaseActivity;

public class JobsFrag extends Fragment implements View.OnClickListener {
    private String TAG = JobsFrag.class.getSimpleName();
    private View view;
    private BaseActivity baseActivity;
    private AppliedJobsFrag appliedJobsFrag = new AppliedJobsFrag();
    private AllJobsFrag allJobsFrag = new AllJobsFrag();
    private FragmentManager fragmentManager;
    FragmentJobsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_jobs, container, false);
        view = binding.getRoot();
        baseActivity.headerNameTV.setText(getResources().getString(R.string.jobs_home));
        fragmentManager = getChildFragmentManager();

        binding.tvAllJobs.setOnClickListener(this);
        binding.tvAppliedJobs.setOnClickListener(this);
        binding.fabFilter.setOnClickListener(this);

        fragmentManager.beginTransaction().add(R.id.frame, allJobsFrag).commit();

        return view;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        baseActivity = (BaseActivity) activity;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvAllJobs:
                setSelected(true, false);
                fragmentManager.beginTransaction().replace(R.id.frame, allJobsFrag).commit();
                break;
            case R.id.tvAppliedJobs:
                setSelected(false, true);
                fragmentManager.beginTransaction().replace(R.id.frame, appliedJobsFrag).commit();
                break;
            case R.id.fab_filter:
                allJobsFrag.dialogAbout();
                break;
        }

    }

    public void setSelected(boolean firstBTN, boolean secondBTN) {
        if (firstBTN) {
            binding.tvAllJobs.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            binding.tvAllJobs.setTextColor(getResources().getColor(R.color.white));
            binding.tvAppliedJobs.setBackgroundColor(getResources().getColor(R.color.white));
            binding.tvAppliedJobs.setTextColor(getResources().getColor(R.color.gray));
        }
        if (secondBTN) {
            binding.tvAllJobs.setBackgroundColor(getResources().getColor(R.color.white));
            binding.tvAllJobs.setTextColor(getResources().getColor(R.color.gray));
            binding.tvAppliedJobs.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            binding.tvAppliedJobs.setTextColor(getResources().getColor(R.color.white));
        }
        binding.tvAllJobs.setSelected(firstBTN);
        binding.tvAppliedJobs.setSelected(secondBTN);
    }

}
