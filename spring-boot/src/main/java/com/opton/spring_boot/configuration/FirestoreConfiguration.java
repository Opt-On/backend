package com.opton.spring_boot.configuration;

import java.io.FileInputStream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;

import lombok.SneakyThrows;

@Configuration
public class FirestoreConfiguration {

    @Bean
    @SneakyThrows
    public FirebaseApp firebaseApp() {
        if (FirebaseApp.getApps().isEmpty()){
            FileInputStream serviceAccount = new FileInputStream("firebase-key.json");
            final var firebaseOptions = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            return FirebaseApp.initializeApp(firebaseOptions);
        }
        
        return FirebaseApp.getInstance();
    }

    @Bean
    public Firestore firestore(final FirebaseApp firebaseApp){
        return FirestoreClient.getFirestore(firebaseApp);
    }

    @Bean
	public FirebaseAuth firebaseAuth(final FirebaseApp firebaseApp) {
		return FirebaseAuth.getInstance(firebaseApp);
	}
}