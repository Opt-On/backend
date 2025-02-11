package com.opton.spring_boot.firestore;

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
public class FirestoreProvider {
    // Use the application default credentials
    
    Firestore db = FirestoreClient.getFirestore();

    @Bean
    @SneakyThrows
    public FirebaseApp firebaseApp() {
        FileInputStream serviceAccount = new FileInputStream("spring-boot/firebase-key.json");
		final var firebaseOptions = FirebaseOptions.builder()
				.setCredentials(GoogleCredentials.fromStream(serviceAccount))
				.build();

		return FirebaseApp.initializeApp(firebaseOptions);
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