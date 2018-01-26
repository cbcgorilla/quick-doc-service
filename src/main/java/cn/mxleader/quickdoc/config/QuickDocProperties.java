package cn.mxleader.quickdoc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("quickdoc")
public class QuickDocProperties {

    /**
     * Stream消息开关
     */
    private Boolean streamEnabled;

    /**
     * 消息流默认主题
     */
    private String streamTopic;

    public Boolean getStreamEnabled() {
        return streamEnabled;
    }

    public void setStreamEnabled(Boolean streamEnabled) {
        this.streamEnabled = streamEnabled;
    }

    public String getStreamTopic() {
        return streamTopic;
    }

    public void setStreamTopic(String streamTopic) {
        this.streamTopic = streamTopic;
    }


}
