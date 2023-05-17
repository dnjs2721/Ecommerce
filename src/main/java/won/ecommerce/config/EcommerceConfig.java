package won.ecommerce.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ecommorce")
@Getter
@Setter
public class EcommerceConfig {
    private String fromPNum;
    private String fromEmail;
}
