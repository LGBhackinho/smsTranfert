package com.example.smstranfer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.telephony.SmsManager;
import android.widget.Toast;

public class smsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isActivated = sharedPreferences.getBoolean("activated", false);
        if (!isActivated) return;

        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                    String source = smsMessage.getDisplayOriginatingAddress();
                    String message = smsMessage.getMessageBody();

                    String sourcePhone = sharedPreferences.getString("source_phone", "");
                    String destinationPhone = sharedPreferences.getString("destination_phone", "");

                    if (sourcePhone.equals(source)) {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(destinationPhone, null, message, null, null);
                        Toast.makeText(context, "Message transféré à " + destinationPhone, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}


