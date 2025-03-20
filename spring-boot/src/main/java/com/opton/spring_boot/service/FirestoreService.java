package com.opton.spring_boot.service;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

public class FirestoreService {
    private static Firestore firestore;

    public static Firestore getFirestore() {
        if (firestore == null) {
            firestore = FirestoreOptions.getDefaultInstance().getService();
        }
        return firestore;
    }
}
