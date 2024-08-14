package com.example.smstranfer;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class main_activity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 123;

    private EditText sourcePhone;
    private EditText destinationPhone;
    private Button saveButton;
    private Button toggleButton;
    private boolean isActivated = false;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        sourcePhone = findViewById(R.id.source_phone);
        destinationPhone = findViewById(R.id.destination_phone);
        saveButton = findViewById(R.id.save_button);
        toggleButton = findViewById(R.id.toggle_button);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        loadPreferences();

        saveButton.setOnClickListener(v -> savePreferences());
        toggleButton.setOnClickListener(v -> toggleActivation());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        }
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("source_phone", sourcePhone.getText().toString());
        editor.putString("destination_phone", destinationPhone.getText().toString());
        editor.putBoolean("activated", isActivated);
        editor.apply();
        Toast.makeText(this, "Données enregistrées", Toast.LENGTH_SHORT).show();
    }

    private void loadPreferences() {
        String source = sharedPreferences.getString("source_phone", "");
        String destination = sharedPreferences.getString("destination_phone", "");
        isActivated = sharedPreferences.getBoolean("activated", false);

        sourcePhone.setText(source);
        destinationPhone.setText(destination);
        toggleButton.setText(isActivated ? "Désactiver" : "Activer");
    }

    private void toggleActivation() {
        isActivated = !isActivated;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("activated", isActivated);
        editor.apply();
        toggleButton.setText(isActivated ? "Désactiver" : "Activer");
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS},
                    PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, proceed with the app functionality
            } else {
                Toast.makeText(this, "Les permissions SMS sont nécessaires pour faire fonctionner l'application.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
