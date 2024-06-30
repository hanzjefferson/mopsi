package com.hanzjefferson.mopsi;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.widget.NestedScrollView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.VolleyError;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.hanzjefferson.mopsi.adapters.AnnouncementSlideAdapter;
import com.hanzjefferson.mopsi.adapters.RekapitulasiPageAdapter;
import com.hanzjefferson.mopsi.databinding.ActivityMainBinding;
import com.hanzjefferson.mopsi.models.Announcement;
import com.hanzjefferson.mopsi.models.Rekapitulasi;
import com.hanzjefferson.mopsi.models.Response;
import com.hanzjefferson.mopsi.utils.AccountUtils;
import com.hanzjefferson.mopsi.utils.ApiServiceUtils;
import com.hanzjefferson.mopsi.utils.SocketUtils;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity{
    private ActivityMainBinding binding;

    private AnnouncementSlideAdapter announcementSliderAdapter;
    private RekapitulasiPageAdapter rekapitulasiPageAdapter;

    private String[] tabTitles = new String[]{"Kehadiran", "Poin"};
    private boolean collapsed = false;

    private SearchView searchView;
    private int vpRekapitulasiPosition = 0;
    private int rekapId = -1;
    private String profile = "";
    private String searchQuery = "";
    private String searchQueryStudent = "";
    private ActivityResultLauncher studentManagerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (o.getResultCode() == RESULT_OK){
                rekapId = o.getData().getIntExtra("id", -1);
                searchQueryStudent = o.getData().getStringExtra("query");

                fetchRekapitulasi();
            }else if (o.getResultCode() == RESULT_CANCELED){
                searchQueryStudent = o.getData().getStringExtra("query");
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        primaryStatusBar(true);

        int jam = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String waktu = "Malam";
        if (jam >= 0 && jam < 12) {
            waktu = "Pagi";
        } else if (jam >= 12 && jam < 15) {
            waktu = "Siang";
        } else if (jam >= 15 && jam < 18) {
            waktu = "Sore";
        }
        binding.heading.tvGreeting.setText("Selamat " + waktu + ",");
        binding.heading.tvName.setText(AccountUtils.getProfile().full_name);

        binding.heading.imgProfile.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        binding.heading.tvSeeAnnouncement.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AnnouncementsActivity.class));
        });

        binding.heading.btnRetry.setOnClickListener(v -> fetchPengumuman());
        binding.content.btnRetry.setOnClickListener(v -> fetchRekapitulasi());

        rekapitulasiPageAdapter = new RekapitulasiPageAdapter(this);
        binding.content.vpRekapitulasi.setAdapter(rekapitulasiPageAdapter);
        binding.content.vpRekapitulasi.setOffscreenPageLimit(2);
        binding.content.vpRekapitulasi.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                vpRekapitulasiPosition = position;
                rekapitulasiPageAdapter.getFragment(vpRekapitulasiPosition).onItemSearch(searchQuery);
            }
        });

        new TabLayoutMediator(binding.tabLayout, binding.content.vpRekapitulasi, (tab, position) -> {
            tab.setText(tabTitles[position]);
        }).attach();

        binding.appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            View fragmentView = rekapitulasiPageAdapter.getFragment(vpRekapitulasiPosition).getView();
            NestedScrollView nestedScrollView = fragmentView != null? ((NestedScrollView) fragmentView.findViewById(R.id.nested_scroll_view)):null;
            if (verticalOffset == 0){
                collapsed = false;
                if (nestedScrollView != null) nestedScrollView.setNestedScrollingEnabled(true);
                binding.content.vpRekapitulasi.setEnabled(false);
                primaryStatusBar(true);
            }else if (Math.abs(verticalOffset) - appBarLayout.getTotalScrollRange() == 0) {
                collapsed = true;
                if (nestedScrollView != null) nestedScrollView.setNestedScrollingEnabled(false);
                binding.content.vpRekapitulasi.setEnabled(true);
                primaryStatusBar(false);
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (collapsed){
                    binding.appBar.setExpanded(true);
                }else{
                    finish();
                }
            }
        });

        fetchPengumuman();
    }

    private void fetchPengumuman(){
        binding.heading.spinKitView.setVisibility(View.VISIBLE);
        binding.heading.vpAnnouncementSlide.setVisibility(View.GONE);
        binding.heading.layoutAnnouncement.setVisibility(View.GONE);

        ApiServiceUtils.getAnnouncement(new ApiServiceUtils.CallbackListener<Announcement[]>() {

            @Override
            public void onResponse(Response<Announcement[]> response) {
                fetchRekapitulasi();
                binding.heading.spinKitView.setVisibility(View.GONE);

                SocketUtils.on("announcements", Announcement[].class, (Announcement[] data)-> {
                    runOnUiThread(() -> handlePengumuman(data));
                });
                if (response.isSuccess()){
                    handlePengumuman(response.data);
                }else {
                    binding.heading.layoutAnnouncement.setVisibility(View.VISIBLE);

                    binding.heading.tvInfo.setText(response.message);
                }
            }

            @Override
            public void onError(VolleyError error) {
                binding.heading.spinKitView.setVisibility(View.GONE);
                binding.heading.layoutAnnouncement.setVisibility(View.VISIBLE);

                binding.heading.tvInfo.setText("Kesalahan Klien!");
            }
        });
    }

    private void fetchRekapitulasi() {
        binding.tabLayout.setVisibility(View.GONE);
        binding.content.spinKitView.setVisibility(View.VISIBLE);
        binding.content.vpRekapitulasi.setVisibility(View.GONE);
        binding.content.layoutInfo.setVisibility(View.GONE);

        ApiServiceUtils.getRekap(rekapId, new ApiServiceUtils.CallbackListener<Rekapitulasi>() {
            @Override
            public void onResponse(Response<Rekapitulasi> response) {
                binding.content.spinKitView.setVisibility(View.GONE);

                if (response.isSuccess()){
                    binding.tabLayout.setVisibility(View.VISIBLE);

                    rekapId = response.data.id;
                    profile = new Gson().toJson(response.data);
                    handleRekapitulasi(response.data);
                } else {
                    binding.content.layoutInfo.setVisibility(View.VISIBLE);
                    binding.content.vpRekapitulasi.setVisibility(View.GONE);

                    binding.content.tvInfo.setText(response.message);
                    binding.content.btnRetry.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(VolleyError error) {
                binding.content.spinKitView.setVisibility(View.GONE);
                binding.content.layoutInfo.setVisibility(View.VISIBLE);
                binding.content.vpRekapitulasi.setVisibility(View.GONE);

                binding.content.tvInfo.setText("Kesalahan Klien!");
                binding.content.btnRetry.setVisibility(View.VISIBLE);
            }
        });
    }

    private void handlePengumuman(Announcement[] announcements){
        if (announcements.length > 0){
            binding.heading.vpAnnouncementSlide.setVisibility(View.VISIBLE);
            binding.heading.layoutAnnouncement.setVisibility(View.GONE);

            announcementSliderAdapter = new AnnouncementSlideAdapter(MainActivity.this, announcements);
            binding.heading.vpAnnouncementSlide.setAdapter(announcementSliderAdapter);
            binding.heading.indicatorAnnouncementSlide.setViewPager(binding.heading.vpAnnouncementSlide);
        } else {
            binding.heading.vpAnnouncementSlide.setVisibility(View.GONE);
            binding.heading.layoutAnnouncement.setVisibility(View.VISIBLE);

            binding.heading.tvInfo.setText("Tidak ada pengumuman.");
            binding.heading.btnRetry.setVisibility(View.GONE);
        }
    }

    private void handleRekapitulasi(Rekapitulasi rekapitulasi){
        binding.content.vpRekapitulasi.setVisibility(View.VISIBLE);
        binding.content.layoutInfo.setVisibility(View.GONE);

        setSupportActionBar(binding.materialToolbar);
        if (AccountUtils.getProfile().role_id != 1) getSupportActionBar().setSubtitle(rekapitulasi.full_name);
        rekapitulasiPageAdapter.setData(rekapitulasi);
    }

    private int getColorAttr(@AttrRes int resId) {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(resId, typedValue, true);
        return typedValue.data;
    }

    private void primaryStatusBar(boolean state){
        if (state){
            getWindow().getDecorView().setSystemUiVisibility(0);
            getWindow().setStatusBarColor(getColorAttr(com.google.android.material.R.attr.colorPrimary));
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(getColorAttr(com.google.android.material.R.attr.colorSurface));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rekapitulasi, menu);

        if (AccountUtils.getProfile().role_id == AccountUtils.ROLE_SISWA) {
            menu.findItem(R.id.menu_switch).setVisible(false);
            menu.findItem(R.id.menu_view_profile).setVisible(false);
        }

        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText;
                return rekapitulasiPageAdapter.getFragment(vpRekapitulasiPosition).onItemSearch(newText);
            }
        });
        searchView.setQueryHint("Cari sesuatu...");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_switch){
            Intent i = new Intent(this, StudentManagerActivity.class);
            i.putExtra("query", searchQueryStudent);
            studentManagerLauncher.launch(i);
        } else if (item.getItemId() == R.id.menu_view_profile) {
            Intent i = new Intent(this, ProfileActivity.class);
            i.putExtra("view_profile", profile);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        Animatoo.INSTANCE.animateSlideLeft(this);
    }
}