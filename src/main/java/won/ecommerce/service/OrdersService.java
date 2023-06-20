package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import won.ecommerce.controller.dto.order.ChangeOrderStatusRequestDto;
import won.ecommerce.entity.*;
import won.ecommerce.repository.dto.search.order.*;
import won.ecommerce.repository.exchangeRefund.ExchangeRefundRepository;
import won.ecommerce.repository.orders.OrderItemRepository;
import won.ecommerce.repository.orders.OrdersForBuyerRepository;
import won.ecommerce.repository.orders.OrdersForSellerRepository;

import java.util.*;

import static won.ecommerce.entity.OrderItemStatus.*;

@Service
@RequiredArgsConstructor
public class OrdersService {
    private final OrdersForBuyerRepository buyerRepository;
    private final OrdersForSellerRepository sellerRepository;
    private final OrderItemRepository orderItemRepository;
    private final ExchangeRefundRepository exchangeRefundRepository;

    /**
     * 구매자용 주문, 판매자용 주문, 주문상품 생성
     * @param buyer 구매자
     * @param allItemsAndCount 전체 주문 상품
     */
    public void createOrders(User buyer, Map<Item, Integer> allItemsAndCount) {
        Map<User, List<Map<Item, Integer>>> sellerAndItem = new HashMap<>(); // 판매자용 주문을 만들기 위한 맵

        Set<Item> items = allItemsAndCount.keySet(); // 맵의 keySet 을 통해 items 을 받아온다.
        for (Item item : items) { // items 순회
            User seller = item.getSeller(); // 판매자 조회

            Map<Item, Integer> itemAndCount = new HashMap<>();

            int shoppingCartItemCount = allItemsAndCount.get(item);

            itemAndCount.put(item, shoppingCartItemCount);

            // 판매자 별로 상품들 분류
            if (!sellerAndItem.containsKey(seller)) { // sellerAndItem 에 판매자 Key 가 없다면
                sellerAndItem.put(seller, new ArrayList<>(List.of(itemAndCount)));
            } else { // sellerAndItem 에 판매자 Key 가 있다면
                sellerAndItem.get(seller).add(itemAndCount);
            }
        }

        OrdersForBuyer orderForBuyer = createOrderForBuyer(buyer); // 구매자용 주문 생성

        // 판매자별로 판매자용 주문 생성
        Set<User> sellers = sellerAndItem.keySet();
        for (User seller : sellers) {
            OrdersForSeller orderForSeller = createOrderForSeller(seller, buyer); // 판매자 주문 생성

            List<Map<Item, Integer>> sellerItemsAndCount = sellerAndItem.get(seller); // 주문 상품 리스트에서 판매자것만 리스트로 가져온다.
            for (Map<Item, Integer> sellerItemAndCount : sellerItemsAndCount) { // 주문 상품 중 판매자 것만 순회
                Set<Item> sellerItems = sellerItemAndCount.keySet(); // 맵의 keySet 을 통해 item 을 받아온다 Set 이지만 하나밖에 없다.
                for (Item sellerItem : sellerItems) { // for 문 이지만 item 은 하나밖에 없다.
                    int shoppingCartItemCount = sellerItemAndCount.get(sellerItem); // 상품 수량을 가지고 온다.
                    int stockQuantity = sellerItem.getStockQuantity();

                    sellerItem.decreaseStockQuantity(shoppingCartItemCount);
                    // 주문상품 생성
                    // 구매자의 주문 id 와 판매자의 주문 id 를 갖는다.
                    // 한 주문에 대하여
                    //      구매자의 주문 id 를 통해 구매자는 판매자와 상관없이 한번에 조회 가능하다.
                    //      판매자의 주문 id 를 통해 판매자는 자신의 상품에 대한 주문을 조회 가능하다.
                    // 판매자 혹은 구매자가 회원 탈퇴 하여도 주문 상품 내역은 남는다. 상품 삭제도 동일
                    // buyerId, sellerId, itemId 를 통해 탈퇴 혹은 삭제 한 객체의 정보 조회 가능 (DeletedUser, DeletedItem)
                    createOrderItem(orderForBuyer.getId(), orderForSeller.getId(), buyer.getId(), seller.getId(), sellerItem, shoppingCartItemCount);
                }
            }
        }
    }

    /**
     * 단건 주문
     */
    public String orderSingleItem(User buyer, Item item, int itemCount) {
        item.decreaseStockQuantity(itemCount); // NotEnoughStockException
        User seller = item.getSeller();
        OrdersForBuyer orderForBuyer = createOrderForBuyer(buyer);
        OrdersForSeller orderForSeller = createOrderForSeller(seller, buyer);
        createOrderItem(orderForBuyer.getId(), orderForSeller.getId(), buyer.getId(), seller.getId(), item, itemCount);

        return item.getName();
    }

    /**
     * 주문 조회 사용자
     */
    public Page<SearchOrdersForBuyerDto> searchOrdersForBuyer(Long buyerId, OrderSearchCondition condition, Pageable pageable) {
        return buyerRepository.searchOrdersForBuyer(buyerId, condition, pageable);
    }

    /**
     * 주문 상세 조회 사용자
     */
    public List<SearchOrderItemsForBuyerDto> searchOrderDetailForBuyer(Long buyerId, Long orderId) throws IllegalAccessException {
        checkBuyerOrder(buyerId, orderId);
        return buyerRepository.searchOrderItemsForBuyer(orderId);
    }

    /**
     * 주문 조회 판매자
     */
    public Page<SearchOrdersForSellerDto> searchOrdersForSeller(Long sellerId, OrderSearchCondition condition, Pageable pageable) {
        return sellerRepository.searchOrdersForSeller(sellerId, condition, pageable);
    }

    /**
     * 주문 상세 조회 판매자
     */
    public List<SearchOrderItemForSellerDto> searchOrderDetailForSeller(Long sellerId, Long orderId) throws IllegalAccessException {
        checkSellerOrder(sellerId, orderId);
        return sellerRepository.searchOrderItemsForSeller(orderId);
    }

    /**
     * 주문 상품 취소 홈
     */
    public void cancelOrderHome(OrderItemStatus orderItemStatus) {
        if (orderItemStatus.equals(CANCEL)) {
            throw new IllegalStateException("이미 취소된 상품입니다");
        } else if (orderItemStatus.equals(WAITING_FOR_DELIVERY) || orderItemStatus.equals(SHIPPING)) {
            throw new IllegalStateException("배송중인 상품입니다.");
        } else if (orderItemStatus.equals(DELIVERY_COMPLETE)) {
            throw new IllegalStateException("배송이 완료된 상품입니다. 교환/환불을 진행해 주세요.");
        }
    }


    /**
     * 판매자 주문 상품 상태 변경
     */
    public String changeOrderStatus(Item item, OrderItem orderItem, ChangeOrderStatusRequestDto request){
        OrderItemStatus orderItemStatus = request.getOrderItemStatus();
        if (orderItemStatus.equals(CANCEL)) {
            orderItem.setComment("판매자에 의한 취소 " + request.getComment());
            item.increaseStockQuantity(orderItem.getCount());
        } else if (orderItemStatus.equals(WAITING_FOR_PAYMENT)) {
            throw new IllegalStateException("해당 단계로 변경할 수 없습니다.");
        }
        orderItem.changeStatus(orderItemStatus);

        return orderItem.getItemName();
    }

    // 주문상품 생성 생성 저장 메서드
    public void createOrderItem(Long buyerOrderId, Long sellerOrderId, Long buyerId, Long sellerId, Item item, int count) {
        OrderItem orderItem = OrderItem.builder()
                .buyerOrderId(buyerOrderId)
                .sellerOrderId(sellerOrderId)
                .buyerId(buyerId)
                .sellerId(sellerId)
                .itemId(item.getId())
                .itemName(item.getName())
                .price(item.getPrice())
                .count(count)
                .build();

        orderItemRepository.save(orderItem);
    }

    // 판매자용 주문 생성 저장 메서드
    public OrdersForSeller createOrderForSeller(User seller, User buyer) {
        OrdersForSeller ordersForSeller = OrdersForSeller.builder()
                .seller(seller)
                .buyerName(buyer.getName())
                .buyerPNum(buyer.getPNum())
                .buyerAddress(buyer.getAddress())
                .build();

        sellerRepository.save(ordersForSeller);

        return ordersForSeller;
    }

    // 구매자용 주문 생성 저장 메서드
    public OrdersForBuyer createOrderForBuyer(User buyer) {
        OrdersForBuyer ordersForBuyer = new OrdersForBuyer(buyer);

        buyerRepository.save(ordersForBuyer);

        return ordersForBuyer;
    }

    // 구매자 주문 존재 체크
    public OrdersForBuyer getOrderForBuyer(Long orderId) {
        Optional<OrdersForBuyer> optionalOrder = buyerRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            throw new NoSuchElementException("잘못된 주문번호 입니다.");
        }
        return optionalOrder.get();
    }

    // 구매자의 주문인지 체크
    public OrdersForBuyer checkBuyerOrder(Long buyerId, Long orderId) throws IllegalAccessException {
        OrdersForBuyer orderForBuyer = getOrderForBuyer(orderId);
        if (!orderForBuyer.getBuyer().getId().equals(buyerId)) {
            throw new IllegalAccessException("사용자의 주문이 아닙니다");
        }
        return orderForBuyer;
    }

    // 판매자 주문 존재 체크
    public OrdersForSeller getOrdersForSeller(Long orderId) {
        Optional<OrdersForSeller> optionalOrder = sellerRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            throw new NoSuchElementException("잘못된 주문번호 입니다.");
        }
        return optionalOrder.get();
    }

    // 핀매자 주문인지 체크
    public void checkSellerOrder(Long sellerId, Long orderId) throws IllegalAccessException {
        OrdersForSeller ordersForSeller = getOrdersForSeller(orderId);
        if (!ordersForSeller.getSeller().getId().equals(sellerId)) {
            throw new IllegalAccessException("사용자의 주문이 아닙니다.");
        }
    }


    // 구매 상품 존재 확인
    public OrderItem checkOrderItem(Long orderItemId) {
        Optional<OrderItem> optionalOrderItem = orderItemRepository.findById(orderItemId);
        if (optionalOrderItem.isEmpty()) {
            throw new NoSuchElementException("잘못된 주문상품번호 입니다.");
        }
        return optionalOrderItem.get();
    }

    // 구매자의 구매 상품인지 확인
    public OrderItem checkBuyerOrderItem(Long buyerId, Long orderItemId) throws IllegalAccessException {
        OrderItem orderItem = checkOrderItem(orderItemId);
        if (!orderItem.getBuyerId().equals(buyerId)) {
            throw new IllegalAccessException("사용자의 주문상품이 아닙니다.");
        }
        return orderItem;
    }

    public OrderItem checkSellerOrderItem(Long sellerId, Long orderItemId) throws IllegalAccessException {
        OrderItem orderItem = checkOrderItem(orderItemId);
        if (!orderItem.getSellerId().equals(sellerId)) {
            throw new IllegalAccessException("사용자의 주문상품이 아닙니다.");
        }
        return orderItem;
    }

    // 결제 번호 입력
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
}
