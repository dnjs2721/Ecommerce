package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import won.ecommerce.entity.*;
import won.ecommerce.repository.orders.OrderItemRepository;
import won.ecommerce.repository.orders.OrdersForBuyerRepository;
import won.ecommerce.repository.orders.OrdersForSellerRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OrdersService {
    private final OrdersForBuyerRepository buyerRepository;
    private final OrdersForSellerRepository sellerRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * 구매자용 주문, 판매자용 주문, 주문상품 생성
     * @param buyer 구매자
     * @param allItemsAndCount 전체 주문 상품
     */
    public void createOrders(User buyer, Map<Item, Integer> allItemsAndCount) {
        Map<User, List<Map<Item, Integer>>> sellerAndItem = new HashMap<>(); // 판매자용 주문을 만들기 위한 맵

        OrdersForBuyer orderForBuyer = createOrderForBuyer(buyer); // 구매자용 주문 생성

        Set<Item> items = allItemsAndCount.keySet(); // 맵의 keySet 을 통해 items 을 받아온다.
        for (Item item : items) { // items 순회
            User seller = item.getSeller(); // 판매자 조회

            Map<Item, Integer> itemAndCount = new HashMap<>();
            itemAndCount.put(item, allItemsAndCount.get(item));

            // 판매자 별로 상품들 분류
            if (!sellerAndItem.containsKey(seller)) { // sellerAndItem 에 판매자 Key 가 없다면
                sellerAndItem.put(seller, new ArrayList<>(List.of(itemAndCount)));
            } else { // sellerAndItem 에 판매자 Key 가 있다면
                sellerAndItem.get(seller).add(itemAndCount);
            }
        }


        // 판매자별로 판매자용 주문 생성
        Set<User> sellers = sellerAndItem.keySet();
        for (User seller : sellers) {
            OrdersForSeller orderForSeller = createOrderForSeller(seller, buyer); // 판매자 주문 생성

            List<Map<Item, Integer>> sellerItemsAndCount = sellerAndItem.get(seller); // 주문 상품 리스트에서 판매자것만 리스트로 가져온다.
            for (Map<Item, Integer> sellerItemAndCount : sellerItemsAndCount) { // 주문 상품 중 판매자 것만 순회
                Set<Item> sellerItems = sellerItemAndCount.keySet(); // 맵의 keySet 을 통해 item 을 받아온다 Set 이지만 하나밖에 없다.
                for (Item sellerItem : sellerItems) { // for 문 이지만 item 은 하나밖에 없다.
                    int count = sellerItemAndCount.get(sellerItem); // 상품 수량을 가지고 온다.

                    // 주문상품 생성
                    // 구매자의 주문 id 와 판매자의 주문 id 를 갖는다.
                    // 한 주문에 대하여
                    //      구매자의 주문 id 를 통해 구매자는 판매자와 상관없이 한번에 조회 가능하다.
                    //      판매자의 주문 id 를 통해 판매자는 자신의 상품에 대한 주문을 조회 가능하다.
                    // 판매자 혹은 구매자가 회원 탈퇴 하여도 주문 상품 내역은 남는다. 상품 삭제도 동일
                    // buyerId, sellerId, itemId 를 통해 탈퇴 혹은 삭제 한 객체의 정보 조회 가능 (DeletedUser, DeletedItem)
                    createOrderItem(orderForBuyer.getId(), orderForSeller.getId(), buyer.getId(), seller.getId(), sellerItem, count);
                }
            }
        }
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
}
