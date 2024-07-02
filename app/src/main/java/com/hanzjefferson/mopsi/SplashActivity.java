package com.hanzjefferson.mopsi;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonObject;
import com.hanzjefferson.mopsi.databinding.ActivitySplashBinding;
import com.hanzjefferson.mopsi.databinding.SheetBotAuthBinding;
import com.hanzjefferson.mopsi.databinding.SheetLoginBinding;
import com.hanzjefferson.mopsi.models.Profile;
import com.hanzjefferson.mopsi.models.Response;
import com.hanzjefferson.mopsi.utils.AccountUtils;
import com.hanzjefferson.mopsi.utils.ApiServiceUtils;
import com.hanzjefferson.mopsi.utils.SocketUtils;

public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;
    private SheetLoginBinding loginBinding;
    private SheetBotAuthBinding botAuthBinding;
    private Intent mainActivityIntent;
    private BottomSheetDialog bottomSheetDialog;
    private boolean isBotAuth = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        loginBinding = SheetLoginBinding.inflate(getLayoutInflater());
        botAuthBinding = SheetBotAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ApiServiceUtils.BASE_URL = getString(R.string.base_route);

        mainActivityIntent = new Intent(this, MainActivity.class);
        bottomSheetDialog = new BottomSheetDialog(SplashActivity.this);

        loginBinding.inputUserid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) loginBinding.buttonLogin.setEnabled(false);
                else loginBinding.buttonLogin.setEnabled(!loginBinding.inputPin.getText().toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        loginBinding.inputPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) loginBinding.buttonLogin.setEnabled(false);
                else loginBinding.buttonLogin.setEnabled(!loginBinding.inputUserid.getText().toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        loginBinding.buttonLogin.setOnClickListener(v -> {
            String id = loginBinding.inputUserid.getText().toString();
            String pin= loginBinding.inputPin.getText().toString();

            binding.spinKitView.setVisibility(View.VISIBLE);
            bottomSheetDialog.dismiss();
            ApiServiceUtils.login(id, pin, new ApiServiceUtils.CallbackListener<JsonObject>() {

                @Override
                public void onResponse(Response<JsonObject> response) {
                    binding.spinKitView.setVisibility(View.GONE);
                    if (response.isSuccess()){
                        AccountUtils.saveState(response.data.get("token").getAsString());
                        onSplashed();
                    } else {
                        new MaterialAlertDialogBuilder(SplashActivity.this)
                                .setTitle("Kesalahan!")
                                .setMessage(response.message)
                                .setCancelable(false)
                                .setPositiveButton("Kembali", (dialog, which) -> {
                                    bottomSheetDialog.show();
                                })
                                .show();
                    }
                }

                @Override
                public void onError(VolleyError error) {
                    binding.imgIcon.setVisibility(View.GONE);
                    new MaterialAlertDialogBuilder(SplashActivity.this)
                            .setTitle("Kesalahan Klien!")
                            .setMessage(error.getMessage())
                            .setCancelable(false)
                            .setPositiveButton("Kembali", (dialog, which) -> {
                                bottomSheetDialog.show();
                            })
                            .show();
                }
            });
        });

        botAuthBinding.buttonSend.setOnClickListener(v -> {
            if (AccountUtils.getProfile() == null){
                Toast.makeText(getApplicationContext(), "Anda belum login!", Toast.LENGTH_LONG).show();
                return;
            }
            String url = getString(R.string.twilio_api) + "&text=" + Uri.encode("auth "+String.valueOf(AccountUtils.getProfile().id+" "+AccountUtils.getToken()));

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        AccountUtils.init(this);
        ApiServiceUtils.init(this);
        
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                SocketUtils.connect(args -> {
                    if (AccountUtils.getProfile() == null) runOnUiThread(this::onSplashed);
                });
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isBotAuth) onSplashed();
    }

    public void onSplashed(){
        isBotAuth = false;
        if (AccountUtils.isLoggedIn()){
            binding.spinKitView.setVisibility(View.VISIBLE);
            AccountUtils.fetchProfile(new ApiServiceUtils.CallbackListener<Profile>() {
                @Override
                public void onResponse(Response<Profile> response) {
                    binding.spinKitView.setVisibility(View.GONE);
                    if (response.isSuccess()){
                        if (response.data.phone_num != null && !response.data.phone_num.isEmpty()){
                            startActivity(mainActivityIntent);
                            finish();
                        }else{
                            isBotAuth = true;
                            bottomSheetDialog.setContentView(botAuthBinding.getRoot());
                            bottomSheetDialog.setCancelable(false);
                            bottomSheetDialog.show();
                        }
                    } else {
                        new MaterialAlertDialogBuilder(SplashActivity.this)
                                .setTitle("Kesalahan!")
                                .setMessage(response.message)
                                .setCancelable(false)
                                .setPositiveButton("Coba lagi", (dialog, which) -> {
                                    AccountUtils.fetchProfile(this);
                                })
                                .setNegativeButton("Login ulang", (dialog, which) -> {
                                    bottomSheetDialog.setContentView(loginBinding.getRoot());
                                    bottomSheetDialog.setCancelable(false);
                                    bottomSheetDialog.show();
                                })
                                .show();
                    }
                }

                @Override
                public void onError(VolleyError error) {
                    binding.spinKitView.setVisibility(View.GONE);
                    new MaterialAlertDialogBuilder(SplashActivity.this)
                            .setTitle("Kesalahan Klien!")
                            .setMessage(error.getMessage())
                            .setCancelable(false)
                            .setPositiveButton("Coba lagi", (dialog, which) -> {
                                AccountUtils.fetchProfile(this);
                            })
                            .setNegativeButton("Login ulang", (dialog, which) -> {
                                bottomSheetDialog.setContentView(loginBinding.getRoot());
                                bottomSheetDialog.setCancelable(false);
                                bottomSheetDialog.show();
                            })
                            .show();
                }
            });
        }else{
            bottomSheetDialog.setContentView(loginBinding.getRoot());
            bottomSheetDialog.setCancelable(false);
            bottomSheetDialog.show();
        }
    }

    @Override
    public void finish() {
        Animatoo.INSTANCE.animateSlideLeft(SplashActivity.this);
        super.finish();
    }
}