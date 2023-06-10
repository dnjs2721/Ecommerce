package won.ecommerce.controller;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import won.ecommerce.config.PortOneApiConfig;
import won.ecommerce.controller.dto.PaymentDto;
import won.ecommerce.service.PaymentService;
import won.ecommerce.service.UserService;

import java.io.IOException;
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

    @ResponseBody
    @GetMapping("/paymentHome")
    public String paymentHome() {
        return "paymentHome";
    }

    @ResponseBody
    @GetMapping("/getToken")
    public ResponseEntity<IamportClient> getToken() throws IamportResponseException, IOException {
        String token = api.getAuth().getResponse().getToken();
        log.info(token);
        return ResponseEntity.ok().body(api);
    }

    //    @PostMapping("/payment")
//    public String payment(@ModelAttribute("formData") PaymentDto dto, Model model) throws IllegalAccessException {
//        userService.payment(dto.getUserId(), dto.getOrderId(), model);
//        return "payment";
//    }
    @GetMapping("/payment/{userId}/{orderId}")
    public String payment(@PathVariable("userId") Long userId, @PathVariable("orderId") Long orderId, Model model) throws IllegalAccessException {
        userService.payment(userId, orderId, model);
        return "payment";
    }

    @ResponseBody
    @PostMapping("verifyIamPort")
    public IamportResponse<Payment> verifyIamPort(@RequestBody Map<String, String> map)
            throws IamportResponseException, IOException {

        String impUid = map.get("imp_uid");
        long orderId = Long.parseLong(map.get("orderId"));
        int amount = Integer.parseInt(map.get("amount"));
        String merchantUid = map.get("merchant_uid");

        IamportResponse<Payment> iamportResponse = api.paymentByImpUid(impUid);
        paymentService.verifyIamPort(iamportResponse, amount, orderId);
        return iamportResponse;
    }

//    @PostMapping("/cancelPayments")
//    public IamportResponse<Payment> cancelPayments(@RequestBody Map<String, String> map) throws IamportResponseException, IOException {
//        IamportResponse<Payment> iamportResponse = null;
//        if (map.containsKey("imp_uid")) {
//            iamportResponse = api.paymentByImpUid(map.get("imp_uid"));
//        } else if (map.containsKey("pgTid")) {
//
//        }
//    }
}
