package com.example.websocketapplication;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientActivity extends AppCompatActivity implements DialogCallback {

    private LinearLayout msgList;
    private EditText edMessage;

    private static final int SERVER_PORT = 3003;
    private static final String SERVER_IP = "192.168.1.4";

    private WebSocket ws = null;
    private String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        setTitle("Client");
        msgList = findViewById(R.id.msgList);
        edMessage = findViewById(R.id.edMessage);
    }

    public void onClick(View view) {
        if (ws.isOpen()) {
            ws.sendText(edMessage.getText().toString());
        } else {
            output("You're not connected...");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ws != null) {
            ws.disconnect();
            ws = null;
        }
    }

    private void output(final String txt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msgList.addView(textView(txt));
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private TextView textView(String message) {
        if (null == message || message.trim().isEmpty()) {
            message = "<Empty Message>";
        }
        TextView tv = new TextView(this);
        tv.setTextColor(ContextCompat.getColor(this, R.color.blue));
        tv.setText(username+" "+message);
        tv.setTextSize(10);
        tv.setPadding(0, 5, 0, 0);
        return tv;
    }

    public void onStart(View view) {
        FragmentManager fm = getSupportFragmentManager();
        final LoginDialogFragment loginDialogFragment = new LoginDialogFragment();
        loginDialogFragment.show(fm, "Connect to Server");
    }

    private void connectToServer(final String username){
        Uri.Builder uriBuilder = Uri.parse(String.format("ws://%s:%s/doLogin",SERVER_IP,SERVER_PORT)).buildUpon();
        uriBuilder.appendQueryParameter("username",username);

        String uri = uriBuilder.build().toString();

        AsyncTaskRunner runner = new AsyncTaskRunner(uri);
        runner.execute();
        this.username = username;
    }

    @Override
    public void returnData(String data) {
        connectToServer(data);
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskRunner extends AsyncTask<Void, Void, String> {

        String uri;
        AsyncTaskRunner(String uri) {
            this.uri = uri;
        }

        @Override
        protected String doInBackground(Void... params) {
            String returnedResult = "Connection Error";
            // Create a WebSocket factory and set 5000 milliseconds as a timeout
            // value for socket connection.
            WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(5000);

            // Create a WebSocket. The timeout value set above is used.
            try {
                ws = factory.createSocket(uri).connect();
                if (!ws.isOpen()) {
                    returnedResult = "Connection failed";
                    output("Connection Failed...");
                } else {
                    returnedResult = "Connection successful";
                    output("Connection successful...");
                }

                ws.addListener(new WebSocketAdapter() {
                    @Override
                    public void onTextMessage(WebSocket websocket, String message) throws Exception {
                        Log.d("onTextMessage", message);
                        output(message);
                    }
                });

                ws.connectAsynchronously();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (WebSocketException e) {
                e.printStackTrace();
            }
            return returnedResult;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("AsyncTask",result);
        }

        @Override
        protected void onPreExecute() {
            Log.d("AsyncTask","Initiating Connection...");
        }
    }
}
