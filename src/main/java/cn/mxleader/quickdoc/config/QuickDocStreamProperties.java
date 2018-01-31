package cn.mxleader.quickdoc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("quickdoc.stream")
public class QuickDocStreamProperties {

    /**
     * Stream消息开关
     */
    private Boolean enabled;

    /**
     * 消息流默认主题
     */
    private String topic;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
