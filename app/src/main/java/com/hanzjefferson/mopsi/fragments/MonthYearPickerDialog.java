package com.hanzjefferson.mopsi.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hanzjefferson.mopsi.databinding.DialogMonthYearPickerBinding;

import java.util.Calendar;

public class MonthYearPickerDialog extends DialogFragment {
    private static final int MAX_YEAR = 2099;
    private static final int MIN_YEAR = 1970;

    private DatePickerDialog.OnDateSetListener listener;

    public void setListener(DatePickerDialog.OnDateSetListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());

        Calendar cal = Calendar.getInstance();

        DialogMonthYearPickerBinding binding = DialogMonthYearPickerBinding.inflate(getActivity().getLayoutInflater());

        binding.pickerMonth.setMinValue(1);
        binding.pickerMonth.setMaxValue(12);
        binding.pickerMonth.setValue(cal.get(Calendar.MONTH)+1);
        binding.pickerMonth.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (oldVal == 12 && newVal == 1) {
                binding.pickerYear.setValue(binding.pickerYear.getValue() + 1);
            } else if (oldVal == 1 && newVal == 12) {
                binding.pickerYear.setValue(binding.pickerYear.getValue() - 1);
            }
        });

        binding.pickerYear.setMinValue(MIN_YEAR);
        binding.pickerYear.setMaxValue(MAX_YEAR);
        binding.pickerYear.setValue(cal.get(Calendar.YEAR));

        builder.setView(binding.getRoot())
                // Add action buttons
                .setPositiveButton("Setel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDateSet(null, binding.pickerYear.getValue(), binding.pickerMonth.getValue()-1, 0);
                    }
                })
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MonthYearPickerDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}