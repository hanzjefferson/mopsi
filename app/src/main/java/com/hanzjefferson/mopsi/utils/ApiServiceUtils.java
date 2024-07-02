package com.hanzjefferson.mopsi.utils;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.hanzjefferson.mopsi.models.Announcement;
import com.hanzjefferson.mopsi.models.Kelas;
import com.hanzjefferson.mopsi.models.Poin;
import com.hanzjefferson.mopsi.models.Profile;
import com.hanzjefferson.mopsi.models.Rekapitulasi;
import com.hanzjefferson.mopsi.models.Response;

import java.util.HashMap;
import java.util.Map;

public class ApiServiceUtils {
    public static String BASE_URL = "http://192.168.1.12:5000/";

    public interface CallbackListener<T>{
        void onResponse(Response<T> response);
        void onError(VolleyError error);
    }

    public static class RequestBuilder<T>{
        private Class<T> responseType;
        private int method = 0;
        private String url = ApiServiceUtils.BASE_URL;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> body = new HashMap<>();
        private CallbackListener<T> listener;

        public static <T> RequestBuilder<T> newBuilder(Class<T> responseType){
            RequestBuilder<T> requestBuilder = new RequestBuilder<>();
            requestBuilder.responseType = responseType;

            return requestBuilder;
        }

        public RequestBuilder<T> method(int method){
            this.method = method;
            return this;
        }

        public RequestBuilder<T> listener(CallbackListener<T> listener) {
            this.listener = listener;
            return this;
        }

        public RequestBuilder<T> endpoint(String endpoint){
            this.url += endpoint;
            return this;
        }

        public RequestBuilder<T> headers(Map<String, String> headers){
            this.headers = headers;
            return this;
        }

        public RequestBuilder<T> body(Map<String, String> body){
            this.body = body;
            return this;
        }

        public StringRequest build(){
            return new StringRequest(method, url, response -> {
                if (listener == null) return;
                Response<T> modelResponse;
                try {
                    JsonObject responseObject = JsonParser.parseString(response).getAsJsonObject();
                    if (responseObject.get("data").isJsonNull()) responseObject.remove("data");
                    modelResponse = new Gson().fromJson(responseObject, TypeToken.getParameterized(Response.class, responseType).getType());
                    listener.onResponse(modelResponse);
                } catch (JsonSyntaxException e) {
                    if (e != null) e.printStackTrace();
                    modelResponse = new Response<>();
                    modelResponse.code = 500;
                    modelResponse.message = "Kesalahan server!";
                    listener.onResponse(modelResponse);
                }
            }, error -> {
                if (listener == null) return;
                listener.onError(error);
            }){
                @Override
                public int getMethod() {
                    return method;
                }

                @Override
                public Map<String, String> getHeaders(){
                    return headers;
                }

                @Override
                protected Map<String, String> getParams(){
                    return body;
                }
            };
        }
    }

    private Context context;

    public static RequestQueue mainRequestQueue;

    public static void init(Context context){
        mainRequestQueue = ApiServiceUtils.newQueue(context);
    }

    public static RequestQueue newQueue(Context context) {
        return Volley.newRequestQueue(context);
    }

    public static Map<String, String> getAuthorizationHeader(String token){
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        return headers;
    }

    public static void addRequest(StringRequest request){
        mainRequestQueue.add(request);
    }

    public static void login(String userId, String pin, CallbackListener<JsonObject> listener){
        Map<String, String> body = new HashMap<>();
        body.put("id", userId);
        body.put("pin", pin);

        addRequest(RequestBuilder.newBuilder(JsonObject.class)
                .endpoint("login")
                .method(Request.Method.POST)
                .body(body)
                .listener(listener)
                .build());
    }

    public static void logout(CallbackListener<JsonObject> listener){
        addRequest(RequestBuilder.newBuilder(JsonObject.class)
                .method(Request.Method.GET)
                .endpoint("logout")
                .headers(getAuthorizationHeader(AccountUtils.getToken()))
                .listener(listener)
                .build());
    }

    public static void call(CallbackListener<JsonObject> callbackListener, int[] ids, long date, String tempat, String perihal) {
        Map<String, String> body = new HashMap<>();
        body.put("ids", new Gson().toJson(ids));
        body.put("tanggal", String.valueOf(date/1000));
        body.put("tempat", tempat);
        body.put("perihal", perihal);

        addRequest(RequestBuilder.newBuilder(JsonObject.class)
                .method(Request.Method.POST)
                .endpoint("call")
                .headers(getAuthorizationHeader(AccountUtils.getToken()))
                .body(body)
                .listener(callbackListener)
                .build());
    }

    public static void newRekap(CallbackListener<JsonObject> listener){
        addRequest(RequestBuilder.newBuilder(JsonObject.class)
                .method(Request.Method.PATCH)
                .endpoint("rekap/poin")
                .headers(getAuthorizationHeader(AccountUtils.getToken()))
                .listener(listener)
                .build());
    }

    public static void saveRekap(CallbackListener<JsonObject> listener, int id, String unique, Poin[] poinArr){
        Map<String, String> body = new HashMap<>();
        body.put("id", String.valueOf(id));
        body.put("unique", unique);
        body.put("poin", new Gson().toJson(poinArr));

        addRequest(RequestBuilder.newBuilder(JsonObject.class)
                .method(Request.Method.POST)
                .endpoint("rekap/poin")
                .headers(getAuthorizationHeader(AccountUtils.getToken()))
                .body(body)
                .listener(listener)
                .build());
    }

    public static void writeRekap(CallbackListener<JsonObject> listener){
        Map<String, String> body = new HashMap<>();
        body.put("action", "write");

        addRequest(RequestBuilder.newBuilder(JsonObject.class)
                .method(Request.Method.PATCH)
                .endpoint("rekap/poin")
                .headers(getAuthorizationHeader(AccountUtils.getToken()))
                .body(body)
                .listener(listener)
                .build());
    }

    public static void deleteRekap(CallbackListener<JsonObject> listener){
        addRequest(RequestBuilder.newBuilder(JsonObject.class)
                .method(Request.Method.DELETE)
                .endpoint("rekap/poin")
                .headers(getAuthorizationHeader(AccountUtils.getToken()))
                .listener(listener)
                .build());
    }

    public static void deleteRekap(CallbackListener<JsonObject> listener, String tanggal){
        addRequest(RequestBuilder.newBuilder(JsonObject.class)
                .method(Request.Method.DELETE)
                .endpoint("rekap/poin/"+tanggal)
                .headers(getAuthorizationHeader(AccountUtils.getToken()))
                .listener(listener)
                .build());
    }

    public static void profile(CallbackListener<Profile> listener){
        addRequest(RequestBuilder.newBuilder(Profile.class)
                .endpoint("profile")
                .headers(getAuthorizationHeader(AccountUtils.getToken()))
                .listener(listener)
                .build());
    }

    public static void templatePoin(CallbackListener<Poin[]> listener){
        addRequest(RequestBuilder.newBuilder(Poin[].class)
                .endpoint("template/poin")
                .headers(getAuthorizationHeader(AccountUtils.getToken()))
                .listener(listener)
                .build());
    }

    public static void getAnnouncement(CallbackListener<Announcement[]> listener){
        addRequest(RequestBuilder.newBuilder(Announcement[].class)
                .endpoint("announcements")
                .headers(getAuthorizationHeader(AccountUtils.getToken()))
                .listener(listener)
                .build());
    }

    public static void getClasses(CallbackListener<Kelas[]> listener){
        addRequest(RequestBuilder.newBuilder(Kelas[].class)
                .endpoint("class")
                .headers(getAuthorizationHeader(AccountUtils.getToken()))
                .listener(listener)
                .build());
    }

    public static void getRekap(CallbackListener<Rekapitulasi> listener){
        getRekap(-1, listener);
    }

    public static void getRekap(int id, CallbackListener<Rekapitulasi> listener){
        addRequest(RequestBuilder.newBuilder(Rekapitulasi.class)
                .endpoint("rekap"+(id != -1? "/"+id : ""))
                .headers(getAuthorizationHeader(AccountUtils.getToken()))
                .listener(listener)
                .build());
    }
}
