import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "firebase")
public class FirebaseConfig {
    private String type = System.getenv("FIREBASE_TYPE");
    private String projectId = System.getenv("FIREBASE_PROJECT_ID");
    private String privateKeyId = System.getenv("FIREBASE_PRIVATE_KEY_ID");
    private String privateKey = System.getenv("FIREBASE_PRIVATE_KEY");
    private String clientEmail = System.getenv("FIREBASE_CLIENT_EMAIL");
    private String clientId = System.getenv("FIREBASE_CLIENT_ID");
    private String authUri = System.getenv("FIREBASE_AUTH_URI");
    private String tokenUri = System.getenv("FIREBASE_TOKEN_URI");
    private String authProviderX509CertUrl = System.getenv("FIREBASE_AUTH_PROVIDER_X509_CERT_URL");
    private String clientX509CertUrl = System.getenv("FIREBASE_CLIENT_X509_CERT_URL");
    private String universeDomain = System.getenv("FIREBASE_UNIVERSE_DOMAIN");
}