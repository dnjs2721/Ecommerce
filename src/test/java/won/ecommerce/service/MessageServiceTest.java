package won.ecommerce.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MessageServiceTest {
    @Autowired
    MessageService messageService;

    @Test
    public void senMessageTest() throws Exception {
        messageService.sendMessage("dd");
    }

}