package won.ecommerce.controller;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import won.ecommerce.config.PortOneApiConfig;
import won.ecommerce.service.UserService;

import java.io.IOException;

@Controller
@Slf4j
@RequestMapping("/api/payment")
public class PaymentController {
    private final IamportClient api;
    private final PortOneApiConfig portOneApiConfig;
    private final UserService userService;

    public PaymentController(PortOneApiConfig portOneApiConfig, UserService userService) {
        this.api = new IamportClient(portOneApiConfig.getApiKey(), portOneApiConfig.getApiSecretKey());
        this.portOneApiConfig = portOneApiConfig;
        this.userService = userService;
    }

    @ResponseBody
    @GetMapping("/getToken")
    public ResponseEntity<IamportClient> getToken() throws IamportResponseException, IOException {
        String token = api.getAuth().getResponse().getToken();
        log.info(token);
        return ResponseEntity.ok().body(api);
    }

    @PostMapping("/order/{userId}/{orderId}")
    public String payment(@PathVariable("userId") Long userId, @PathVariable("orderId") Long orderId) throws IllegalAccessException {
        userService.payment(userId, orderId);
        return "payment";
    }
}
