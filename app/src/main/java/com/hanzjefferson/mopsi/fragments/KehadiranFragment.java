package com.hanzjefferson.mopsi.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.AttrRes;

import com.hanzjefferson.mopsi.R;
import com.hanzjefferson.mopsi.adapters.KehadiranAdapter;
import com.hanzjefferson.mopsi.databinding.FragmentKehadiranBinding;
import com.hanzjefferson.mopsi.models.Kehadiran;
import com.hanzjefferson.mopsi.models.Rekapitulasi;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class KehadiranFragment extends RekapitulasiFragment {
    private static final String[] WEEK_NAMES = new String[]{
            "MIN", "SEN", "SEL", "RAB", "KAM", "JUM", "SAB"
    };
    private static final float TRANSLATION_OFFSET = 150f;

    private FragmentKehadiranBinding binding;
    private Map<String, Map<String, Kehadiran[]>> data = new HashMap<>();
    private Calendar calendar = Calendar.getInstance();
    private String jenisHadir = Kehadiran.KBM;
    private KehadiranAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentKehadiranBinding.inflate(inflater, container, false);

        binding.getRoot().setEnabled(false);

        MonthYearPickerDialog dialog = new MonthYearPickerDialog();
        dialog.setListener((view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            renderDate();
        });

        binding.tvDate.setOnClickListener(v -> {
            dialog.show(getParentFragmentManager(), "MonthYearPickerDialog");
        });
        binding.imgChangeLeft.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            binding.gridDate.animate()
                    .translationX(TRANSLATION_OFFSET)
                    .alpha(0)
                    .setDuration(200)
                    .withEndAction(()-> {
                        renderDate();
                        binding.gridDate.setTranslationX(-TRANSLATION_OFFSET);
                        binding.gridDate.animate()
                                .translationX(0)
                                .alpha(1)
                                .setDuration(200);
                    });
        });
        binding.imgChangeRight.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            binding.gridDate.animate()
                    .translationX(-TRANSLATION_OFFSET)
                    .alpha(0)
                    .setDuration(200)
                    .withEndAction(()->{
                        renderDate();
                        binding.gridDate.setTranslationX(TRANSLATION_OFFSET);
                        binding.gridDate.animate()
                                .translationX(0)
                                .alpha(1)
                                .setDuration(200);
                    });
        });
        binding.chipGroup.setOnCheckedStateChangeListener((chipGroup, list) -> {
                if (chipGroup.getCheckedChipId() == R.id.chip_kbm) {
                    jenisHadir = Kehadiran.KBM;
                }else if (chipGroup.getCheckedChipId() == R.id.chip_sholat) {
                    jenisHadir = Kehadiran.SHOLAT;
                }else if (chipGroup.getCheckedChipId() == R.id.chip_extra) {
                    jenisHadir = Kehadiran.EKSTRA;
                }
                renderDate();
        });

        renderDate();
        return binding.getRoot();
    }

    @Override
    public void onReceiveData(Rekapitulasi rekapitulasi) {
        this.data = rekapitulasi.kehadiran;
        if (binding == null) return;
        renderDate();
    }

    private void renderDate() {
        String tanggal = new SimpleDateFormat("YYYY-MM").format(calendar.getTime());
        Kehadiran[] kehadiranArr;
        if (data.containsKey(tanggal)) {
            kehadiranArr = data.get(tanggal).get(jenisHadir);
        } else {
            kehadiranArr = new Kehadiran[0];
        }

        binding.tvDate.setText(calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + ", " + calendar.get(Calendar.YEAR));
        binding.gridDate.removeAllViews();
        for (String weekName : WEEK_NAMES) {
            TextView textView = dateTextView(weekName);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setTextColor(getColorAttr(com.google.android.material.R.attr.colorPrimary));
            binding.gridDate.addView(textView);
        }
        int firstDay = (calendar.getActualMinimum(Calendar.DAY_OF_WEEK)+5)%7;
        Calendar lastMonthCalender = Calendar.getInstance();
        lastMonthCalender.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        lastMonthCalender.set(Calendar.MONTH, calendar.get(Calendar.MONTH)-1);

        int lastMonthMaxDay = lastMonthCalender.getActualMaximum(Calendar.DAY_OF_MONTH);
        int monthMaxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int nextMonthMinDay = 6*7-(firstDay + monthMaxDay);
        int totalDay = firstDay + monthMaxDay + nextMonthMinDay;

        for (int i = 1; i <= totalDay; i++) {
            TextView textView;
            if (i <= firstDay) {
                textView = dateTextView(String.format("%02d", (lastMonthMaxDay-firstDay)+i));
                textView.setAlpha(0.5f);
            } else if (i <= firstDay+monthMaxDay){
                textView = dateTextView(String.format("%02d", i-firstDay));
                textView.setTextColor(getColorAttr(com.google.android.material.R.attr.colorOnSurface));
            } else {
                textView = dateTextView(String.format("%02d", i-monthMaxDay-firstDay));
                textView.setAlpha(0.5f);
            }

            try {
                Kehadiran kehadiran = kehadiranArr[i-firstDay-  1];
                textView.setTextColor(getContext().getColor(android.R.color.white));
                textView.setTypeface(Typeface.DEFAULT_BOLD);
                if (kehadiran.status.equals(Kehadiran.STATUS_HADIR)) {
                    textView.setBackgroundColor(getContext().getColor(android.R.color.holo_green_dark));
                } else if (kehadiran.status.equals(Kehadiran.STATUS_SAKIT) || kehadiran.status.equals(Kehadiran.STATUS_IZIN)) {
                    textView.setBackgroundColor(getContext().getColor(android.R.color.holo_orange_dark));
                } else if (kehadiran.status.equals(Kehadiran.STATUS_ALPHA)) {
                    textView.setBackgroundColor(getContext().getColor(android.R.color.holo_red_dark));
                }
            } catch (IndexOutOfBoundsException e){
                if (i%7 == 1) textView.setTextColor(getContext().getColor(android.R.color.holo_red_dark));
            } finally {
                binding.gridDate.addView(textView);
            }
        }
        if (adapter == null){
            adapter = new KehadiranAdapter(getContext(), kehadiranArr);
            binding.recyclerView.setAdapter(adapter);
        }

        if (kehadiranArr.length > 0){
            binding.tvNone.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);
            adapter.update(kehadiranArr);
        } else {
            binding.tvNone.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
        }
    }

    private TextView dateTextView(String text) {
        TextView textView = new TextView(getContext());
        textView.setLayoutParams(new GridLayout.LayoutParams(GridLayout.spec(
                GridLayout.UNDEFINED,GridLayout.FILL,1f),
                GridLayout.spec(GridLayout.UNDEFINED,GridLayout.FILL,1f)));
        textView.setText(text);
        textView.setGravity(Gravity.CENTER);
        textView.setTextAppearance(com.google.android.material.R.style.TextAppearance_AppCompat_Body1);
        textView.setPadding(8, 8, 8, 8);
        return textView;
    }

    private int getColorAttr(@AttrRes int resId) {
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(resId, typedValue, true);
        return typedValue.data;
    }
}