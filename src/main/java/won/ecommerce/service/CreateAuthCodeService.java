package won.ecommerce.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class CreateAuthCodeService {

    public String createCode() {
        Random random = new Random();
        StringBuffer key = new StringBuffer();

        for (int i = 0; i <8; i++) {
            int index = random.nextInt(3); // 0 ~ 2 랜덤 index -> case 문

            switch (index) {
                case 0 -> key.append((char) ((int) random.nextInt(26) + 97)); // 대문자
                case 1 -> key.append((char) ((int) random.nextInt(26) + 65)); // 소문자
                case 2 -> key.append(random.nextInt(9)); // 숫자
            }
        }
        return key.toString();
    }
}
