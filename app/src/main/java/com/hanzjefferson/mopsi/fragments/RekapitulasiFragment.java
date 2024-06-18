package com.hanzjefferson.mopsi.fragments;

import androidx.fragment.app.Fragment;

import com.hanzjefferson.mopsi.models.Rekapitulasi;

public abstract class RekapitulasiFragment extends Fragment {
    public abstract void onReceiveData(Rekapitulasi rekapitulasi);

}
