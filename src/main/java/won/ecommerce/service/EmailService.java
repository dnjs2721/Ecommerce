package won.ecommerce.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import won.ecommerce.util.PropertyUtil;
import won.ecommerce.util.RedisUtil;

import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender emailSender;
    private final RedisUtil redisUtil;

    private String authNum; // 랜덤 인증 코드

    // 랜덤 인증 코드 생성
    public void createCode() {
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
        authNum = key.toString();
    }

    // 메일 양식 작성
    public MimeMessage createEmailForm(String email) throws MessagingException, UnsupportedEncodingException {
        createCode(); // 인증 코드 생성
        String setFrom = PropertyUtil.getProperty("fromEmail"); // 보내는 사람
        String title = "E-Commerce 이메일 인증 번호"; // 제목

        MimeMessage message = emailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject(title);
        message.setFrom(setFrom);
        String text="";
        text+= "<div style='margin:100px;'>";
        text+= "<div align='center' style='border:1px solid black; font-family:verdana';>";
        text+= "<h3 style='color:blue;'>인증 코드입니다.</h3>";
        text+= "<div style='font-size:130%'>";
        text+= "CODE : <strong>";
        text+= authNum+"</strong><div><br/> ";
        message.setText(text, "utf-8", "html");

        return message;
    }

    public String sendEmail(String toEmail) throws MessagingException, UnsupportedEncodingException {
        MimeMessage emailForm = createEmailForm(toEmail);
        emailSender.send(emailForm);
        redisUtil.setDataExpire(toEmail, authNum, 60*5L);
        return authNum;
    }

    public void validateCode(String email, String code) {
        String data = redisUtil.getData(email);
        if (data == null) {
            throw new NoSuchElementException("만료된 인증코드 혹은 잘못된 이메일 입니다.");
        }
        if (!data.equals(code)) {
            throw new IllegalArgumentException("잘못된 인증번호 입니다.");
        }
        redisUtil.deleteData(email);
    }
}
