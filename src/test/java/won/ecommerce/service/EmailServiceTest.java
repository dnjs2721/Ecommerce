package won.ecommerce.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EmailServiceTest {
    @Autowired EmailService emailService;
    @Autowired
    StringRedisTemplate redisTemplate;
    private final String KEY = "keyword";

    @Test
    public void sendEmail() throws Exception {
        //given
        String toEmail = "###@###.###";

        emailService.sendAuthCode(toEmail);
    }

    @Test
    public void redisTestSave() throws Exception {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set("testKey", "testValue");
    }

    @Test
    public void redisTestDelete() throws Exception {
        String key = "testKey";
        redisTemplate.delete(key);
    }

    @Test
    public void redisTestSetDataExpire() throws Exception {
        String key = "testKey";
        String value = "testValue";
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Duration expireDuration = Duration.ofSeconds(60 * 5L);// 유효시간 5분 동안 저장
        valueOperations.set(key, value, expireDuration);
    }
    
    @Test
    public void redisTestGetData() throws Exception {
        String key = "testKey";
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        System.out.println("valueOperations.get(key) = " + valueOperations.get(key));
    }
}