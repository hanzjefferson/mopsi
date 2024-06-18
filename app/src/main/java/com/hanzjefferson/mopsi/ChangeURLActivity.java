package com.hanzjefferson.mopsi;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.hanzjefferson.mopsi.databinding.ActivityChangeUrlBinding;
import com.hanzjefferson.mopsi.utils.ApiServiceUtils;

public class ChangeURLActivity extends AppCompatActivity {
    private ActivityChangeUrlBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangeUrlBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnEnter.setOnClickListener(v -> {
            String url = binding.edUrl.getText().toString();
            if (url.isEmpty()) {
                binding.edUrl.setError("URL cannot be empty");
            } else {
                binding.edUrl.setError(null);
                ApiServiceUtils.BASE_URL = url;
                startActivity(new Intent(this, SplashActivity.class));
                finish();
            }
        });
    }
}