package com.hanzjefferson.mopsi.utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketUtils {
    public interface SocketListener<T> {
        void onEvent(T data);
    }

    private static Socket socket;

    public static Socket getSocket() {
        return socket;
    }

    public static void connect(Emitter.Listener listener) {
        try {
            socket = IO.socket(ApiServiceUtils.BASE_URL);
            socket.connect();
            if (listener != null) socket.on(Socket.EVENT_CONNECT, listener);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void disconnect() {
        socket.disconnect();
    }

    public static <T> void on(String event, Type type, SocketListener<T> listener) {
        if (socket == null) connect(null);
        socket.on(event, args -> {
            if (listener == null) return;
            try {
                T model = new Gson().fromJson((String) args[0], type);
                listener.onEvent(model);
            } catch (JsonSyntaxException e) {
                if (e != null) e.printStackTrace();
                listener.onEvent(null);
            }
        });
    }

    public static void on(String event, SocketListener<Object[]> listener){
        if (socket == null) connect(null);
        socket.on(event, args -> {
            if (listener == null) return;
            listener.onEvent(args);
        });
    }
}
