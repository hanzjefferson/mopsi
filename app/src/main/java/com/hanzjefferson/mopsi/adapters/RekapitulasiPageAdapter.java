package com.hanzjefferson.mopsi.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.hanzjefferson.mopsi.fragments.KehadiranFragment;
import com.hanzjefferson.mopsi.fragments.PoinFragment;
import com.hanzjefferson.mopsi.fragments.RekapitulasiFragment;
import com.hanzjefferson.mopsi.models.Rekapitulasi;


public class RekapitulasiPageAdapter extends FragmentStateAdapter {
    private final RekapitulasiFragment[] fragments;
    private Rekapitulasi rekapitulasi;

    public RekapitulasiPageAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.fragments = new RekapitulasiFragment[]{
                new KehadiranFragment(),
                new PoinFragment()
        };
    }

    public RekapitulasiFragment getFragment(int position){
        return fragments[position];
    }

    public void setData(Rekapitulasi rekapitulasi){
        this.rekapitulasi = rekapitulasi;
        for (RekapitulasiFragment fragment : fragments){
            fragment.onReceiveData(rekapitulasi);
        }
    }

    public Rekapitulasi getData(){
        return rekapitulasi;
    }

    @Override
    public Fragment createFragment(int position) {
        return fragments[position];
    }

    @Override
    public int getItemCount() {
        return fragments.length;
    }

}