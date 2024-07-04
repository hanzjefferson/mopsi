package com.hanzjefferson.mopsi.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.hanzjefferson.mopsi.TemplatePoinActivity;
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
    private ActivityResultLauncher templatePoinLauncher;
    private BottomSheetDialog bottomSheetDialog;
    private SheetPoinEditBinding editBinding;
    private SheetPoinAddBinding navAddBinding;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPoinBinding.inflate(inflater, container, false);

        binding.getRoot().setEnabled(false);

        binding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        bottomSheetDialog = new BottomSheetDialog(getContext());
        editBinding = SheetPoinEditBinding.inflate(getLayoutInflater());
        navAddBinding = SheetPoinAddBinding.inflate(getLayoutInflater());

        templatePoinLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode() == RESULT_OK){
                    Poin poin = new Gson().fromJson(o.getData().getStringExtra("template"), Poin.class);
                    if (poin != null){
                        navAddBinding.inputBobot.setText(String.valueOf(poin.bobot));
                        navAddBinding.inputKeterangan.setText(poin.keterangan);
                    }
                }
            }
        });
        adapter = new PoinAdapter(getContext());
        adapter.setOnItemClickListener((view, poin1, position) -> {
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
                            setLoading(true);
                            List<Poin> poinList = adapter.getItemModels();
                            poinList.remove(position);
                            saveUnPub(poinList.toArray(new Poin[0]));
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
                            poinList.set(position, poin1);
                            saveUnPub(poinList.toArray(new Poin[0]));
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

        binding.layoutAdd.setOnClickListener(v->addPoin());
        binding.layoutWrite.setOnClickListener(v->writeUnPub());

        binding.chipGroup.setOnCheckedStateChangeListener((chipGroup, list) -> {
            Chip chip = chipGroup.findViewById(chipGroup.getCheckedChipId());
            String chipText = chip.getText().toString();
            if (chipText.equals("Unpublished")) chipText = "unpublished";
            changeRekap(chipText);
        });

        binding.fab.setOnClickListener(v -> deleteRekap());

        SocketUtils.on("rekap", JsonObject.class, (JsonObject data) -> {
            getActivity().runOnUiThread(() -> {
                setLoading(false);
                if (data == null) return;
                this.data.clear();
                for (String tanggal : data.keySet()) {
                    this.data.put(tanggal, new Poin[0]);
                    JsonObject tanggalObject = data.get(tanggal).getAsJsonObject();
                    if (tanggalObject.has(unique)) {
                        JsonObject uniqueObject = tanggalObject.getAsJsonObject(unique);
                        if (uniqueObject.has(String.valueOf(id))) {
                            Poin[] poinArray = new Gson().fromJson(uniqueObject.get(String.valueOf(id)), Poin[].class);
                            this.data.put(tanggal, poinArray);
                        }
                    }
                }
                renderData();
            });
        });

        /*SocketUtils.on("unpublished", JsonObject.class, (JsonObject data) -> {
            getActivity().runOnUiThread(() -> {
                setLoading(false);
                if (data == null);
                int unpubId = data.has("id")? data.get("id").getAsInt() : -1;
                String unpubUnique = data.has("unique")? data.get("unique").getAsString() : null;
                Poin[] unpubPoin = data.has("data")? new Gson().fromJson(data.get("data"), Poin[].class) : null;

                if (unpubId != -1 && unpubUnique != null && unpubPoin != null){
                    if (this.data.containsKey("unpublished")) this.data.remove("unpublished");
                    this.data.put("unpublished", unpubPoin);
                }
            });
        });*/
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
        boolean thereUnpub = false;
        for (String key : data.keySet()){
            if (key.equals(tanggal)) foundTanggal = true;
            if (key.equals("unpublished")) {
                thereUnpub = true;
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
        int roleId = AccountUtils.getProfile().role_id;
        if (thereUnpub && (roleId == 2 || roleId == 4 || roleId == 5)) {
            if (!foundTanggal) {
                foundTanggal = true;
                tanggal = "unpublished";
            }
            Chip chip = new Chip(getContext());
            chip.setText("Unpublished");
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
            tanggal = chipList.get(selected).getText().toString();
            chipList.get(selected).setChecked(true);
        }

        for (Chip chip : chipList){
            binding.chipGroup.addView(chip);
        }

        if (i > 0 || thereUnpub){
            changeRekap(tanggal);
        }else {
            setNone(true);
            binding.layoutInfo.setVisibility(View.GONE);
            binding.layoutNavigation.setVisibility(View.GONE);
            binding.fab.setVisibility(View.GONE);
        }
    }

    private void newRekap(){
        String tanggalBackup = tanggal;
        tanggal = "unpublished";
        data.put("unpublished", new Poin[0]);

        setLoading(true);
        ApiServiceUtils.newRekap(new ApiServiceUtils.CallbackListener<JsonObject>() {
            @Override
            public void onResponse(Response<JsonObject> response) {
                if (!response.isSuccess()){
                    setLoading(false);
                    tanggal = tanggalBackup;
                    data.remove("unpublished");
                }
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle(response.isSuccess()? "Berhasil":"Gagal")
                        .setMessage((response.isSuccess()? "":"Gagal menyimpan rekap: ")+response.message)
                        .setPositiveButton("OK", null)
                        .show();
            }

            @Override
            public void onError(VolleyError error) {
                setLoading(false);
                tanggal = tanggalBackup;
                data.remove("unpublished");
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle("Kesalahan Klien")
                        .setMessage(error.getMessage())
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void changeRekap(String tanggal){
        this.tanggal = tanggal;
        if (tanggal.equals("unpublished")) {
            adapter.setEditable(AccountUtils.getProfile().role_id == 2 || AccountUtils.getProfile().role_id == 4 || AccountUtils.getProfile().role_id == 5);
            if (AccountUtils.getProfile().role_id == 5) binding.fab.setVisibility(View.VISIBLE);
        } else {
            binding.fab.setVisibility(View.GONE);
            adapter.setEditable(false);
        }

        Poin[] poin = new Poin[0];

        if (data.containsKey(tanggal)) poin = data.get(tanggal);

        if (poin == null || poin.length == 0){
            setNone(true);
            return;
        }else{
            setNone(false);

            int jumlahPoin = 0;
            int poinPositif = 0;
            int poinNegatif = 0;
            for (Poin p : poin){
                jumlahPoin += p.bobot;
                if (p.bobot > 0) poinPositif += p.bobot;
                else poinNegatif += p.bobot;
            }
            binding.tvTotal.setText(String.valueOf(jumlahPoin).startsWith("-") ? String.valueOf(jumlahPoin) : "+" + String.valueOf(jumlahPoin));
            binding.tvTotal.setTextColor(getContext().getColor(jumlahPoin > 0 ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
            binding.tvPositive.setText(String.valueOf(poinPositif));
            binding.tvNegative.setText(String.valueOf(poinNegatif).startsWith("-") ? String.valueOf(poinNegatif) : "+" + String.valueOf(poinNegatif));
        }
        adapter.update(poin);
    }

    private void addPoin(){
        navAddBinding.inputBobot.setText("");
        navAddBinding.inputKeterangan.setText("");
        navAddBinding.inputBobot.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                navAddBinding.buttonAdd.setEnabled(!s.toString().equals("0")&&!s.toString().isEmpty());
            }
        });

        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
        AtomicReference<Long> date = new AtomicReference<>(new Date().getTime());

        navAddBinding.inputTanggal.setText(dateFormat.format(new Date()));
        navAddBinding.inputTanggal.setOnClickListener(inp -> {
            MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setCalendarConstraints(new CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now()).build())
                    .setSelection(date.get())
                    .build();
            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                date.set(selection);
                navAddBinding.inputTanggal.setText(dateFormat.format(new Date(selection)));
            });
            materialDatePicker.show(getParentFragmentManager(), "date_picker");
        });

        navAddBinding.buttonTemplate.setOnClickListener(btn->{
            templatePoinLauncher.launch(new Intent(getContext(), TemplatePoinActivity.class));
        });
        navAddBinding.buttonAdd.setOnClickListener(btn -> {
            bottomSheetDialog.cancel();
            Poin poin = new Poin();
            poin.bobot = Integer.parseInt(navAddBinding.inputBobot.getText().toString());
            poin.keterangan = navAddBinding.inputKeterangan.getText().toString();
            poin.tanggal = navAddBinding.inputTanggal.getText().toString();

            List<Poin> poinList = adapter.getItemModels();
            poinList.add(0, poin);

            navAddBinding.inputBobot.setText("");
            navAddBinding.inputKeterangan.setText("");

            saveUnPub(poinList.toArray(new Poin[0]));
        });
        bottomSheetDialog.setContentView(navAddBinding.getRoot());
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();

        templatePoinLauncher.launch(new Intent(getContext(), TemplatePoinActivity.class));
    }

    private void saveUnPub(Poin[] poinArr){
        setLoading(true);
        ApiServiceUtils.saveRekap(new ApiServiceUtils.CallbackListener<JsonObject>() {
            @Override
            public void onResponse(Response<JsonObject> response) {
                setLoading(false);
                if (response.isSuccess()) data.put(tanggal, poinArr);
                renderData();
                MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(getContext())
                        .setTitle(response.isSuccess()? "Berhasil":"Gagal")
                        .setMessage((response.isSuccess()? "":"Gagal mengedit rekap: ")+response.message)
                        .setPositiveButton(response.isSuccess()? "OK":"Coba Lagi", (d, i)->{
                            if (!response.isSuccess()) saveUnPub(poinArr);
                        });
                if (!response.isSuccess()) dialogBuilder.setNegativeButton("Batal", null);
                dialogBuilder.show();
            }

            @Override
            public void onError(VolleyError error) {
                setLoading(false);
                renderData();

                new MaterialAlertDialogBuilder(getContext())
                        .setTitle("Kesalahan Klien")
                        .setMessage(error.getMessage())
                        .setPositiveButton("Coba lagi", (d, i)->{
                            saveUnPub(poinArr);
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            }
        }, id, unique, poinArr);
    }

    private void writeUnPub(){
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Konfirmasi")
                .setMessage("Apakah anda yakin ingin mempublikasikan rekap ini?")
                .setPositiveButton("Ya", ((dialog, which) -> {
                    String tanggalBackup = tanggal;
                    tanggal = "";
                    setLoading(true);
                    ApiServiceUtils.writeRekap(new ApiServiceUtils.CallbackListener<JsonObject>() {
                        @Override
                        public void onResponse(Response<JsonObject> response) {
                            if (!SocketUtils.getSocket().connected()||!response.isSuccess()) {
                                setLoading(false);
                                tanggal = tanggalBackup;
                                renderData();
                            }
                            new MaterialAlertDialogBuilder(getContext())
                                    .setTitle(response.isSuccess()? "Berhasil":"Gagal")
                                    .setMessage((response.isSuccess()? "":"Gagal menulis rekap: ")+response.message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }

                        @Override
                        public void onError(VolleyError error) {
                            setLoading(false);
                            tanggal = tanggalBackup;
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

    private void deleteRekap(){
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Konfirmasi")
                .setMessage("Apakah anda yakin ingin menghapus rekap ini?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    String tanggalBackup = tanggal;
                    tanggal = "";
                    setLoading(true);

                    ApiServiceUtils.CallbackListener<JsonObject> listener = new ApiServiceUtils.CallbackListener<JsonObject>() {
                        @Override
                        public void onResponse(Response<JsonObject> response) {
                            if (!SocketUtils.getSocket().connected()||!response.isSuccess()) {
                                setLoading(false);
                                tanggal = tanggalBackup;
                                renderData();
                            }
                            new MaterialAlertDialogBuilder(getContext())
                                    .setTitle(response.isSuccess()? "Berhasil":"Gagal")
                                    .setMessage((response.isSuccess()? "":"Gagal menghapus rekap: ")+response.message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }

                        @Override
                        public void onError(VolleyError error) {
                            setLoading(false);
                            tanggal = tanggalBackup;
                            renderData();

                            new MaterialAlertDialogBuilder(getContext())
                                    .setTitle("Kesalahan Klien")
                                    .setMessage(error.getMessage())
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    };
                    if (tanggalBackup.equals("unpublished")) ApiServiceUtils.deleteRekap(listener);
                    else ApiServiceUtils.deleteRekap(listener, tanggalBackup);
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    @Override
    public boolean onItemSearch(String query) {
        if (adapter != null) {
            adapter.setSearchQuery(query);
            if (adapter.getItemModels().isEmpty()) {
                binding.tvNone.setVisibility(View.VISIBLE);
            } else {
                binding.tvNone.setVisibility(View.GONE);
            }
            return true;
        }
        return super.onItemSearch(query);
    }

    private void setLoading(boolean state){
        if (state) {
            binding.layoutInfo.setVisibility(View.GONE);
            binding.chipGroup.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.GONE);
            binding.spinKitView.setVisibility(View.VISIBLE);
            binding.tvNone.setVisibility(View.GONE);
            binding.layoutNavigation.setVisibility(View.GONE);
        }else {
            binding.layoutNavigation.setVisibility(View.VISIBLE);
            if (!tanggal.equals("unpublished")) binding.layoutNavigation.setVisibility(View.GONE);
            else if (AccountUtils.getProfile().role_id != 5) {
                binding.layoutWrite.setVisibility(View.GONE);
            }

            binding.layoutInfo.setVisibility(View.VISIBLE);
            binding.chipGroup.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.spinKitView.setVisibility(View.GONE);
            binding.tvNone.setVisibility(View.GONE);
        }
    }

    private void setNone(boolean state){
        setLoading(false);
        if (state){
            binding.recyclerView.setVisibility(View.GONE);
            binding.tvNone.setVisibility(View.VISIBLE);
        }
    }
}