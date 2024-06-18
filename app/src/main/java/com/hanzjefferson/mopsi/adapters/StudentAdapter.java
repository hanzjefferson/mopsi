package com.hanzjefferson.mopsi.adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hanzjefferson.mopsi.databinding.ItemStudentBinding;
import com.hanzjefferson.mopsi.models.Student;

public class StudentAdapter extends ListAdapter<Student, StudentAdapter.ViewHolder>{

    public StudentAdapter(Context context) {
        super(context);
    }

    public StudentAdapter(Context context, Student[] models) {
        super(context, models);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemStudentBinding.inflate(getLayoutInflater(), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Student student = getItemModel(position);

        ObjectAnimator.ofFloat(holder.binding.getRoot(), View.ALPHA, 0, 1).setDuration(500).start();

        attachOnClickListener(holder.binding.getRoot(), student, position);
        holder.binding.tvName.setText(student.student);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ItemStudentBinding binding;
        public ViewHolder(ItemStudentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
