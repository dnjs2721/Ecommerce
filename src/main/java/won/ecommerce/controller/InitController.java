package won.ecommerce.controller;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import won.ecommerce.config.EcommerceConfig;
import won.ecommerce.entity.*;
import won.ecommerce.service.UserService;

@Component
@RequiredArgsConstructor
public class InitController {

    private final InitControllerService initControllerService;

    @PostConstruct
    public void init() {
        initControllerService.init();
    }

    @Component
    @RequiredArgsConstructor
    static class InitControllerService {

        private final EntityManager em;

        @Transactional
        public void init() {
            User admin = User.builder()
                    .name("관리자")
                    .nickname("Admin")
                    .email("ska5du1@naver.com")
                    .password("admin")
                    .pNum("01012341234")
                    .birth("000000")
                    .address(new Address("tt", "tt", "tt", "tt", "tt"))
                    .build();
            admin.setStatus(UserStatus.ADMIN);
            em.persist(admin);

            User user1 = User.builder()
                    .name("최상원")
                    .nickname("dnjs2721")
                    .email("skadu66@gmail.com")
                    .password("Ska@1012")
                    .pNum("01035582721")
                    .birth("981012")
                    .address(new Address("대구광역시", "북구", "대현로 17길 5", "서봉빌라 401호", "41525"))
                    .build();
            user1.setStatus(UserStatus.SELLER);
            em.persist(user1);

            User user2 = User.builder()
                    .name("김연수")
                    .nickname("skadu6")
                    .email("dnjs2721@gmail.com")
                    .password("Ska@1012")
                    .pNum("01035582721")
                    .birth("010706")
                    .address(new Address("경상북도", "경산시 하양읍", "대경로 669-5", "한울타리 101호", "41525"))
                    .build();
            user2.setStatus(UserStatus.SELLER);
            em.persist(user2);

            Category etc = createCategory("기타");
            Category keyboard = createCategory("키보드");
            Category lowNoiseKeyboard = createCategory("저소음 키보드");
            Category mechanicalKeyboard = createCategory("기계식 키보드");
            Category slimKeyboard = createCategory("슬림 키보드");
            lowNoiseKeyboard.addParentCategory(keyboard);
            mechanicalKeyboard.addParentCategory(keyboard);
            slimKeyboard.addParentCategory(keyboard);

            Category keyCap = createCategory("키캡");
            Category osaKeyKap = createCategory("OSA 프로파일");
            Category cherryKeyCap = createCategory("체리 프로파일");
            Category oemKeyCap = createCategory("OEM 프로파일");
            osaKeyKap.addParentCategory(keyCap);
            cherryKeyCap.addParentCategory(keyCap);
            oemKeyCap.addParentCategory(keyCap);

            Category switches = createCategory("스위치");
            Category redSwitch = createCategory("적축");
            Category brownSwitch = createCategory("갈축");
            Category blueSwitch = createCategory("청축");
            redSwitch.addParentCategory(switches);
            brownSwitch.addParentCategory(switches);
            blueSwitch.addParentCategory(switches);

            createItem("KeyChron K2", 80000, 20, mechanicalKeyboard, user1);
            createItem("KeyChron K4", 80000, 15, mechanicalKeyboard, user1);
            createItem("KeyChron K6", 80000, 17, mechanicalKeyboard, user2);
            createItem("KeyChron K8", 90000, 30, mechanicalKeyboard, user2);

            createItem("KeyChron K10 Pro 저소음 적축", 1800000, 10, lowNoiseKeyboard, user1);
            createItem("KeyChron K8 Pro 저소음 갈축", 160000, 12, lowNoiseKeyboard, user2);
            createItem("KeyChron K8 Pro 저소음 적축", 160000, 12, lowNoiseKeyboard, user2);
            createItem("KeyChron K10 Pro 저소음 갈축", 180000, 11, lowNoiseKeyboard, user1);

            createItem("KeyChron K1", 60000, 6, slimKeyboard, user1);
            createItem("KeyChron K7", 65000, 8, slimKeyboard, user2);

            createItem("이중사출 OSA PBY 134키 교체용 키캡 풀 세트", 64000, 15, osaKeyKap, user1);
            createItem("이중사출 PBT OSA 프로파일 키캡 Black", 59000, 15, osaKeyKap, user1);

            createItem("체리 프로파일 이중사출 PBT 영문 키캡 풀 세트 White Mint", 50000, 15, cherryKeyCap, user2);

            createItem("OEM 염료승화 PBT 영문 세트 Bluish Black White", 50000, 15, oemKeyCap, user2);

            createItem("키크론 K-PRO 기계식 스위치 적축", 40000, 100, redSwitch, user1);

            createItem("키크론 K-PRO 기계식 스위치 갈축", 40000, 100, redSwitch, user1);

            createItem("키크론 K-PRO 기계식 스위치 청축", 40000, 100, redSwitch, user1);
        }

        public Category createCategory(String name) {
            Category category = new Category(name);
            em.persist(category);
            return category;
        }

        public Item createItem(String name, int price, int stockQuantity, Category category, User seller) {
            Item item = Item.builder()
                    .name(name)
                    .price(price)
                    .stockQuantity(stockQuantity)
                    .build();

            item.setCategory(category);
            item.setSeller(seller);
            em.persist(item);
            return item;
        }
    }
}
