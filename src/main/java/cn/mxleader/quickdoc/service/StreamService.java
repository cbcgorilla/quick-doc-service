package cn.mxleader.quickdoc.service;

public interface StreamService {

    void sendMessage(String message);

    void sendMessage(String topic, String message);
}
