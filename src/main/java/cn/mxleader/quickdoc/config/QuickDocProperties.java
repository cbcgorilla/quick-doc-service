package cn.mxleader.quickdoc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("quickdoc")
public class QuickDocProperties {

    /**
     * Kafka消息流默认主题
     */
    private String streamTopic;

    public String getStreamTopic() {
        return streamTopic;
    }

    public void setStreamTopic(String streamTopic) {
        this.streamTopic = streamTopic;
    }
}
