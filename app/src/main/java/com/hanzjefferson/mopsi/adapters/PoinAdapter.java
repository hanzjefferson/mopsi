package com.hanzjefferson.mopsi.adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hanzjefferson.mopsi.R;
import com.hanzjefferson.mopsi.databinding.ItemPoinBinding;
import com.hanzjefferson.mopsi.models.Poin;

import java.util.Arrays;

public class PoinAdapter extends ListAdapter<Poin, PoinAdapter.ViewHolder> {
    private boolean editable = false;

    public PoinAdapter(Context context) {
        super(context);
    }

    @Override
    public void update(Poin[] models) {
        Arrays.sort(models, (d1, d2) -> d2.tanggal.compareTo(d1.tanggal));
        super.update(models);
    }

    @Override
    public Poin onItemSearch(Poin model) {
        if (model.tanggal.toLowerCase().contains(getSearchQuery()) || model.keterangan.toLowerCase().contains(getSearchQuery()) || String.valueOf(model.bobot).contains(getSearchQuery()))
            return model;
        return null;
    }

    public void setEditable(boolean state){
        this.editable = state;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemPoinBinding.inflate(getLayoutInflater(), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Poin poin = getItemModel(position);

        /*if (position == 0) {
            holder.binding.layoutInfo.setVisibility(View.VISIBLE);

            int jumlahPoin = getItemModels().stream().mapToInt(p -> p.bobot).sum();
            int poinPositif = getItemModels().stream().mapToInt(p -> p.bobot).filter(b -> b <= 0).sum();
            int poinNegatif = getItemModels().stream().mapToInt(p -> p.bobot).filter(b -> b > 0).sum();
            holder.binding.tvTotal.setText(String.valueOf(jumlahPoin).startsWith("-") ? String.valueOf(jumlahPoin) : "+" + String.valueOf(jumlahPoin));
            holder.binding.tvTotal.setTextColor(getContext().getColor(jumlahPoin > 0 ? android.R.color.holo_red_dark : android.R.color.holo_green_dark));
            holder.binding.tvPositive.setText(String.valueOf(poinPositif));
            holder.binding.tvNegative.setText(String.valueOf(poin Negatif).startsWith("-") ? String.valueOf(poinNegatif) : "+" + String.valueOf(poinNegatif));
        }*/

        ObjectAnimator.ofFloat(holder.binding.getRoot(), View.ALPHA, 0, 1).setDuration(500).start();
        if (editable) attachOnClickListener(holder.binding.getRoot(), poin, position);
        else holder.binding.getRoot().setOnClickListener(null);
        holder.binding.indicator.setBackground(getContext().getDrawable(poin.bobot < 0? R.drawable.negative_indicator : R.drawable.positive_indicator));
        holder.binding.tvKeterangan.setText(poin.keterangan);
        holder.binding.tvTanggal.setText(poin.tanggal);
        holder.binding.tvPoin.setText(String.valueOf(poin.bobot).startsWith("-")? String.valueOf(poin.bobot) : "+" + String.valueOf(poin.bobot));
        holder.binding.tvPoin.setTextColor(getContext().getColor(poin.bobot < 0? android.R.color.holo_red_dark : android.R.color.holo_green_dark));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ItemPoinBinding binding;
        public ViewHolder(ItemPoinBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
