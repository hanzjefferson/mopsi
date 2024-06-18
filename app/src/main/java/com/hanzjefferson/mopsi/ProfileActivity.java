package com.hanzjefferson.mopsi;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonObject;
import com.hanzjefferson.mopsi.databinding.ActivityProfileBinding;
import com.hanzjefferson.mopsi.models.Profile;
import com.hanzjefferson.mopsi.models.Response;
import com.hanzjefferson.mopsi.utils.AccountUtils;
import com.hanzjefferson.mopsi.utils.ApiServiceUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.materialToolbar.setNavigationOnClickListener(v -> finish());

        Profile profile = AccountUtils.getProfile();

        binding.tvFullName.setText(profile.full_name);

        if (profile.phone_num == null || profile.phone_num.isEmpty()) {
            binding.tvDisplayTelp.setVisibility(View.GONE);
        } else {
            binding.tvDisplayTelp.setText(profile.phone_num);
        }

        if (!(profile.nick_name == null || profile.nick_name.isEmpty())) binding.tvAddress.setText(profile.nick_name);

        binding.tvGender.setText(profile.gender.equals("l") ? "Laki-laki" : "Perempuan");

        if (!(profile.address == null || profile.address.isEmpty())) binding.tvAddress.setText(profile.address);

        binding.tvBirth.setText(new SimpleDateFormat("dd MMMM yyyy").format(new Date(profile.birth)));

        if (!(profile.phone_num == null || profile.phone_num.isEmpty())) binding.tvTelephone.setText(profile.phone_num);

        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_logout){
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Konfirmasi")
                    .setMessage("Apakah anda yakin ingin keluar dari akun?")
                    .setPositiveButton("Ya", (d, w)->{
                        ApiServiceUtils.logout(new ApiServiceUtils.CallbackListener<JsonObject>() {
                            @Override
                            public void onResponse(Response<JsonObject> response) {
                                if (response.isSuccess()){
                                    AccountUtils.clearState();
                                    startActivity(new Intent(ProfileActivity.this, SplashActivity.class));
                                    finish();
                                } else new MaterialAlertDialogBuilder(ProfileActivity.this)
                                        .setTitle("Terjadi Kesalahan!")
                                        .setMessage(response.message)
                                        .setPositiveButton("OK", null)
                                        .show();
                            }

                            @Override
                            public void onError(VolleyError error) {
                                new MaterialAlertDialogBuilder(ProfileActivity.this)
                                        .setTitle("Kesalahan Klien")
                                        .setMessage(error.getMessage())
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        });
                    })
                    .setNegativeButton("Tidak", null)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }
}