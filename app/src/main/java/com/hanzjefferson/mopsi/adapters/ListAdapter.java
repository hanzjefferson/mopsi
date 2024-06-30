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
    private List<I> models = new ArrayList<>();
    private onItemClickListener<I> onItemClickListener;
    private List<I> searchModels = new ArrayList<>();
    private String searchQuery = "";
    
    public ListAdapter(Context context) {
        this.context = context;
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

    public void setSearchQuery(String searchQuery){
        this.searchQuery = searchQuery.toLowerCase();
        searchModels.clear();
        for (I model : models) {
            I item = onItemSearch(model);
            if (item != null) {
                searchModels.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public String getSearchQuery(){
        return searchQuery;
    }

    public I onItemSearch(I model){
        return model;
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
        return !searchQuery.isEmpty() ? searchModels : models;
    }
    
    public I getItemModel(int index){
        return !searchQuery.isEmpty() ? searchModels.get(index) : getItemModels().get(index);
    }

    @Override
    public int getItemCount() {
        return !searchQuery.isEmpty() ? searchModels.size() : models.size();
    }
}
