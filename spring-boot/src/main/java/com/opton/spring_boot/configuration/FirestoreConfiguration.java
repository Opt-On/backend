package com.opton.spring_boot.configuration;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.SneakyThrows;

@Configuration
public class FirestoreConfiguration {

    @Bean
    @SneakyThrows
    public FirebaseApp firebaseApp() {
        List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
        if (!firebaseApps.isEmpty()) {
            return firebaseApps.get(0); // Return existing instance
        }
        Dotenv dotenv = Dotenv.load();
        
        String privateKey = dotenv.get("FIREBASE_PRIVATE_KEY").replace("\\n", "\n");
        String privateKeyId = dotenv.get("FIREBASE_PRIVATE_KEY_ID");
        String clientEmail = dotenv.get("FIREBASE_CLIENT_EMAIL");
        String projectId = dotenv.get("FIREBASE_PROJECT_ID");
        String clientId = dotenv.get("FIREBASE_CLIENT_ID");

        GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream((
            "{"
                + "\"type\": \"service_account\","
                + "\"project_id\": \"" + projectId + "\","
                + "\"private_key_id\": \"" + privateKeyId +  "\","
                + "\"private_key\": \"" + privateKey + "\","
                + "\"client_email\": \"" + clientEmail + "\","
                + "\"client_id\": \"" + clientId + "\","
                + "\"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\","
                + "\"token_uri\": \"https://oauth2.googleapis.com/token\","
                + "\"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\","
                + "\"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-fbsvc%40opton-5596f.iam.gserviceaccount.com\","
                + "\"universe_domain\": \"googleapis.com\""
                + "}"
        ).getBytes(StandardCharsets.UTF_8)));

        FirebaseOptions firebaseOptions = FirebaseOptions.builder()
            .setCredentials(credentials)
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