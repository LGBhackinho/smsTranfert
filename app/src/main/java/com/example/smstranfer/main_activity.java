package com.example.smstranfer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class main_activity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 123; // Code de requête pour les permissions
    private static final int PICK_CONTACT_REQUEST = 456; // Code de requête pour la sélection de contact

    private EditText sourcePhone; // Champ pour le numéro de téléphone source
    private EditText destinationPhone; // Champ pour le numéro de téléphone destinataire
    private Button saveButton; // Bouton pour enregistrer les préférences
    private Button toggleButton; // Bouton pour activer/désactiver la fonctionnalité
    private Button selectContactButton; // Bouton pour sélectionner un contact
    private boolean isActivated = false; // État de la fonctionnalité (activée/désactivée)
    private SharedPreferences sharedPreferences; // Pour stocker les préférences utilisateur

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity); // Chargement de l'interface utilisateur

        // Initialisation des vues
        sourcePhone = findViewById(R.id.source_phone);
        destinationPhone = findViewById(R.id.destination_phone);
        saveButton = findViewById(R.id.save_button);
        toggleButton = findViewById(R.id.toggle_button);
        selectContactButton = findViewById(R.id.select_contact_button);

        // Chargement des préférences sauvegardées
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        loadPreferences();

        // Définition des actions des boutons
        saveButton.setOnClickListener(v -> savePreferences()); // Enregistrer les préférences
        toggleButton.setOnClickListener(v -> toggleActivation()); // Activer ou désactiver
        selectContactButton.setOnClickListener(v -> openContactPicker()); // Ouvrir le sélecteur de contacts

        // Vérifier et demander les permissions nécessaires si l'API est >= M (Marshmallow)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        }
    }

    private void savePreferences() {
        // Sauvegarder les préférences dans SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("source_phone", sourcePhone.getText().toString()); // Numéro source
        editor.putString("destination_phone", destinationPhone.getText().toString()); // Numéro destinataire
        editor.putBoolean("activated", isActivated); // État de la fonctionnalité
        editor.apply(); // Appliquer les changements
        Toast.makeText(this, "Données enregistrées", Toast.LENGTH_SHORT).show(); // Message de confirmation
    }

    private void loadPreferences() {
        // Charger les préférences sauvegardées
        String source = sharedPreferences.getString("source_phone", ""); // Numéro source
        String destination = sharedPreferences.getString("destination_phone", ""); // Numéro destinataire
        isActivated = sharedPreferences.getBoolean("activated", false); // État de la fonctionnalité

        // Mettre à jour les champs avec les valeurs chargées
        sourcePhone.setText(source);
        destinationPhone.setText(destination);
        toggleButton.setText(isActivated ? "Désactiver" : "Activer"); // Mettre à jour le texte du bouton
    }

    private void toggleActivation() {
        // Inverser l'état de la fonctionnalité
        isActivated = !isActivated;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("activated", isActivated); // Sauvegarder le nouvel état
        editor.apply(); // Appliquer les changements
        toggleButton.setText(isActivated ? "Désactiver" : "Activer"); // Mettre à jour le texte du bouton
    }

    private void checkPermissions() {
        // Vérifier les permissions nécessaires
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {

            // Demander les permissions manquantes
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            // Vérifier si toutes les permissions sont accordées
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions accordées
            } else {
                // Permissions refusées
                Toast.makeText(this, "Les permissions SMS et Contacts sont nécessaires pour faire fonctionner l'application.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openContactPicker() {
        // Ouvrir l'application de contacts pour sélectionner un numéro
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Vérifier si la requête est celle pour la sélection de contact et que la sélection a réussi
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER}; // Projection pour obtenir le numéro de téléphone

            if (contactUri != null) {
                ContentResolver contentResolver = getContentResolver();
                try (Cursor cursor = contentResolver.query(contactUri, projection, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        if (numberIndex != -1) { // Vérifier si l'index de colonne est valide
                            String phoneNumber = cursor.getString(numberIndex); // Obtenir le numéro de téléphone
                            destinationPhone.setText(phoneNumber); // Mettre à jour le champ destinataire
                        } else {
                            Toast.makeText(this, "Colonne numéro de téléphone non trouvée", Toast.LENGTH_SHORT).show(); // Message d'erreur
                        }
                    }
                }
            }
        }
    }
}
