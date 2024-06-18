package com.hanzjefferson.mopsi.utils;

import android.content.Context;

import com.android.volley.VolleyError;
import com.hanzjefferson.mopsi.models.Profile;
import com.hanzjefferson.mopsi.models.Response;

public class AccountUtils {
    public static final int ROLE_SISWA = 1;
    public static final int ROLE_PETUGAS_REKAP = 2;
    public static final int ROLE_WALI_SISWA = 3;
    public static final int ROLE_WALI_KELAS = 4;
    public static final int ROLE_TIM_TATIB = 5;

    private static Context context;
    private static Profile profile;

    public static void init(Context context) {
        AccountUtils.context = context;
    }

    public static boolean isLoggedIn() {
        return getToken() != null;
    }

    public static void saveState(String token) {
        AccountUtils.context.getSharedPreferences("auth", Context.MODE_PRIVATE).edit()
                .putString("token", token)
                .apply();
    }

    public static String getToken(){
        return AccountUtils.context.getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", null);
    }

    public static Profile getProfile(){
        return profile;
    }

    public static void fetchProfile(ApiServiceUtils.CallbackListener<Profile> listener){
        ApiServiceUtils.profile(new ApiServiceUtils.CallbackListener<Profile>() {
            @Override
            public void onResponse(Response<Profile> response) {
                profile = response.data;
                listener.onResponse(response);
            }

            @Override
            public void onError(VolleyError error) {
                listener.onError(error);
            }
        });
    }

    public static void clearState() {
        AccountUtils.context.getSharedPreferences("auth", Context.MODE_PRIVATE).edit().clear().apply();
    }
}
