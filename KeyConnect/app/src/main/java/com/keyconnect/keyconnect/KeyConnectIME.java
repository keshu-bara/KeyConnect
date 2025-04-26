package com.keyconnect.keyconnect;

import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.prefs.Preferences;

public class KeyConnectIME extends InputMethodService {

    private static final String TAG = "KeyConnectIME";
    
    private WebSocketClient webSocketClient;
    private View keyboardView;
    private TextView statusTextView;
    private boolean isConnected = false;
    private String serverUrl = "ws://192.168.1.100:8000"; // Default URL

    @Override
    public void onCreate() {
        super.onCreate();
        // Load saved server URL if available
        serverUrl = getSharedPreferences("KeyConnectPrefs", MODE_PRIVATE)
                .getString("serverUrl", serverUrl);
        connectToServer();
    }

    @Override
    public View onCreateInputView() {
        keyboardView = getLayoutInflater().inflate(R.layout.keyboard_view, null);
        statusTextView = keyboardView.findViewById(R.id.statusTextView);
        updateConnectionStatus();
        return keyboardView;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        if (!isConnected) {
            connectToServer();
        }
    }

    private void connectToServer() {
        try {
            URI uri = new URI(serverUrl);
            
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d(TAG, "WebSocket connection opened");
                    isConnected = true;
                    updateConnectionStatus();
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "Received message: " + message);
                    try {
                        JSONObject jsonObject = new JSONObject(message);
                        String key = jsonObject.getString("key");
                        
                        // Handle different key types
                        if (key.equals("<backspace>")) {
                            getCurrentInputConnection().deleteSurroundingText(1, 0);
                        } else if (key.equals("\n") || key.equals("<enter>")) {
                            getCurrentInputConnection().commitText("\n", 1);
                        } else if (key.equals("<tab>")) {
                            getCurrentInputConnection().commitText("\t", 1);
                        } else if (key.equals("<space>") || key.equals(" ")) {
                            getCurrentInputConnection().commitText(" ", 1);
                        } else if (key.startsWith("<") && key.endsWith(">")) {
                            // Special keys - could implement more as needed
                            Log.d(TAG, "Special key received: " + key);
                        } else {
                            // Regular character input
                            getCurrentInputConnection().commitText(key, 1);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing received message: " + e.getMessage());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "WebSocket connection closed: " + reason);
                    isConnected = false;
                    updateConnectionStatus();
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket error: " + ex.getMessage());
                    isConnected = false;
                    updateConnectionStatus();
                }
            };
            
            webSocketClient.connect();
            
        } catch (URISyntaxException e) {
            Log.e(TAG, "Invalid server URL: " + e.getMessage());
        }
    }

    private void updateConnectionStatus() {
        if (statusTextView != null) {
            statusTextView.post(() -> {
                if (isConnected) {
                    statusTextView.setText("Connected to " + serverUrl);
                    statusTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                } else {
                    statusTextView.setText("Disconnected");
                    statusTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
        super.onDestroy();
    }
}