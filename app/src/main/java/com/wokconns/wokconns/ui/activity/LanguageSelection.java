package com.wokconns.wokconns.ui.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.wokconns.wokconns.dto.LanguageDTO;
import com.wokconns.wokconns.dto.UserDTO;
import com.wokconns.wokconns.R;
import com.wokconns.wokconns.databinding.ActivityLanguageSelectionBinding;
import com.wokconns.wokconns.interfacess.Consts;
import com.wokconns.wokconns.preferences.SharedPrefrence;
import com.wokconns.wokconns.ui.adapter.AdapterLanguage;

import java.util.ArrayList;
import java.util.Locale;

public class LanguageSelection extends AppCompatActivity implements View.OnClickListener {
    private ActivityLanguageSelectionBinding binding;
    private static final String TAG = LanguageSelection.class.getSimpleName();
    private SharedPrefrence sharedPrefrence;
    private Context mContext;
    private AdapterLanguage adapterLanguage;
    private ArrayList<LanguageDTO> languageDTOList;
    public static int flag = 0;
    private UserDTO userDTO;
    private LinearLayoutManager layoutManager;
    String type ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_language_selection);
        mContext = LanguageSelection.this;
        sharedPrefrence = SharedPrefrence.getInstance(mContext);
        userDTO = sharedPrefrence.getParentUser(Consts.USER_DTO);
        init();
    }

    private void init() {
        binding.llBack.setOnClickListener(this);
        type = getIntent().getStringExtra(Consts.TYPE);
        showLanguage();
    }

    private void showLanguage() {
        languageDTOList = new ArrayList<>();

        languageDTOList.add(new LanguageDTO(getString(R.string.english), "en"));
        languageDTOList.add(new LanguageDTO(getString(R.string.arabic), "ar"));

        GridLayoutManager gLayout = new GridLayoutManager(mContext, 2);
        adapterLanguage = new AdapterLanguage(languageDTOList, LanguageSelection.this, type);
        binding.rvLanguage.setLayoutManager(gLayout);
        binding.rvLanguage.setHasFixedSize(true);

        binding.rvLanguage.setAdapter(adapterLanguage);
    }

    public void language(String language) {
        String languageToLoad = language;

        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.locale = locale;

        mContext.getResources().updateConfiguration(config,
                mContext.getResources().getDisplayMetrics());
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        finish();
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