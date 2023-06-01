package won.ecommerce.service;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import won.ecommerce.config.EcommerceConfig;
import won.ecommerce.util.RedisUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender emailSender;
    private final SpringTemplateEngine templateEngine;
    private final RedisUtil redisUtil;
    private final CreateAuthCodeService authCodeService;
    private final EcommerceConfig ecommerceConfig;

    /**
     * 인증 코드 메일 전송
     */
    public void sendAuthCode(String email) throws MessagingException {
        String authCode = authCodeService.createCode(); // 인증 코드 생성
        String setFrom = ecommerceConfig.getFromEmail(); // 보내는 사람
        String title = "E-Commerce 이메일 인증 번호"; // 제목

        MimeMessage message = emailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject(title);
        message.setFrom(setFrom);
        message.setText(setContextByAuthCode(authCode), "utf-8", "html");

        // 메시지 전송
        emailSender.send(message);
        // 레디스 등록 제한시간 5분
        redisUtil.setDataExpire(email, authCode, 60*5L);
    }

    /**
     * 인증번호 검증
     */
    public void validateCode(String email, String code) throws AuthenticationFailedException {
        redisUtil.validateCode(email, code);
    }

    /**
     * 카테고리 내 상품의 판매자에게 경고 메일 전송
     */
    public void sendCategoryWarningMail(String email, String categoryName, String sellerName, List<String> itemsName) throws MessagingException {
        String setFrom = ecommerceConfig.getFromEmail();
        String title = "[E-Commerce] 카테고리 변경으로 인한 경고 메일";

        MimeMessage message = emailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject(title);
        message.setFrom(setFrom);
        message.setText(setContextByWarning(categoryName, sellerName, itemsName.toString()), "utf-8", "html");

        emailSender.send(message);
    }

    /**
     * 카테고리 내 상품의 판매자에게 안내 메일 전송
     */
    public void sendCategoryNoticeMail(String email, String categoryName, String changeCategoryName, String sellerName, List<String> itemNames) throws MessagingException {

        String setFrom = ecommerceConfig.getFromEmail();
        String title = "[E-Commerce] 카테고리 변경으로 인한 안내 메일";

        MimeMessage message = emailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject(title);
        message.setFrom(setFrom);
        message.setText(setContextByNotice(categoryName, changeCategoryName, sellerName, itemNames.toString()), "utf-8", "html");

        emailSender.send(message);
    }

    /**
     * 타임리프 context 설정 - 인증번호
     */
    public String setContextByAuthCode(String code) {
        Context context = new Context();
        context.setVariable("code", code);
        return templateEngine.process("mail", context);
    }

    public String setContextByWarning(String categoryName, String sellerName, String itemsName) {
        Context context = new Context();
        context.setVariable("categoryName", categoryName);
        context.setVariable("sellerName", sellerName);
        context.setVariable("item", itemsName);
        return templateEngine.process("warning", context);
    }

    private String setContextByNotice(String categoryName, String changeCategoryName, String sellerName, String itemNames) {
        Context context = new Context();
        context.setVariable("categoryName", categoryName);
        context.setVariable("changeCategoryName", changeCategoryName);
        context.setVariable("sellerName", sellerName);
        context.setVariable("item", itemNames);
        return templateEngine.process("notice", context);
    }
}
