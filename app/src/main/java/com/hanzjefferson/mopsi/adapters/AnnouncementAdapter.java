package com.hanzjefferson.mopsi.adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hanzjefferson.mopsi.databinding.ItemAnnouncementBinding;
import com.hanzjefferson.mopsi.models.Announcement;
import com.hanzjefferson.mopsi.models.Poin;

import java.text.SimpleDateFormat;

public class AnnouncementAdapter extends ListAdapter<Announcement, AnnouncementAdapter.ViewHolder> {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY/MM/dd hh:mm");
    private Context ctx;
    private Poin[] poins;
    private boolean editable = false;

    public AnnouncementAdapter(Context context) {
        super(context);
    }

    public AnnouncementAdapter(Context context, Announcement[] models) {
        super(context, models);
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemAnnouncementBinding.inflate(getLayoutInflater(), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Announcement announcement = getItemModel(position);

        ObjectAnimator.ofFloat(holder.binding.getRoot(), View.ALPHA, 0, 1).setDuration(500).start();

        holder.binding.tvTitle.setText(announcement.title);
        holder.binding.tvAuthor.setText("Oleh "+announcement.author);
        holder.binding.tvDate.setText(announcement.created_at);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ItemAnnouncementBinding binding;
        public ViewHolder(ItemAnnouncementBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
