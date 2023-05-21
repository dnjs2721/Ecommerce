package won.ecommerce.service;

import jakarta.mail.AuthenticationFailedException;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.stereotype.Service;
import won.ecommerce.config.CoolSmsApiConfig;
import won.ecommerce.config.EcommerceConfig;
import won.ecommerce.util.RedisUtil;

@Service
public class MessageService {
    private final DefaultMessageService messageService;
    private final CreateAuthCodeService authCodeService;
    private final RedisUtil redisUtil;
    private final CoolSmsApiConfig apiConfig;
    private final EcommerceConfig ecommerceConfig;

    public MessageService(CreateAuthCodeService authCodeService, RedisUtil redisUtil, CoolSmsApiConfig apiConfig, EcommerceConfig ecommerceConfig) {
        this.authCodeService = authCodeService;
        this.redisUtil = redisUtil;
        this.apiConfig = apiConfig;
        this.ecommerceConfig = ecommerceConfig;
        this.messageService = NurigoApp.INSTANCE.initialize(apiConfig.getApiKey(), apiConfig.getApiSecretKey(), "https://api.coolsms.co.kr");
    }

    /**
     * 인증코드 메시지 전송
     */
    public void sendMessage(String toPNum) {
        Message message = new Message();
        String authCode = authCodeService.createCode();
        String fromPNum = ecommerceConfig.getFromPNum();

        message.setFrom(fromPNum);
        message.setTo(toPNum);
        message.setText("[ECOMMERCE] 인증코드 : " + authCode);

        redisUtil.setDataExpire(toPNum, authCode, 60 * 5L);

        SingleMessageSentResponse response =
                this.messageService.sendOne(new SingleMessageSendingRequest(message));
    }

    /**
     * 인증코드 검증
     */
    public void validateCode(String pNum, String code) throws AuthenticationFailedException {
        redisUtil.validateCode(pNum, code);
    }
}
