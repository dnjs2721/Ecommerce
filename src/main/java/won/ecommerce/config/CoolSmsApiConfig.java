package won.ecommerce.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "coolsms")
@Getter
@Setter
public class CoolSmsApiConfig {
    private String apiKey;
    private String apiSecretKey;
}
