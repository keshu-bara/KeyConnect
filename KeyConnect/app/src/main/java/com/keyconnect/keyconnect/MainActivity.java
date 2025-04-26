package com.keyconnect.keyconnect;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {

    private EditText serverUrlEditText;
    private Button saveButton;
    private Button keyboardSettingsButton;
    private Button selectKeyboardButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize UI components
        serverUrlEditText = findViewById(R.id.serverUrlEditText);
        saveButton = findViewById(R.id.saveButton);
        keyboardSettingsButton = findViewById(R.id.keyboardSettingsButton);
        selectKeyboardButton = findViewById(R.id.selectKeyboardButton);
        
        // Load saved WebSocket URL
        String savedUrl = getSharedPreferences("KeyConnectPrefs", MODE_PRIVATE)
                .getString("serverUrl", "ws://192.168.1.100:8000");
        serverUrlEditText.setText(savedUrl);
        
        // Set up button click listeners
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveServerUrl();
            }
        });
        
        keyboardSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openKeyboardSettings();
            }
        });
        
        selectKeyboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInputMethodPicker();
            }
        });
    }
    
    private void saveServerUrl() {
        String serverUrl = serverUrlEditText.getText().toString().trim();
        
        if (serverUrl.isEmpty()) {
            Toast.makeText(this, "Please enter a server URL", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Save the server URL to SharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("KeyConnectPrefs", MODE_PRIVATE).edit();
        editor.putString("serverUrl", serverUrl);
        editor.apply();
        
        Toast.makeText(this, "Server URL saved", Toast.LENGTH_SHORT).show();
    }
    
    private void openKeyboardSettings() {
        Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
        startActivity(intent);
    }
    
    private void openInputMethodPicker() {
        ((android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
            .showInputMethodPicker();
    }
}
