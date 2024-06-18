package com.hanzjefferson.mopsi.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.hanzjefferson.mopsi.databinding.CardAnnouncementBinding;
import com.hanzjefferson.mopsi.models.Announcement;

import java.text.SimpleDateFormat;

public class AnnouncementSlideAdapter extends RecyclerView.Adapter<AnnouncementSlideAdapter.ViewHolder> {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY/MM/dd hh:mm");
    private Context ctx;
    private Announcement[] announcements;
    private int limit = 3;

    public AnnouncementSlideAdapter(Context ctx, Announcement[] announcements) {
        this.ctx = ctx;
        this.announcements = announcements;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardAnnouncementBinding binding = CardAnnouncementBinding.inflate(LayoutInflater.from(ctx), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Announcement announcement = announcements[position];

        holder.binding.tvTitle.setText(announcement.title);
        holder.binding.tvAuthor.setText("Oleh "+announcement.author);
        holder.binding.tvContent.setText(announcement.content);
        holder.binding.tvDate.setText(announcement.created_at);
    }

    @Override
    public int getItemCount() {
        return Math.min(announcements.length, limit);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardAnnouncementBinding binding;
        public ViewHolder(CardAnnouncementBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
