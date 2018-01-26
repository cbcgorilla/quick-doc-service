package cn.mxleader.quickdoc;

import cn.mxleader.quickdoc.service.StreamService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@RunWith(JUnitPlatform.class)
@SpringBootTest(classes = QuickDocTaskExecutorTestConfig.class)
@ExtendWith(SpringExtension.class)
public class QuickDocConfigurationTest {

    private final Logger log = LoggerFactory.getLogger(QuickDocConfigurationTest.class);

    @Autowired
    private StreamService streamService;

    @Autowired
    private AsyncServiceTest asyncServiceTest;

    @Test
    @DisplayName("AsyncService Test")
    public void setAsyncServiceTest() {
        System.out.println("Hello.....................");
        asyncServiceTest.sendMessage("ddd");
    }

    @Test
    @DisplayName("Kafka Service Test")
    public void kafkaServiceTest() {
        streamService.sendMessage("Hello Message from JUnit........");
        log.info("Hello Message has benn sent out...");
    }

    @Test
    @DisplayName("Kafka Service Topic Test")
    public void kafkaServiceTopicTest() {
        streamService.sendMessage("active-session", "Hello Message from JUnit........");
        log.info("Hello Message has benn sent out...");
    }

}
