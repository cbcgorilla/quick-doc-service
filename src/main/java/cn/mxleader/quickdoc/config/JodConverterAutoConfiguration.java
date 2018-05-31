package cn.mxleader.quickdoc.config;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jodconverter.DocumentConverter;
import org.jodconverter.LocalConverter;
import org.jodconverter.office.LocalOfficeManager;
import org.jodconverter.office.OfficeManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Stream;

@Configuration
@ConditionalOnClass(LocalConverter.class)
@ConditionalOnProperty(
        prefix = "jodconverter",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
@EnableConfigurationProperties(JodConverterAutoProperties.class)
public class JodConverterAutoConfiguration {

    private final JodConverterAutoProperties properties;

    public JodConverterAutoConfiguration(final JodConverterAutoProperties properties) {
        this.properties = properties;
    }

    // Creates the OfficeManager bean.
    private OfficeManager createOfficeManager() {

        final LocalOfficeManager.Builder builder = LocalOfficeManager.builder();

        if (!StringUtils.isBlank(properties.getPortNumbers())) {
            builder.portNumbers(
                    ArrayUtils.toPrimitive(
                            Stream.of(StringUtils.split(properties.getPortNumbers(), ", "))
                                    .map(str -> NumberUtils.toInt(str, 2002))
                                    .toArray(Integer[]::new)));
        }

        builder.officeHome(properties.getOfficeHome());
        builder.workingDir(properties.getWorkingDir());
        builder.templateProfileDir(properties.getTemplateProfileDir());
        builder.killExistingProcess(properties.isKillExistingProcess());
        builder.processTimeout(properties.getProcessTimeout());
        builder.processRetryInterval(properties.getProcessRetryInterval());
        builder.taskExecutionTimeout(properties.getTaskExecutionTimeout());
        builder.maxTasksPerProcess(properties.getMaxTasksPerProcess());
        builder.taskQueueTimeout(properties.getTaskQueueTimeout());

        // Starts the manager
        return builder.build();
    }

    @Bean(name = "autoOfficeManager", initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean(name = "autoOfficeManager")
    public OfficeManager autoOfficeManager() {

        return createOfficeManager();
    }

    // Must appear after the autoOfficeManager bean creation. Do not reorder this class by name.
    @Bean
    @ConditionalOnMissingBean(name = "autoDocumentConverter")
    @ConditionalOnBean(name = "autoOfficeManager")
    public DocumentConverter autoDocumentConverter(final OfficeManager autoOfficeManager) {

        return LocalConverter.make(autoOfficeManager);
    }
}
