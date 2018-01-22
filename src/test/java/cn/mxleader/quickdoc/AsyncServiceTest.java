package cn.mxleader.quickdoc;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AsyncServiceTest {
    @Async
    public void sendMessage(String message) {
        System.out.println("Async Hello World..........异步消息调用"+message);
    }

}
