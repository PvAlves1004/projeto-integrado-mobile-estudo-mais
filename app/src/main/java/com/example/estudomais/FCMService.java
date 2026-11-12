package com.example.estudomais;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.util.HashMap;
import java.util.Map;

// Keeps FCM token in sync with Firestore. Notification delivery is handled locally by WorkManager.
public class FCMService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        Map<String, Object> update = new HashMap<>();
        update.put("fcmToken", token);
        FirebaseFirestore.getInstance()
                .collection("usuarios").document(uid)
                .update(update);
    }
}
