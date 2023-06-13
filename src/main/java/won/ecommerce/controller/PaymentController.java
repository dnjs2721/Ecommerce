package won.ecommerce.controller;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import won.ecommerce.config.PortOneApiConfig;
import won.ecommerce.controller.dto.paymentDto.CancelPaymentDto;
import won.ecommerce.controller.dto.paymentDto.PaymentDto;
import won.ecommerce.exception.VerifyIamportException;
import won.ecommerce.service.PaymentService;
import won.ecommerce.service.UserService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@Controller
@Slf4j
public class PaymentController {
    private final IamportClient api;
    private final PortOneApiConfig portOneApiConfig;
    private final UserService userService;
    private final PaymentService paymentService;

    public PaymentController(PortOneApiConfig portOneApiConfig, UserService userService, PaymentService paymentService) {
        this.api = new IamportClient(portOneApiConfig.getApiKey(), portOneApiConfig.getApiSecretKey());
        this.portOneApiConfig = portOneApiConfig;
        this.userService = userService;
        this.paymentService = paymentService;
    }

    /**
     * 결제, 취소 홈
     */
    @GetMapping("/paymentHome")
    public String paymentHome() {
        return "paymentHome";
    }

    /**
     * 결제
     */
    @PostMapping("/payment")
    public String payment(@ModelAttribute("formData1") PaymentDto dto, Model model) {
        try {
            userService.payment(dto.getPaymentUserId(), dto.getOrderId(), model);
            return "payment";
        } catch (Exception e) {
            String errorMessage = e.getMessage(); // 에러 메시지 가져오기
            model.addAttribute("errorMessage", errorMessage);
            return "errorPage";
        }
    }

    /**
     * 결제검증
     */
    @ResponseBody
    @PostMapping("verifyIamPort")
    public IamportResponse<Payment> verifyIamPort(@RequestBody Map<String, String> map)
            throws IamportResponseException, IOException {

        String impUid = map.get("imp_uid");
        long orderId = Long.parseLong(map.get("orderId"));
        int amount = Integer.parseInt(map.get("amount"));

        IamportResponse<Payment> iamportResponse = api.paymentByImpUid(impUid);
        paymentService.verifyIamPort(iamportResponse, amount, orderId, impUid);
        return iamportResponse;
    }

    /**
     * 주문취소, 결제취소 버튼
     */
    @PostMapping("/cancelOrderHome")
    public String cancelOrderHome(@ModelAttribute("formData2") CancelPaymentDto dto, Model model) {
        try {
            userService.cancelOrderHome(dto.getCancelPaymentUserId(), dto.getOrderItemId(), model);
            return "cancelOrderHome";
        } catch (Exception e) {
            String errorMessage = e.getMessage(); // 에러 메시지 가져오기
            model.addAttribute("errorMessage", errorMessage);
            return "errorPage";
        }
    }

    /**
     * 주문 취소
     */
    @PostMapping("/cancelOrder/{orderItemId}")
    public String cancelOrder(@PathVariable("orderItemId") Long orderItemId) {
        paymentService.cancelOrderItem(orderItemId);
        return "redirect:/paymentHome";
    }

    /**
     * 결제 취소 로직
     */
    @ResponseBody
    @PostMapping("/cancelPayments")
    public IamportResponse<Payment> cancelPayments(@RequestBody Map<String, String> map) throws IamportResponseException, IOException {
        IamportResponse<Payment> iamportResponse = new IamportResponse<>();
        Long orderItemId = null;
        if (map.containsKey("impUid")) {
            iamportResponse = api.paymentByImpUid(map.get("impUid"));
        } else if (map.containsKey("paymentUid")){
            iamportResponse = api.paymentByImpUid(map.get("paymentUid"));
            orderItemId = Long.parseLong(map.get("orderItemId"));
            int orderItemTotalPrice = paymentService.getOrderItemTotalPrice(orderItemId);
            if (Integer.parseInt(map.get("checkSum")) != orderItemTotalPrice) {
                throw new VerifyIamportException("환불금액 위/변조. 환불금액이 일치하지 않습니다.");
            }
        }
        CancelData data = cancelData(iamportResponse, map);
        if (orderItemId != null) {
            paymentService.cancelOrderItem(orderItemId);
        }
        return api.cancelPaymentByImpUid(data);
    }

    // 취소 데이터 생성
    public CancelData cancelData(IamportResponse<Payment> iamportResponse, Map<String, String> map) {
        String reason = map.get("reason");
        BigDecimal checkSum = new BigDecimal(Integer.parseInt(map.get("checkSum")));
        String refundHolder = map.get("refundHolder");

        CancelData data = new CancelData(iamportResponse.getResponse().getImpUid(), true, checkSum);
        data.setReason(reason);
        data.setRefund_holder(refundHolder);

        return data;
    }
}
