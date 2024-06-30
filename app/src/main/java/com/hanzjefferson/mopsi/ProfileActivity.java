package com.hanzjefferson.mopsi;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hanzjefferson.mopsi.databinding.ActivityProfileBinding;
import com.hanzjefferson.mopsi.databinding.SheetPemanggilanBinding;
import com.hanzjefferson.mopsi.models.Kelas;
import com.hanzjefferson.mopsi.models.Profile;
import com.hanzjefferson.mopsi.models.Response;
import com.hanzjefferson.mopsi.utils.AccountUtils;
import com.hanzjefferson.mopsi.utils.ApiServiceUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private Profile profile;
    private SheetPemanggilanBinding pemanggilanBinding;
    private BottomSheetDialog dialog;
    private boolean viewProfile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        pemanggilanBinding = SheetPemanggilanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.materialToolbar.setNavigationOnClickListener(v -> finish());

        dialog = new BottomSheetDialog(this);

        viewProfile = getIntent().getStringExtra("view_profile") != null;
        profile = viewProfile? new Gson().fromJson(getIntent().getStringExtra("view_profile"), Profile.class) : AccountUtils.getProfile();

        if (viewProfile) getSupportActionBar().setTitle("Profil "+profile.nick_name);

        binding.tvFullName.setText(profile.full_name);
        binding.tvNickName.setText(profile.nick_name);

        String[] roles = new String[]{"Siswa", "Petugas Rekap", "Wali Murid", "Wali Kelas", "Tim Tata Tertib"};
        binding.tvDisplayRole.setText(roles[profile.role_id-1]);

        if (!(profile.nick_name == null || profile.nick_name.isEmpty())) binding.tvAddress.setText(profile.nick_name);

        binding.tvGender.setText(profile.gender.equals("l") ? "Laki-laki" : "Perempuan");

        if (!(profile.address == null || profile.address.isEmpty())) binding.tvAddress.setText(profile.address);

        if (profile.birth > 0) binding.tvBirth.setText(new SimpleDateFormat("dd MMMM yyyy").format(new Date(profile.birth*1000)));

        if (!(profile.phone_num == null || profile.phone_num.isEmpty())) binding.tvTelephone.setText(profile.phone_num);

        if (profile.others != null){
            binding.layoutOthers.setVisibility(View.VISIBLE);
            switch (profile.role_id){
                case 1:
                case 2:
                    Profile parentProfile = new Gson().fromJson(new Gson().toJson(profile.others), Profile.class);
                    binding.layoutParent.setVisibility(View.VISIBLE);
                    binding.layoutParent.setOnClickListener(v->{
                        Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
                        intent.putExtra("view_profile", new Gson().toJson(parentProfile));
                        startActivity(intent);
                    });
                    binding.tvParent.setText(parentProfile.full_name);
                    break;
                case 3:

                    break;
                case 4:
                    Kelas kelas = new Gson().fromJson(new Gson().toJson(profile.others), Kelas.class);
                    binding.layoutClass.setVisibility(View.VISIBLE);
                    binding.tvClass.setText("Kelas "+kelas.name);
                    break;
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm");
        AtomicReference<Long> date = new AtomicReference<>(new Date().getTime());

        pemanggilanBinding.inputTanggal.setText(dateFormat.format(new Date()));
        pemanggilanBinding.inputTanggal.setOnClickListener(inp -> {;
            MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setCalendarConstraints(new CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now()).build())
                    .setSelection(date.get())
                    .build();
            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                date.set(selection-7*3600000);
                pemanggilanBinding.inputTanggal.setText(dateFormat.format(new Date(date.get())));
                MaterialTimePicker materialTimePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                        .build();
                materialTimePicker.addOnPositiveButtonClickListener(v->{
                    long hour = materialTimePicker.getHour();
                    long minute = materialTimePicker.getMinute();
                    long selectedDate = date.get();

                    long updatedDate = selectedDate + hour * 3600000 + minute * 60000;
                    date.set(updatedDate);

                    pemanggilanBinding.inputTanggal.setText(dateFormat.format(new Date(date.get())));
                });
                materialTimePicker.show(getSupportFragmentManager(), "time_picker");
            });
            materialDatePicker.show(getSupportFragmentManager(), "date_picker");
        });

        String[] pihak = new String[]{"Siswa dan wali", "Siswa saja", "Wali saja"};
        pemanggilanBinding.inputPihak.setText(pihak[0]);
        pemanggilanBinding.inputPihak.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, pihak));

        pemanggilanBinding.inputTempat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (pemanggilanBinding.inputPerihal.getText().toString().isEmpty() || s.toString().isEmpty()) pemanggilanBinding.buttonSend.setEnabled(false);
                else pemanggilanBinding.buttonSend.setEnabled(true);
            }
        });

        pemanggilanBinding.inputPerihal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (pemanggilanBinding.inputTempat.getText().toString().isEmpty() || s.toString().isEmpty()) pemanggilanBinding.buttonSend.setEnabled(false);
                else pemanggilanBinding.buttonSend.setEnabled(true);
            }
        });

        pemanggilanBinding.buttonSend.setOnClickListener(v->{
            dialog.cancel();
            binding.layoutProfile.setVisibility(View.GONE);
            binding.spinKit.setVisibility(View.VISIBLE);

            String pihakStr = pemanggilanBinding.inputPihak.getText().toString();
            int[] ids = new int[0];
            if (pihakStr.equals(pihak[0])){
                ids = new int[]{profile.id, new Gson().fromJson(new Gson().toJson(profile.others), Profile.class).id};
            }else if (pihakStr.equals(pihak[1])){
                ids = new int[]{profile.id};
            } else {
                ids = new int[]{new Gson().fromJson(new Gson().toJson(profile.others), Profile.class).id};
            }

            ApiServiceUtils.call(new ApiServiceUtils.CallbackListener<JsonObject>() {
                @Override
                public void onResponse(Response<JsonObject> response) {
                    binding.layoutProfile.setVisibility(View.VISIBLE);
                    binding.spinKit.setVisibility(View.GONE);
                    new MaterialAlertDialogBuilder(ProfileActivity.this)
                            .setTitle(response.isSuccess()? "Pemanggilan Berhasil":"Gagal")
                            .setMessage((response.isSuccess()? "":"Gagal memanggil: ")+response.message)
                            .setPositiveButton("OK", null)
                            .show();
                }

                @Override
                public void onError(VolleyError error) {
                    binding.layoutProfile.setVisibility(View.VISIBLE);
                    binding.spinKit.setVisibility(View.GONE);
                    new MaterialAlertDialogBuilder(ProfileActivity.this)
                            .setTitle("Kesalahan Klien")
                            .setMessage(error.getMessage())
                            .setPositiveButton("OK", null)
                            .show();
                }
            }, ids, date.get(), pemanggilanBinding.inputTempat.getText().toString(), pemanggilanBinding.inputPerihal.getText().toString());
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        if (viewProfile) {
            if ((profile.role_id != 1 && profile.role_id !=2) || (AccountUtils.getProfile().role_id == 1 || AccountUtils.getProfile().role_id == 2 || AccountUtils.getProfile().role_id == 3)) menu.findItem(R.id.menu_panggilan).setVisible(false);
            menu.findItem(R.id.menu_logout).setVisible(false);
        } else menu.findItem(R.id.menu_panggilan).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_panggilan){
            dialog.setContentView(pemanggilanBinding.getRoot());
            dialog.show();
        } else if (item.getItemId() == R.id.menu_logout){
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