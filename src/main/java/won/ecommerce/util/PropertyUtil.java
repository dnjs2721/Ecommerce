package won.ecommerce.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@RequiredArgsConstructor
@PropertySource("classpath:properties/email.properties")
public class PropertyUtil implements EnvironmentAware {
    private static Environment environment;

    @Override
    public void setEnvironment(final Environment environment) {
        PropertyUtil.environment = environment;
    }

    public static String getProperty(String key) {
        return environment.getProperty(key);
    }
}