package cn.mxleader.quickdoc.config;

import cn.mxleader.quickdoc.service.ReactiveFileService;
import cn.mxleader.quickdoc.service.TensorFlowService;
import cn.mxleader.quickdoc.service.impl.TensorFlowServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(TensorFlowService.class)
@EnableConfigurationProperties(TensorFlowProperties.class)
public class TensorFlowConfiguration {
    private final Logger log = LoggerFactory.getLogger(TensorFlowConfiguration.class);
    private final TensorFlowProperties tensorFlowProperties;

    public TensorFlowConfiguration(TensorFlowProperties tensorFlowProperties) {
        this.tensorFlowProperties = tensorFlowProperties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "quickdoc.tensorflow", value = "model-dir")
    @Autowired
    public TensorFlowService tensorFlowService(ReactiveFileService reactiveFileService) {
        return new TensorFlowServiceImpl(reactiveFileService, tensorFlowProperties.getModelDir());
    }

}
