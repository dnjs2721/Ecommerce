package won.ecommerce.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "portone")
@Getter
@Setter
public class PortOneApiConfig {
    private String apiKey;
    private String apiSecretKey;
    private String identificationCode;
}
