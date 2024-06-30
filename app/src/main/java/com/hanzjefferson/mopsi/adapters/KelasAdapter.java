package com.hanzjefferson.mopsi.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.hanzjefferson.mopsi.ProfileActivity;
import com.hanzjefferson.mopsi.databinding.ItemClassBinding;
import com.hanzjefferson.mopsi.databinding.SheetClassBinding;
import com.hanzjefferson.mopsi.models.Kelas;
import com.hanzjefferson.mopsi.models.Student;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class KelasAdapter extends ListAdapter<Kelas, KelasAdapter.ViewHolder>{
    private onItemClickListener<Student> studentOnItemClickListener;

    public KelasAdapter(Context context) {
        super(context);
    }

    public KelasAdapter(Context context, Kelas[] models) {
        super(context, models);
    }

    public void setStudentOnItemClickListener(ListAdapter.onItemClickListener<Student> studentOnItemClickListener) {
        this.studentOnItemClickListener = studentOnItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemClassBinding.inflate(getLayoutInflater(), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Kelas kelas = getItemModel(position);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        SheetClassBinding sheetClassBinding = SheetClassBinding.inflate(getLayoutInflater());
        sheetClassBinding.tvName.setText(kelas.name);
        sheetClassBinding.tvTeacher.setText(kelas.teacher.full_name);
        sheetClassBinding.tvJumlah.setText(String.valueOf(kelas.members.length));
        Intent viewProfileIntent = new Intent(getContext(), ProfileActivity.class);
        viewProfileIntent.putExtra("view_profile", new Gson().toJson(kelas.teacher));
        sheetClassBinding.buttonInfo.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            getContext().startActivity(viewProfileIntent);
        });
        bottomSheetDialog.setContentView(sheetClassBinding.getRoot());
        bottomSheetDialog.setCancelable(true);

        holder.binding.tvName.setText("Kelas "+kelas.name);
        holder.binding.tvName.setOnClickListener(v -> bottomSheetDialog.show());

        if (kelas.members.length == 0){
            holder.binding.tvNone.setVisibility(View.VISIBLE);
            holder.binding.recyclerView.setVisibility(View.GONE);
        }else {
            holder.binding.tvNone.setVisibility(View.GONE);
            holder.binding.recyclerView.setVisibility(View.VISIBLE);
            if (holder.binding.recyclerView.getAdapter() == null){
                holder.binding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

                StudentAdapter studentAdapter = new StudentAdapter(getContext(), kelas.members);
                studentAdapter.setOnItemClickListener(studentOnItemClickListener);
                holder.binding.recyclerView.setAdapter(studentAdapter);
            }else ((StudentAdapter)holder.binding.recyclerView.getAdapter()).update(kelas.members);
        }
    }

    @Override
    public Kelas onItemSearch(Kelas model) {
        AtomicBoolean found = new AtomicBoolean(false);
        if (model.name.toLowerCase().contains(getSearchQuery()) || model.teacher.full_name.toLowerCase().contains(getSearchQuery()))
            found.set(true);
        Kelas copy = new Gson().fromJson(new Gson().toJson(model), Kelas.class);
        copy.members = Arrays.stream(copy.members).filter(student -> student.student.toLowerCase().contains(getSearchQuery())).toArray(Student[]::new);
        if (copy.members.length > 0){
            model = copy;
            found.set(true);
        }
        return found.get() ? model : null;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ItemClassBinding binding;
        public ViewHolder(ItemClassBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
