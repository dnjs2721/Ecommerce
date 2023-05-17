package won.ecommerce.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RedisUtil {
    private final StringRedisTemplate redisTemplate;

    // 키를 통해 값 조회
    public String getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void setData(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // 유효 시간 포함 데이터 저장
    public void setDataExpire(String key, String value, long duration) {
        Duration expireDuration = Duration.ofSeconds(duration);
        redisTemplate.opsForValue().set(key, value, expireDuration);
    }

    // 삭제
    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

    // 인증 코드 검증
    public void validateCode(String key, String value) {
        String data = this.getData(key);
        if (data == null) {
            throw new NoSuchElementException("만료된 인증코드 혹은 잘못된 키 입니다.");
        }
        if (!data.equals(value)) {
            throw new IllegalArgumentException("잘못된 인증코드 입니다.");
        }
        this.deleteData(key);
    }
}
