## 💵 Payment PAGE

### 결제
- Payment Page
    ```javascript
     <script th:inline="javascript">
        /*<![CDATA[*/
        $(document).ready(function(){
            $("#PortOnePayment").click(function(){
                payment(); //버튼 클릭하면 호출
            });
        })

        function payment() {
            var IMP = window.IMP;
            IMP.init([[${identificationCode}]]);
            IMP.request_pay({
                pg: "kakaopay." + [[${CID}]],
                pay_method: "card",
                merchant_uid: "merchant_" + new Date().getTime(),   // 주문번호
                name: "[(${itemsName})]",
                amount: [[${totalPrice}]],                         // 숫자 타입
                buyer_email: [[${buyerEmail}]],
                buyer_tel : [[${buyerPNUm}]],
                buyer_name: "[(${buyerName})]"
            }, function (rsp) {
                if (rsp.success) {
                    // 결제 성공 시: 결제 승인 또는 가상계좌 발급에 성공한 경우
                    let  data = {
                        imp_uid: rsp.imp_uid,
                        amount: rsp.paid_amount.toString(),
                        orderId : "[(${orderId})]"
                    }
                    $.ajax({
                        type:"POST",
                        url:"/verifyIamPort",
                        data:JSON.stringify(data),
                        contentType:"application/json; charset=utf-8",
                        dataType:"json",
                        success: function (result) {
                            alert("성공적으로 결제되었습니다.");
                            window.location.replace("/paymentHome");
                        },
                        error: function (result) {
                            alert(result.responseText);
                            cancelPayments(rsp);
                        },
                    })
                } else {
                    alert(rsp.error_msg);
                    window.location.replace("/paymentHome");
                    console.log(rsp);
                }
            });
        }

        function cancelPayments(temp) {
            let data = {
                impUid: temp.imp_uid,
                reason: "결제금액 위/변조. 결제금액이 일치하지 않습니다.",
                checksum: temp.amount,
                refundHolder: temp.buyer_name
            };
            $.ajax({
                type: "POST",
                url: "/cancelPayments",
                data: JSON.stringify(data),
                contentType: "application/json; charset=utf-8",
                success: function (result) {
                    alert("결제가 성공적으로 취소되었습니다");
                    window.location.replace("/paymentHome");
                },
                error: function (result) {
                    alert("결제 취소 중 오류가 발생했습니다: " + result.responseText);
                    window.location.replace("/paymentHome");
                },
            });
        }
        /*]]>*/
    </script>
    ```

```
결제 요청이 들어오면 function payment()를 실행한다.

payment() 실패시 : alert 로 에러 메세지를 반환하고 /paymentHome 으로 이동한다.

payment() 성공시 : ajax 결제검증 실행 (Post  /verifyIamPort)
결제검증 :
    data : 결제 ID, 결제 금액, 주문 고유번호
    
    ajax 성공시 : alert 로 성공 메세지를 반환하고 /paymentHome 으로 이동한다.
    ajax 실패시 : alert 로 에러 메세지를 반환하고 결제취소를 실행 (function cancelPayments())

cancelPayments() 실행시 : ajax 결제취소 실행 (POST /cancelPayments)
결제취소 :
    data : 결제 ID, 취소 이유, 취소 금액, 구매자 이름
    
    ajax 성공시 : alert 로 성공 메세지를 반환하고 /paymentHome 으로 이동한다.
    ajax 실패시 : alert 로 에러 메세지를 반환하고 결제취소를 실행 (function cancelPayments())
```


### 결제
- Controller
    ```java
    PostMapping("/payment")
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
    ```

- PaymentDto
    ```java
    @Data
    public class PaymentDto {
        Long paymentUserId;
        Long orderId;
    }
    ```

- Service
  - userService.payment
    ```java
    @Transactional
    public void payment(Long userId, Long orderId, Model model) throws IllegalAccessException {
        User user = checkUserById(userId);
        paymentService.payment(user, orderId, model);
    }
    ```
  - paymentService.payment
    ```java
    public void payment(User user, Long orderId, Model model) throws IllegalAccessException {
        OrdersForBuyer order = ordersService.checkBuyerOrder(user.getId(), orderId);
        int totalPrice = order.getTotalPrice();
        if (totalPrice == 0) {
            throw new IllegalStateException("결제 금액이 0인 주문입니다.");
        }
        if (order.getImpUid() != null) {
            throw new IllegalStateException("이미 처리된 주문입니다.");
        }
        String itemsName = order.getOrderItemsName().toString();
        model.addAttribute("orderId", order.getId());
        model.addAttribute("itemsName", itemsName);
        model.addAttribute("buyerName", user.getName());
        model.addAttribute("buyerEmail", user.getEmail());
        model.addAttribute("buyerPNUm", user.getPNum());
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("identificationCode", portOneApiConfig.getIdentificationCode());
        model.addAttribute("CID", portOneApiConfig.getCID());
    }
    ```
  - ordersService.checkBuyerOrder
    ```java
    public OrdersForBuyer checkBuyerOrder(Long buyerId, Long orderId) throws IllegalAccessException {
        OrdersForBuyer orderForBuyer = getOrderForBuyer(orderId);
        if (!orderForBuyer.getBuyer().getId().equals(buyerId)) {
            throw new IllegalAccessException("사용자의 주문이 아닙니다");
        }
        return orderForBuyer;
    }
    ```

|     종류     |           상세           |                                                                                                   설명                                                                                                   |
|:----------:|:----------------------:|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| Controller |   POST<br/> /payment   |                                                                POST 통신을 통해 결제에 필요한 정보와 model 을 전달받는다.<br/> Service 실행 후 payment.html 반환                                                                |
|    Dto     |       PaymentDto       |                                                                                결제에 필요한 정보 : 결제대상 사용자 고유번호, 결제대상 주문 고유번호                                                                                |
|  Service   |  userService.payment   |                                                                         사용지 고유번호를 통해 사용자 존재 검증<br/> paymentService.payment 호출                                                                          |  
|  Service   | paymentService.payment |                                                                            전달받은 주문이 사용자의 주문인지 검증<br/> model 에 Attribute 추가                                                                             |
|   Model    |         model          | orderId : 주문번호<br/> itemsName : 상품들 이름<br/> buyerName : 구매자 이름<br/> buyerEmail : 구매자 이메일<br/> buyerPNum : 구매자 전화번호<br/> totalPrice : 주문 전체 가격<br/> identificationCode : api 식별코드<br/> CID : api 가맹점 코드 |

### 결제검증
- Controller
    ```java
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
    ```

- Map<String, String> map
    ```
    imp_uid : 결제 ID
    amount : 결제 금액
    orderId : 주문 고유번호
    ```

- Service
  - paymentService.verifyIamPort
      ```java
      @Transactional
      public void verifyIamPort(IamportResponse<Payment> iamportResponse, int amount, Long orderId, String impUid) {
          OrdersForBuyer orderForBuyer = ordersService.getOrderForBuyer(orderId);
          if (iamportResponse.getResponse().getAmount().intValue() != amount) {
              throw new VerifyIamportException("PortOne 서버 결제 금액이 다릅니다.");
          }
          if (amount != orderForBuyer.getTotalPrice()) {
              throw new VerifyIamportException("주문서와 결제 금액이 다릅니다.");
          }
          ordersService.changeOrderStatusToCompletePayment(orderForBuyer, impUid);
      }
      ```
  - ordersService.changeOrderStatusToCompletePayment
    ```java
    public void changeOrderStatusToCompletePayment(OrdersForBuyer ordersForBuyer, String impUid) {
        List<OrderItem> orderItems = ordersForBuyer.getOrderItems();
        Set<OrdersForSeller> ordersForSellers = new HashSet<>();
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getOrderItemStatus().equals(WAITING_FOR_PAYMENT)) {
                orderItem.changeStatus(COMPLETE_PAYMENT);
                orderItem.setImpUid(impUid);
                ordersForSellers.add(getOrdersForSeller(orderItem.getSellerOrderId()));
            }
        }
        ordersForBuyer.setImpUid(impUid);
        for (OrdersForSeller ordersForSeller : ordersForSellers) {
            ordersForSeller.setImpUid(impUid);
        }
    }
    ```

|          종류          |                        상세                         |                                                           설명                                                            |
|:--------------------:|:-------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------------:|
|      Controller      |              POST<br/> verifyIamPort              | payment.html 의 function payment() 성공시 실행된다<br/> POST 통신을 결제검증에 필요한 정보를 전달받는다.<br/> imp_uid 를 통해 portOne 에서 결제된 건을 조회한다. |
| Map<String, String>  |                        map                        |                                                      결제검증에 필요한 정보                                                       |
|       Service        |           paymentService.verifyIamPort            |  주문 고유번호를 통해 DB에 저장된 주문을 불러온다.<br/> portOne 에서 조회한 결제건의 실제 결제 가격과 전달받은 금액이 같은지 검증<br/> DB에 저장된 주문의 금액과 전달받은 금액이 같은지 검증  |  
|       Service        | ordersService.changeOrderStatusToCompletePayment  |                                             구매자의 주문서와 판매자의 주문서에 결제번호를 저장한다.                                             |

### 결제오류로 인한 결제취소
- Controller
    ```java
    @ResponseBody
    @PostMapping("/cancelPayments")
    public IamportResponse<Payment> cancelPayments(@RequestBody Map<String, String> map) throws IamportResponseException, IOException {
        iamportResponse = api.paymentByImpUid(map.get("impUid"));
        CancelData data = cancelData(iamportResponse, map);
        return api.cancelPaymentByImpUid(data);
    }
    ```

- Map<String, String> map
    ```
    impUid: 결제 ID,
    reason: "결제금액 위/변조. 결제금액이 일치하지 않습니다.",
    checksum: 환불 금액,
    refundHolder: 구매자 이름
    ```

- cancelData
    ```java
    public CancelData cancelData(IamportResponse<Payment> iamportResponse, Map<String, String> map) {
        String reason = map.get("reason");
        BigDecimal checkSum = new BigDecimal(Integer.parseInt(map.get("checkSum")));
        String refundHolder = map.get("refundHolder");

        CancelData data = new CancelData(iamportResponse.getResponse().getImpUid(), true, checkSum);
        data.setReason(reason);
        data.setChecksum(checkSum);
        data.setRefund_holder(refundHolder);

        return data;
    }
    ```

|         종류          |            상세             |                                             설명                                              |
|:-------------------:|:-------------------------:|:-------------------------------------------------------------------------------------------:|
|     Controller      | POST<br/> /cancelPayments | 결제검증 실패시 실행된다<br/> POST 통신을 결제 취소에 필요한 정보를 전달받는다.<br/> imp_uid 를 통해 portOne 에서 결제된 건을 조회한다. |
| Map<String, String> |            map            |                                        결제 취소에 필요한 정보                                        |
|     CancelData      |        cancelData         |                                portOne 결제 취소에 필요한 데이터를 생성한다.                                |
```
checksum을 사용한이유
PortOne에서 권장하고 있으며 checksum은 환불 가능 금액을 뜻한다.
    - 1000원짜리 제품의 checksum 은 1000원이며, 100원이 부분환불 되었다면 checksum은 900원이다.
checksum을 사용해서 서버와 postOne 서버간에 환불 가능 금액이 일치하는지 검증한다. 만약 일치하지 않으면 환불이 실패한다.
checksum은 필수가 아니며 미 입력시 환불 가능 금액에 대한 검증을 하지 않는다.
```