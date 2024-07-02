package com.hanzjefferson.mopsi.adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hanzjefferson.mopsi.databinding.ItemAnnouncementBinding;
import com.hanzjefferson.mopsi.databinding.ItemTemplatePoinBinding;
import com.hanzjefferson.mopsi.models.Announcement;
import com.hanzjefferson.mopsi.models.Poin;

import java.text.SimpleDateFormat;

public class TemplatePoinAdapter extends ListAdapter<Poin, TemplatePoinAdapter.ViewHolder> {
    private Context ctx;

    public TemplatePoinAdapter(Context context) {
        super(context);
    }

    public TemplatePoinAdapter(Context context, Poin[] models) {
        super(context, models);
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemTemplatePoinBinding.inflate(getLayoutInflater(), parent, false));
    }

    @Override
    public Poin onItemSearch(Poin model) {
        if (model.keterangan.toLowerCase().contains(getSearchQuery()) || String.valueOf(model.bobot).contains(getSearchQuery()))
            return model;
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Poin poin = getItemModel(position);

        ObjectAnimator.ofFloat(holder.binding.getRoot(), View.ALPHA, 0, 1).setDuration(500).start();

        holder.binding.tvKeterangan.setText(poin.keterangan);
        holder.binding.tvBobot.setText("Bobot: "+(String.valueOf(poin.bobot).startsWith("-")? String.valueOf(poin.bobot) : "+" + String.valueOf(poin.bobot)));
        if (poin.bobot > 0) holder.binding.tvBobot.setTextColor(getContext().getColor(android.R.color.holo_green_dark));
        else if (poin.bobot < -10) holder.binding.tvBobot.setTextColor(getContext().getColor(android.R.color.holo_orange_dark));
        else if (poin.bobot < -50) holder.binding.tvBobot.setTextColor(getContext().getColor(android.R.color.holo_red_dark));
        attachOnClickListener(holder.binding.getRoot(), poin, position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ItemTemplatePoinBinding binding;
        public ViewHolder(ItemTemplatePoinBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
