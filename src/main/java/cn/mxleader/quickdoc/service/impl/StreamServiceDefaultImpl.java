package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.service.StreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StreamServiceDefaultImpl implements StreamService {
    private final Logger log = LoggerFactory.getLogger(StreamServiceDefaultImpl.class);

    @Override
    public void sendMessage(String message) {
        log.info("DefaultStreamServiceImpl sending message: " + message);
    }

    @Override
    public void sendMessage(String topic, String message) {
        log.info("DefaultStreamServiceImpl sending topic: " + topic + ", message: " + message);
    }
}
