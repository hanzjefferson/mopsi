package com.hanzjefferson.mopsi.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hanzjefferson.mopsi.databinding.ItemClassBinding;
import com.hanzjefferson.mopsi.databinding.SheetClassBinding;
import com.hanzjefferson.mopsi.models.Kelas;
import com.hanzjefferson.mopsi.models.Student;

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
        sheetClassBinding.tvTeacher.setText(kelas.teacher);
        sheetClassBinding.tvJumlah.setText(String.valueOf(kelas.members.length));
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
            holder.binding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

            StudentAdapter studentAdapter = new StudentAdapter(getContext(), kelas.members);
            studentAdapter.setOnItemClickListener(studentOnItemClickListener);
            holder.binding.recyclerView.setAdapter(null);
            holder.binding.recyclerView.setAdapter(studentAdapter);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ItemClassBinding binding;
        public ViewHolder(ItemClassBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
