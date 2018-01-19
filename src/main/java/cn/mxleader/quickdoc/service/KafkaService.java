package cn.mxleader.quickdoc.service;

public interface KafkaService {

    void sendMessage(String message);

    void sendMessage(String topic, String message);
}
