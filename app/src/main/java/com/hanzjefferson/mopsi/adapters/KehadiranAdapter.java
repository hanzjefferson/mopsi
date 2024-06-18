package com.hanzjefferson.mopsi.adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hanzjefferson.mopsi.R;
import com.hanzjefferson.mopsi.databinding.ItemHadirBinding;
import com.hanzjefferson.mopsi.models.Kehadiran;

public class KehadiranAdapter extends ListAdapter<Kehadiran, KehadiranAdapter.ViewHolder> {


    public KehadiranAdapter(Context context) {
        super(context);
    }

    public KehadiranAdapter(Context context, Kehadiran[] models) {
        super(context, models);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemHadirBinding.inflate(getLayoutInflater(), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Kehadiran kehadiran = getItemModel(position);

        if (kehadiran == null) return;
        ObjectAnimator.ofFloat(holder.binding.getRoot(), View.ALPHA, 0, 1).setDuration(500).start();

        holder.binding.tvStatus.setText(kehadiran.status.substring(0, 1).toUpperCase()+kehadiran.status.substring(1));
        holder.binding.tvKeterangan.setText(kehadiran.keterangan);
        holder.binding.tvDate.setText(String.format("%02d", position+1));
        if (kehadiran.status.equals(Kehadiran.STATUS_HADIR)) {
            holder.binding.tvDate.setBackground(getContext().getDrawable(R.drawable.rounded_green_background));
        } else if (kehadiran.status.equals(Kehadiran.STATUS_SAKIT) || kehadiran.status.equals(Kehadiran.STATUS_IZIN)) {
            holder.binding.tvDate.setBackground(getContext().getDrawable(R.drawable.rounded_orange_background));
        } else if (kehadiran.status.equals(Kehadiran.STATUS_ALPHA)) {
            holder.binding.tvDate.setBackground(getContext().getDrawable(R.drawable.rounded_red_background));
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ItemHadirBinding binding;
        public ViewHolder(ItemHadirBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
