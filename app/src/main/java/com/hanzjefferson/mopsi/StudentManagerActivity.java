package com.hanzjefferson.mopsi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hanzjefferson.mopsi.adapters.KelasAdapter;
import com.hanzjefferson.mopsi.databinding.ActivityStudentManagerBinding;
import com.hanzjefferson.mopsi.models.Kelas;
import com.hanzjefferson.mopsi.models.Response;
import com.hanzjefferson.mopsi.utils.ApiServiceUtils;

public class StudentManagerActivity extends AppCompatActivity {
    private ActivityStudentManagerBinding binding;
    private KelasAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentManagerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.materialToolbar.setNavigationOnClickListener(v -> finish());

        fetch();
    }

    private void fetch() {
        binding.recyclerView.setVisibility(View.GONE);
        binding.spinKit.setVisibility(View.VISIBLE);

        ApiServiceUtils.getClasses(new ApiServiceUtils.CallbackListener<Kelas[]>() {

            @Override
            public void onResponse(Response<Kelas[]> response) {
                if (response.isSuccess()) {
                    binding.spinKit.setVisibility(View.GONE);
                    handleData(response.data);
                } else new MaterialAlertDialogBuilder(StudentManagerActivity.this)
                        .setTitle("Terjadi kesalahan!")
                        .setMessage(response.message)
                        .setPositiveButton("Coba lagi", (d, w) -> fetch())
                        .setNegativeButton("Kembali", (d, w) -> finish())
                        .show();
            }

            @Override
            public void onError(VolleyError error) {
                new MaterialAlertDialogBuilder(StudentManagerActivity.this)
                        .setTitle("Terjadi kesalahan!")
                        .setMessage(error.getMessage())
                        .setPositiveButton("Coba lagi", (d, w) -> fetch())
                        .setNegativeButton("Kembali", (d, w) -> finish())
                        .show();
            }
        });
    }

    private void handleData(Kelas[] kelas){
        if (kelas.length > 0){
            binding.recyclerView.setVisibility(View.VISIBLE);
            if (adapter == null) {
                adapter = new KelasAdapter(StudentManagerActivity.this, kelas);
                adapter.setStudentOnItemClickListener((v, m, i) -> {
                    Intent intent = new Intent();
                    intent.putExtra("id", m.account_id);
                    setResult(RESULT_OK, intent);
                    finish();
                });
                binding.recyclerView.setAdapter(adapter);
            } else {
                adapter.update(kelas);
            }
        }else{
            binding.tvNoAnnouncement.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void finish() {
        Animatoo.INSTANCE.animateSlideRight(this);
        super.finish();
    }
}