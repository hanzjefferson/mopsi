package com.hanzjefferson.mopsi;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

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
    private SearchView searchView;

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
                    intent.putExtra("query", adapter.getSearchQuery());
                    setResult(RESULT_OK, intent);
                    super.finish();
                });
                binding.recyclerView.setAdapter(adapter);

                String query = getIntent().getStringExtra("query");
                if (query != null) {
                    adapter.setSearchQuery(query);
                    if (adapter.getItemModels().isEmpty()) {
                        binding.tvNoAnnouncement.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvNoAnnouncement.setVisibility(View.GONE);
                    }
                }
            } else {
                adapter.update(kelas);
            }
        }else{
            binding.tvNoAnnouncement.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_student_manager, menu);

        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null){
                    adapter.setSearchQuery(newText);
                    if (adapter.getItemModels().isEmpty()) {
                        binding.tvNoAnnouncement.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvNoAnnouncement.setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });
        searchView.setQueryHint("Cari sesuatu...");

        String query = getIntent().getStringExtra("query");
        if (query != null && !query.isEmpty()) {
            searchView.onActionViewExpanded();
            searchView.setQuery(query, false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void finish() {
        Animatoo.INSTANCE.animateSlideRight(this);
        Intent intent = new Intent();
        intent.putExtra("query", adapter.getSearchQuery());
        setResult(RESULT_CANCELED, intent);
        super.finish();
    }
}