package com.hanzjefferson.mopsi;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.android.volley.VolleyError;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hanzjefferson.mopsi.adapters.AnnouncementAdapter;
import com.hanzjefferson.mopsi.databinding.ActivityAnnouncementsBinding;
import com.hanzjefferson.mopsi.models.Announcement;
import com.hanzjefferson.mopsi.models.Response;
import com.hanzjefferson.mopsi.utils.ApiServiceUtils;
import com.hanzjefferson.mopsi.utils.SocketUtils;

public class AnnouncementsActivity extends AppCompatActivity {
    private ActivityAnnouncementsBinding binding;
    private AnnouncementAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnnouncementsBinding.inflate(getLayoutInflater());
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

        ApiServiceUtils.getAnnouncement(new ApiServiceUtils.CallbackListener<Announcement[]>() {

            @Override
            public void onResponse(Response<Announcement[]> response) {
                if (response.isSuccess()) {
                    binding.spinKit.setVisibility(View.GONE);
                    SocketUtils.on("announcements", Announcement[].class, (Announcement[] data)-> {
                        runOnUiThread(() -> handleData(data));
                    });
                    handleData(response.data);
                } else new MaterialAlertDialogBuilder(AnnouncementsActivity.this)
                        .setTitle("Terjadi kesalahan!")
                        .setMessage(response.message)
                        .setPositiveButton("Coba lagi", (d, w) -> fetch())
                        .setNegativeButton("Kembali", (d, w) -> finish())
                        .show();
            }

            @Override
            public void onError(VolleyError error) {
                new MaterialAlertDialogBuilder(AnnouncementsActivity.this)
                        .setTitle("Terjadi kesalahan!")
                        .setMessage(error.getMessage())
                        .setPositiveButton("Coba lagi", (d, w) -> fetch())
                        .setNegativeButton("Kembali", (d, w) -> finish())
                        .show();
            }
        });
    }

    private void handleData(Announcement[] announcements) {
        if (announcements.length > 0){
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.tvNoAnnouncement.setVisibility(View.GONE);
            if (adapter == null) {
                adapter = new AnnouncementAdapter(AnnouncementsActivity.this, announcements);
                binding.recyclerView.setAdapter(adapter);
            } else {
                adapter.update(announcements);
            }
        }else {
            binding.recyclerView.setVisibility(View.GONE);
            binding.tvNoAnnouncement.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void finish() {
        Animatoo.INSTANCE.animateSlideRight(this);
        super.finish();
    }
}