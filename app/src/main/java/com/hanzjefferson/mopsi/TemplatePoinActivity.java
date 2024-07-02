package com.hanzjefferson.mopsi;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.android.volley.VolleyError;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.hanzjefferson.mopsi.adapters.AnnouncementAdapter;
import com.hanzjefferson.mopsi.adapters.TemplatePoinAdapter;
import com.hanzjefferson.mopsi.databinding.ActivityAnnouncementsBinding;
import com.hanzjefferson.mopsi.databinding.ActivityTemplatePoinBinding;
import com.hanzjefferson.mopsi.models.Announcement;
import com.hanzjefferson.mopsi.models.Poin;
import com.hanzjefferson.mopsi.models.Response;
import com.hanzjefferson.mopsi.utils.ApiServiceUtils;
import com.hanzjefferson.mopsi.utils.SocketUtils;

public class TemplatePoinActivity extends AppCompatActivity {
    private ActivityTemplatePoinBinding binding;
    private TemplatePoinAdapter adapter;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTemplatePoinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.materialToolbar.setNavigationOnClickListener(v -> finish());

        binding.recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        fetch();
    }

    private void fetch() {
        binding.recyclerView.setVisibility(View.GONE);
        binding.spinKit.setVisibility(View.VISIBLE);

        ApiServiceUtils.templatePoin(new ApiServiceUtils.CallbackListener<Poin[]>() {

            @Override
            public void onResponse(Response<Poin[]> response) {
                if (response.isSuccess()) {
                    binding.spinKit.setVisibility(View.GONE);
                    handleData(response.data);
                } else new MaterialAlertDialogBuilder(TemplatePoinActivity.this)
                        .setTitle("Terjadi kesalahan!")
                        .setMessage(response.message)
                        .setPositiveButton("Coba lagi", (d, w) -> fetch())
                        .setNegativeButton("Kembali", (d, w) -> finish())
                        .show();
            }

            @Override
            public void onError(VolleyError error) {
                new MaterialAlertDialogBuilder(TemplatePoinActivity.this)
                        .setTitle("Terjadi kesalahan!")
                        .setMessage(error.getMessage())
                        .setPositiveButton("Coba lagi", (d, w) -> fetch())
                        .setNegativeButton("Kembali", (d, w) -> finish())
                        .show();
            }
        });
    }

    private void handleData(Poin[] poins) {
        if (poins.length > 0){
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.tvNoItem.setVisibility(View.GONE);
            if (adapter == null) {
                adapter = new TemplatePoinAdapter(TemplatePoinActivity.this, poins);
                adapter.setOnItemClickListener((v, m, i)->{
                    Intent intent = new Intent();
                    intent.putExtra("template", new Gson().toJson(m));
                    setResult(RESULT_OK, intent);
                    finish();
                });
                binding.recyclerView.setAdapter(adapter);
            } else {
                adapter.update(poins);
            }
        }else {
            binding.recyclerView.setVisibility(View.GONE);
            binding.tvNoItem.setVisibility(View.VISIBLE);
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
                        binding.tvNoItem.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvNoItem.setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });
        searchView.setQueryHint("Cari sesuatu...");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void finish() {
        Animatoo.INSTANCE.animateSlideRight(this);
        super.finish();
    }
}