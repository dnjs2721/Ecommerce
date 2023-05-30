package won.ecommerce.controller;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import won.ecommerce.config.EcommerceConfig;
import won.ecommerce.entity.Address;
import won.ecommerce.entity.Category;
import won.ecommerce.entity.User;
import won.ecommerce.entity.UserStatus;
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
            User user = User.builder()
                    .name("관리자")
                    .nickname("Admin")
                    .email("admin@admin.com")
                    .password("admin")
                    .pNum("01012341234")
                    .birth("000000")
                    .address(new Address("tt", "tt", "tt", "tt", "tt"))
                    .build();
            user.setStatus(UserStatus.ADMIN);
            em.persist(user);

            Category keyboard = createCategory("키보드");
            Category lowNoiseKeyboard = createCategory("저소음 키보드");
            Category mechanicalKeyboard = createCategory("기계식 키보드");
            Category slimKeyboard = createCategory("슬림 키보드");
            lowNoiseKeyboard.addParentCategory(keyboard);
            mechanicalKeyboard.addParentCategory(keyboard);
            slimKeyboard.addParentCategory(keyboard);

            Category keyCap = createCategory("키캡");
            Category pointKeyCap = createCategory("포인트 키캡");
            Category cherryKeyCap = createCategory("체리 키캡");
            Category oEMKeyCap = createCategory("OEM 키캡");
            pointKeyCap.addParentCategory(keyCap);
            cherryKeyCap.addParentCategory(keyCap);
            oEMKeyCap.addParentCategory(keyCap);

            Category switches = createCategory("스위치");
            Category redSwitch = createCategory("적축");
            Category brownSwitch = createCategory("갈축");
            Category blueSwitch = createCategory("청축");
            redSwitch.addParentCategory(switches);
            brownSwitch.addParentCategory(switches);
            blueSwitch.addParentCategory(switches);


        }

        public Category createCategory(String name) {
            Category category = new Category(name);
            em.persist(category);
            return category;
        }
    }
}
