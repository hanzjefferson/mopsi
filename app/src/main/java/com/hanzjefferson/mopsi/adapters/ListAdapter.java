package com.hanzjefferson.mopsi.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public abstract class ListAdapter<I, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    public interface onItemClickListener<I> {
        void onItemClick(View view, I model, int position);
    }

    private Context context;
    private List<I> models;
    private onItemClickListener<I> onItemClickListener;
    
    public ListAdapter(Context context) {
        this.context = context;
        this.models = new ArrayList<>();
    }
    
    public ListAdapter(Context context, I[] models){
        this.context = context;
        this.update(models);
    }
    
    public void update(I[] models){
        this.models = new LinkedList<>(Arrays.asList(models));
        notifyDataSetChanged();
    }

    public void add(I model){
        models.add(model);
        notifyItemInserted(models.size()-1);
    }

    public void delete(int index){
        models.remove(index);
        notifyItemRemoved(index);
    }

    public void set(int index, I model){
        models.set(index, model);
        notifyItemChanged(index);
    }

    public void setOnItemClickListener(onItemClickListener<I> onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public onItemClickListener<I> getOnItemClickListener(){
        return onItemClickListener;
    }

    public void attachOnClickListener(View view, I model, int position){
        if (onItemClickListener != null) view.setOnClickListener(v -> {
            onItemClickListener.onItemClick(v, model, position);
        });
    }
    
    public Context getContext(){
        return context;
    }
    
    public LayoutInflater getLayoutInflater(){
        return LayoutInflater.from(context);
    }
    
    public List<I> getItemModels(){
        return models;
    }
    
    public I getItemModel(int index){
        return getItemModels().get(index);
    }

    @Override
    public int getItemCount() {
        return models.size();
    }
}
