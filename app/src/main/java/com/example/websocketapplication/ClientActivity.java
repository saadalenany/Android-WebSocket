package com.example.websocketapplication;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ClientActivity extends AppCompatActivity implements DialogCallback {

    private LinearLayout msgList;
    private EditText edMessage;

    private static final int SERVER_PORT = 3003;
    private static final String SERVER_IP = "192.168.1.4";

    private WebSocket ws = null;
    private String username = "";
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        setTitle("Client");
        msgList = findViewById(R.id.msgList);
        edMessage = findViewById(R.id.edMessage);
    }

    public void onClick(View view) {
        ChatMessage chatMessage = new ChatMessage(ChatMessage.MessageType.CHAT, edMessage.getText().toString(), username);
        if (ws != null && isConnected) {
            Gson gson = new Gson();
            Log.d(this.getClass().getSimpleName(), "onClick: " + gson.toJson(chatMessage));
            ws.send(gson.toJson(chatMessage));
            output(chatMessage);
        } else {
            chatMessage.setContent("You're not connected...");
            output(chatMessage);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ws != null && isConnected) {
            ws.close(0, "Application destroyed");
            ws = null;
        }
    }

    private void output(final ChatMessage chatMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msgList.addView(textView(chatMessage));
            }
        });
    }

    @SuppressLint({"SetTextI18n", "RtlHardcoded"})
    private TextView textView(ChatMessage chatMessage) {
        String message = chatMessage.getContent();
        if (null == message || message.trim().isEmpty()) {
            message = "<Empty Message>";
        }
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(params);
        String srcStr;
        if (chatMessage.getType().equals(ChatMessage.MessageType.JOIN)) {
            srcStr = "<b>" + chatMessage.getSender() + "</b> joined.";
            tv.setTextSize(10);
            tv.setTextColor(ContextCompat.getColor(this, R.color.yellow));
            tv.setGravity(Gravity.CENTER);
        } else if (chatMessage.getType().equals(ChatMessage.MessageType.LEAVE)) {
            srcStr = "<b>" + chatMessage.getSender() + "</b> left.";
            tv.setTextSize(10);
            tv.setTextColor(ContextCompat.getColor(this, R.color.red));
            tv.setGravity(Gravity.CENTER);
        } else {
            if (username.equals(chatMessage.getSender())) {
                tv.setTextSize(15);
                tv.setTextColor(ContextCompat.getColor(this, R.color.green));
                tv.setGravity(Gravity.LEFT);
            } else {
                tv.setTextSize(15);
                tv.setTextColor(ContextCompat.getColor(this, R.color.blue));
                tv.setGravity(Gravity.RIGHT);
            }
            srcStr = "<b>" + chatMessage.getSender() + "</b> " + message;
        }
        tv.setText(Html.fromHtml(srcStr));
        tv.setPadding(0, 5, 0, 5);
        return tv;
    }

    public void onStart(View view) {
        FragmentManager fm = getSupportFragmentManager();
        final LoginDialogFragment loginDialogFragment = new LoginDialogFragment();
        loginDialogFragment.show(fm, "Connect to Server");
    }

    private void connectToServer(final String username) {
        Uri.Builder uriBuilder = Uri.parse(String.format("ws://%s:%s/ws/websocket", SERVER_IP, SERVER_PORT)).buildUpon();

        String uri = uriBuilder.build().toString();
        Log.d(this.getClass().getSimpleName(), "connectToServer: URI " + uri);

        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder().url(uri).build();

        SocketListener socketListener = new SocketListener();
        ws = client.newWebSocket(request, socketListener);
        this.username = username;
    }

    @Override
    public void returnData(String data) {
        connectToServer(data);
    }

    private class SocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
            Log.d(this.getClass().getSimpleName(), "onOpen: Connection Opened");
            output(new ChatMessage(ChatMessage.MessageType.JOIN, "", username));
            isConnected = true;
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);

            Gson gson = new Gson();
            ChatMessage chatMessage = gson.fromJson(text, ChatMessage.class);
            Log.d(this.getClass().getSimpleName(), "onMessage: " + text);
            output(chatMessage);
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            super.onClosing(webSocket, code, reason);
            Log.d(this.getClass().getSimpleName(), "onClosing");
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);
            Log.d(this.getClass().getSimpleName(), "onClosed");
            output(new ChatMessage(ChatMessage.MessageType.LEAVE, "", username));
            username = "";
            isConnected = false;
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
            super.onFailure(webSocket, t, response);
            Log.d(this.getClass().getSimpleName(), "onFailure");
            Log.e(this.getClass().getSimpleName(), "Throwable: " + t.getMessage());
            t.printStackTrace();
            isConnected = false;
        }
    }
}
