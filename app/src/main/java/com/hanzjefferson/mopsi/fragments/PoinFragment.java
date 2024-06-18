package com.hanzjefferson.mopsi.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DividerItemDecoration;

import com.android.volley.VolleyError;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.hanzjefferson.mopsi.R;
import com.hanzjefferson.mopsi.adapters.PoinAdapter;
import com.hanzjefferson.mopsi.databinding.FragmentPoinBinding;
import com.hanzjefferson.mopsi.databinding.SheetPoinAddBinding;
import com.hanzjefferson.mopsi.databinding.SheetPoinEditBinding;
import com.hanzjefferson.mopsi.databinding.SheetPoinMenuBinding;
import com.hanzjefferson.mopsi.models.Poin;
import com.hanzjefferson.mopsi.models.Rekapitulasi;
import com.hanzjefferson.mopsi.models.Response;
import com.hanzjefferson.mopsi.utils.AccountUtils;
import com.hanzjefferson.mopsi.utils.ApiServiceUtils;
import com.hanzjefferson.mopsi.utils.SocketUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class PoinFragment extends RekapitulasiFragment {

    private FragmentPoinBinding binding;
    private int id;
    private String unique;
    private Map<String, Poin[]> data = new HashMap<>();
    private PoinAdapter adapter;
    private String tanggal = "";
    private SheetPoinMenuBinding menuBinding;
    private BottomSheetDialog menuSheetDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPoinBinding.inflate(inflater, container, false);

        binding.getRoot().setEnabled(false);

        binding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        adapter = new PoinAdapter(getContext());
        adapter.setOnItemClickListener((view, poin1, position) -> {
            int index = adapter.getItemModels().indexOf(poin1);
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
            SheetPoinEditBinding editBinding = SheetPoinEditBinding.inflate(getLayoutInflater());
            editBinding.inputBobot.setText(String.valueOf(poin1.bobot));
            editBinding.inputBobot.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    editBinding.buttonEdit.setEnabled(!s.toString().equals("0")&&!s.toString().isEmpty());
                }
            });

            editBinding.inputKeterangan.setText(poin1.keterangan);

            SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
            AtomicReference<Long> date = new AtomicReference<>(new Date().getTime());

            editBinding.inputTanggal.setText(poin1.tanggal);
            editBinding.inputTanggal.setOnClickListener(inp -> {
                MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                        .setCalendarConstraints(new CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now()).build())
                        .setSelection(date.get())
                        .build();
                materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                    date.set(selection);
                    editBinding.inputTanggal.setText(dateFormat.format(new Date(selection)));
                });
                materialDatePicker.show(getParentFragmentManager(), "date_picker");
            });

            editBinding.buttonDelete.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle("Konfirmasi")
                        .setMessage("Apakah anda yakin ingin menghapus poin ini?")
                        .setPositiveButton("Ya", ((dialog, which) -> {
                            bottomSheetDialog.cancel();
                            List<Poin> poinList = adapter.getItemModels();
                            poinList.remove(index);
                            data.put("prerekap", poinList.toArray(new Poin[0]));

                            if (index >= adapter.unsavedLength) {
                                binding.recyclerView.setVisibility(View.GONE);
                                binding.spinKitView.setVisibility(View.VISIBLE);
                                binding.tvNone.setVisibility(View.GONE);

                                if (poinList.size() > adapter.unsavedLength) {
                                    poinList.subList(poinList.size()-adapter.unsavedLength, poinList.size()).clear();
                                }
                                ApiServiceUtils.saveRekap(new ApiServiceUtils.CallbackListener<JsonObject>() {
                                    @Override
                                    public void onResponse(Response<JsonObject> response) {
                                        if (response.isSuccess()) adapter.unsavedLength = 0;
                                        else renderData();
                                        new MaterialAlertDialogBuilder(getContext())
                                                .setTitle(response.isSuccess()? "Berhasil":"Gagal")
                                                .setMessage((response.isSuccess()? "":"Gagal menghapus rekap: ")+response.message)
                                                .setPositiveButton("OK", null)
                                                .show();
                                    }

                                    @Override
                                    public void onError(VolleyError error) {
                                        renderData();

                                        new MaterialAlertDialogBuilder(getContext())
                                                .setTitle("Kesalahan Klien")
                                                .setMessage(error.getMessage())
                                                .setPositiveButton("OK", null)
                                                .show();
                                    }
                                }, id, unique, poinList.toArray(new Poin[0]));
                                return;
                            }else adapter.unsavedLength--;
                            renderData();
                        }))
                        .setNegativeButton("Tidak", null)
                        .show();
            });

            editBinding.buttonEdit.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle("Konfirmasi")
                        .setMessage("Apakah anda yakin ingin mengedit poin ini?")
                        .setPositiveButton("Ya", ((dialog, which) -> {
                            bottomSheetDialog.cancel();
                            poin1.bobot = Integer.parseInt(editBinding.inputBobot.getText().toString());
                            poin1.keterangan = editBinding.inputKeterangan.getText().toString();
                            poin1.tanggal = editBinding.inputTanggal.getText().toString();

                            List<Poin> poinList = adapter.getItemModels();
                            poinList.set(index, poin1);
                            data.put("prerekap", poinList.toArray(new Poin[0]));

                            if (index >= adapter.unsavedLength) {
                                binding.recyclerView.setVisibility(View.GONE);
                                binding.spinKitView.setVisibility(View.VISIBLE);
                                binding.tvNone.setVisibility(View.GONE);

                                if (poinList.size() > adapter.unsavedLength) {
                                    poinList.subList(poinList.size()-adapter.unsavedLength, poinList.size()).clear();
                                }
                                ApiServiceUtils.saveRekap(new ApiServiceUtils.CallbackListener<JsonObject>() {
                                    @Override
                                    public void onResponse(Response<JsonObject> response) {
                                        if (response.isSuccess()) adapter.unsavedLength = 0;
                                        else renderData();
                                        new MaterialAlertDialogBuilder(getContext())
                                                .setTitle(response.isSuccess()? "Berhasil":"Gagal")
                                                .setMessage((response.isSuccess()? "":"Gagal mengedit rekap: ")+response.message)
                                                .setPositiveButton("OK", null)
                                                .show();
                                    }

                                    @Override
                                    public void onError(VolleyError error) {
                                        renderData();

                                        new MaterialAlertDialogBuilder(getContext())
                                                .setTitle("Kesalahan Klien")
                                                .setMessage(error.getMessage())
                                                .setPositiveButton("OK", null)
                                                .show();
                                    }
                                }, id, unique, poinList.toArray(new Poin[0]));
                                return;
                            }
                            renderData();

                        }))
                        .setNegativeButton("Tidak", null)
                        .show();
            });

            bottomSheetDialog.setContentView(editBinding.getRoot());
            bottomSheetDialog.setCancelable(true);
            bottomSheetDialog.show();
        });
        binding.recyclerView.setAdapter(adapter);

        if (data != null) renderData();

        binding.chipGroup.setOnCheckedStateChangeListener((chipGroup, list) -> {
            Chip chip = chipGroup.findViewById(chipGroup.getCheckedChipId());
            String chipText = chip.getText().toString();
            if (chipText.equals("Pre-rekap")) chipText = "prerekap";
            changeRekap(chipText);
        });

        menuBinding = SheetPoinMenuBinding.inflate(inflater);
        menuSheetDialog = new BottomSheetDialog(getContext());
        menuSheetDialog.setContentView(menuBinding.getRoot());
        menuSheetDialog.setCancelable(true);

        menuBinding.layoutAdd.setOnClickListener(v -> menuAdd());
        menuBinding.layoutWrite.setOnClickListener(v -> menuWrite());
        menuBinding.layoutSave.setOnClickListener(v -> menuSave());
        menuBinding.layoutDelete.setOnClickListener(v -> menuDelete());

        binding.fab.setOnClickListener(v -> {
            if (adapter.unsavedLength == 0){
                menuBinding.layoutSave.setVisibility(View.GONE);
                menuBinding.layoutWrite.setVisibility(View.VISIBLE);
            }else{
                menuBinding.layoutSave.setVisibility(View.VISIBLE);
                menuBinding.layoutWrite.setVisibility(View.GONE);
            }

            if (AccountUtils.getProfile().role_id != 5){
                menuBinding.layoutWrite.setVisibility(View.GONE);
                menuBinding.layoutDelete.setVisibility(View.GONE);
            }
            menuSheetDialog.show();
        });

        SocketUtils.on("prerekap", new TypeToken<Map<String, Map<String, Poin[]>>>(){}.getType(), (Map<String, Map<String, Poin[]>> data) -> {
            getActivity().runOnUiThread(() -> {
                if (data.containsKey(unique)) this.data.put("prerekap", data.get(unique).get(String.valueOf(id)));
                else this.data.put("prerekap", new Poin[0]);
                renderData();
            });
        });

        SocketUtils.on("prerekap/write", (Object[] data) -> {
            getActivity().runOnUiThread(() -> {
                if (this.data.containsKey("prerekap")){
                    this.data.remove("prerekap");
                    this.tanggal = (String) data[0];
                    Map<String, Map<String, Poin[]>> poin = new Gson().fromJson((String) data[1], new TypeToken<Map<String, Map<String, Poin[]>>>(){}.getType());
                    if (poin.containsKey(unique)){
                        adapter.unsavedLength = 0;
                        this.data.put(this.tanggal, poin.get(unique).get(String.valueOf(id)));
                    }
                    renderData();
                }
            });
        });

        SocketUtils.on("prerekap/delete", (Object[] data) -> {
            getActivity().runOnUiThread(() -> {
                if (this.data.containsKey("prerekap")){
                    this.data.remove("prerekap");
                    renderData();
                }
            });
        });
        return binding.getRoot();
    }

    @Override
    public void onReceiveData(Rekapitulasi rekapitulasi) {
        this.data = rekapitulasi.poin;
        this.id = rekapitulasi.id;
        this.unique = rekapitulasi.unique;
        if (binding == null) return;
        renderData();
    }

    private void renderData(){
        List<Chip> chipList = new ArrayList<>();
        binding.chipGroup.removeAllViews();

        int i = 0;
        boolean foundTanggal = false;
        boolean therePreRekap = false;
        for (String key : data.keySet()){
            if (key.equals(tanggal)) foundTanggal = true;
            if (key.equals("prerekap")) {
                therePreRekap = true;
                continue;
            }

            Chip chip = new Chip(getContext());
            chip.setText(key);
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setChecked(foundTanggal);
            chip.setCheckedIconVisible(true);
            chip.setCheckedIcon(getContext().getDrawable(R.drawable.ic_check));
            chipList.add(chip);
            i++;
        }

        int selected = 0;
        if (therePreRekap) {
            if (!foundTanggal) {
                foundTanggal = true;
                tanggal = "prerekap";
            }
            Chip chip = new Chip(getContext());
            chip.setText("Pre-rekap");
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setChecked(foundTanggal);
            chip.setChipIcon(getContext().getDrawable(R.drawable.ic_history));
            chipList.add(chip);
        }else if (AccountUtils.getProfile().role_id == 5){
            selected = 1;
            Chip chip = new Chip(getContext());
            chip.setText("Tambahkan Rekap");
            chip.setCheckable(false);
            chip.setClickable(true);
            chip.setChipIcon(getContext().getDrawable(R.drawable.ic_add));
            chip.setOnClickListener(v->{
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle("Konfirmasi")
                        .setMessage("Apakah anda yakin ingin menambahkan rekap baru?")
                        .setPositiveButton("Ya", (dialog, which) -> {
                            newRekap();
                        })
                        .setNegativeButton("Tidak", null)
                        .show();
            });
            chipList.add(chip);
        }

        Collections.reverse(chipList);
        if (!foundTanggal && selected < chipList.size()){
            System.out.print("sisi");
            tanggal = chipList.get(selected).getText().toString();
            chipList.get(selected).setChecked(true);
        }

        for (Chip chip : chipList){
            binding.chipGroup.addView(chip);
        }

        if (i > 0 || therePreRekap){
            changeRekap(tanggal);
        }else {
            binding.recyclerView.setVisibility(View.GONE);
            binding.spinKitView.setVisibility(View.GONE);
            binding.tvNone.setVisibility(View.VISIBLE);
        }
    }

    private void newRekap(){
        String tanggalBackup = tanggal;
        tanggal = "prerekap";
        data.put("prerekap", new Poin[0]);
        renderData();

        binding.recyclerView.setVisibility(View.GONE);
        binding.spinKitView.setVisibility(View.VISIBLE);
        binding.tvNone.setVisibility(View.GONE);
        ApiServiceUtils.newRekap(new ApiServiceUtils.CallbackListener<JsonObject>() {
            @Override
            public void onResponse(Response<JsonObject> response) {
                if (response.isSuccess()){
                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle("Berhasil")
                            .setMessage("Rekap baru berhasil dibuat")
                            .setPositiveButton("OK", null)
                            .show();
                }else{
                    tanggal = tanggalBackup;
                    data.remove("prerekap");
                    renderData();
                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle("Gagal")
                            .setMessage("Gagal membuat rekap baru, "+response.message)
                            .setPositiveButton("OK", null)
                            .show();
                }
            }

            @Override
            public void onError(VolleyError error) {
                tanggal = tanggalBackup;
                data.remove("prerekap");
                renderData();
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle("Kesalahan Klien")
                        .setMessage(error.getMessage())
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void changeRekap(String tanggal){
        if (tanggal.equals("prerekap")) {
            adapter.setEditable(AccountUtils.getProfile().role_id == 2 || AccountUtils.getProfile().role_id == 4 || AccountUtils.getProfile().role_id == 5);
            binding.fab.show();
        }
        else {
            adapter.setEditable(false);
            binding.fab.hide();
        }

        Poin[] poin = new Poin[0];

        if (data.containsKey(tanggal)) poin = data.get(tanggal);

        if (poin == null || poin.length == 0){
            binding.spinKitView.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.GONE);
            binding.tvNone.setVisibility(View.VISIBLE);
            return;
        }else{
            binding.spinKitView.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.tvNone.setVisibility(View.GONE);
        }
        adapter.update(poin);
    }

    private void menuAdd(){
        menuSheetDialog.dismiss();
        SheetPoinAddBinding menuAddBinding = SheetPoinAddBinding.inflate(getLayoutInflater());
        menuAddBinding.inputBobot.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                menuAddBinding.buttonAdd.setEnabled(!s.toString().equals("0")&&!s.toString().isEmpty());
            }
        });

        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
        AtomicReference<Long> date = new AtomicReference<>(new Date().getTime());

        menuAddBinding.inputTanggal.setText(dateFormat.format(new Date()));
        menuAddBinding.inputTanggal.setOnClickListener(inp -> {
            MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setCalendarConstraints(new CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now()).build())
                    .setSelection(date.get())
                    .build();
            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                date.set(selection);
                menuAddBinding.inputTanggal.setText(dateFormat.format(new Date(selection)));
            });
            materialDatePicker.show(getParentFragmentManager(), "date_picker");
        });

        menuAddBinding.buttonAdd.setOnClickListener(btn -> {
            Poin poin = new Poin();
            poin.bobot = Integer.parseInt(menuAddBinding.inputBobot.getText().toString());
            poin.keterangan = menuAddBinding.inputKeterangan.getText().toString();
            poin.tanggal = menuAddBinding.inputTanggal.getText().toString();

            if (data.containsKey("prerekap")){
                List<Poin> list = new LinkedList<>(Arrays.asList(data.get("prerekap")));
                list.add(poin);
                adapter.unsavedLength++;
                data.put("prerekap", list.toArray(new Poin[0]));
                changeRekap("prerekap");
            }
        });
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(menuAddBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
    }

    private void menuSave(){
        menuSheetDialog.dismiss();
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Konfirmasi")
                .setMessage("Apakah anda yakin ingin menyimpan rekap ini?")
                .setPositiveButton("Ya", ((dialog, which) -> {
                    binding.recyclerView.setVisibility(View.GONE);
                    binding.spinKitView.setVisibility(View.VISIBLE);
                    binding.tvNone.setVisibility(View.GONE);

                    ApiServiceUtils.saveRekap(new ApiServiceUtils.CallbackListener<JsonObject>() {
                        @Override
                        public void onResponse(Response<JsonObject> response) {
                            if (response.isSuccess()) adapter.unsavedLength = 0;
                            else renderData();
                            new MaterialAlertDialogBuilder(getContext())
                                    .setTitle(response.isSuccess()? "Berhasil":"Gagal")
                                    .setMessage((response.isSuccess()? "":"Gagal menyimpan rekap: ")+response.message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }

                        @Override
                        public void onError(VolleyError error) {
                            renderData();

                            new MaterialAlertDialogBuilder(getContext())
                                    .setTitle("Kesalahan Klien")
                                    .setMessage(error.getMessage())
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }, id, unique, adapter.getItemModels().toArray(new Poin[0]));
                }))
                .setNegativeButton("Tidak", null)
                .show();
    }

    private void menuWrite(){
        menuSheetDialog.dismiss();
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Konfirmasi")
                .setMessage("Apakah anda yakin ingin menyimpan rekap ini (untuk selamanya)?")
                .setPositiveButton("Ya", ((dialog, which) -> {
                    binding.recyclerView.setVisibility(View.GONE);
                    binding.spinKitView.setVisibility(View.VISIBLE);
                    binding.tvNone.setVisibility(View.GONE);

                    ApiServiceUtils.writeRekap(new ApiServiceUtils.CallbackListener<JsonObject>() {
                        @Override
                        public void onResponse(Response<JsonObject> response) {
                            if (!response.isSuccess()) renderData();
                            new MaterialAlertDialogBuilder(getContext())
                                    .setTitle(response.isSuccess()? "Berhasil":"Gagal")
                                    .setMessage((response.isSuccess()? "":"Gagal menulis rekap: ")+response.message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }

                        @Override
                        public void onError(VolleyError error) {
                            renderData();

                            new MaterialAlertDialogBuilder(getContext())
                                    .setTitle("Kesalahan Klien")
                                    .setMessage(error.getMessage())
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    });
                }))
                .setNegativeButton("Tidak", null)
                .show();
    }

    private void menuDelete(){
        menuSheetDialog.dismiss();
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Konfirmasi")
                .setMessage("Apakah anda yakin ingin menghapus rekap ini?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    binding.recyclerView.setVisibility(View.GONE);
                    binding.spinKitView.setVisibility(View.VISIBLE);
                    binding.tvNone.setVisibility(View.GONE);

                    ApiServiceUtils.deleteRekap(new ApiServiceUtils.CallbackListener<JsonObject>() {
                        @Override
                        public void onResponse(Response<JsonObject> response) {
                            if (!response.isSuccess()) renderData();
                            new MaterialAlertDialogBuilder(getContext())
                                    .setTitle(response.isSuccess()? "Berhasil":"Gagal")
                                    .setMessage((response.isSuccess()? "":"Gagal menghapus rekap: ")+response.message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }

                        @Override
                        public void onError(VolleyError error) {
                            renderData();

                            new MaterialAlertDialogBuilder(getContext())
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
}