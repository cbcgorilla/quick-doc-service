package cn.mxleader.quickdoc.config;

import cn.mxleader.quickdoc.entities.AccessAuthorization;
import cn.mxleader.quickdoc.entities.QuickDocHealth;
import cn.mxleader.quickdoc.entities.QuickDocUser;
import cn.mxleader.quickdoc.service.ConfigService;
import cn.mxleader.quickdoc.service.ReactiveFolderService;
import cn.mxleader.quickdoc.service.StreamService;
import cn.mxleader.quickdoc.service.impl.DefaultStreamServiceImpl;
import cn.mxleader.quickdoc.service.impl.KafkaStreamServiceImpl;
import cn.mxleader.quickdoc.service.impl.ReactiveUserServiceImpl;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import static cn.mxleader.quickdoc.web.config.AuthenticationToolkit.SYSTEM_ADMIN_GROUP_OWNER;

@SpringBootConfiguration
@ConditionalOnClass(StreamService.class)
@EnableConfigurationProperties(QuickDocStreamProperties.class)
public class QuickDocConfiguration {

    @Value("${server.port}")
    String serverPort;

    private String serviceAddress() {
        try {
            return InetAddress.getLocalHost().getHostName() + ":" + serverPort;
        } catch (UnknownHostException e) {
            return "localhost:" + serverPort;
        }
    }

    private final QuickDocStreamProperties quickDocStreamProperties;

    public QuickDocConfiguration(QuickDocStreamProperties quickDocStreamProperties) {
        this.quickDocStreamProperties = quickDocStreamProperties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "quickdoc.stream", value = "enabled")
    public StreamService streamService(KafkaTemplate<String, String> kafkaTemplate) {
        if (quickDocStreamProperties.getEnabled())
            return new KafkaStreamServiceImpl(kafkaTemplate, quickDocStreamProperties.getTopic());
        else
            return new DefaultStreamServiceImpl();
    }

    @Bean
    CommandLineRunner initConfigurationData(ReactiveUserServiceImpl reactiveUserService,
                                            ReactiveFolderService reactiveFolderService,
                                            ConfigService configService) {
        return args -> {
            QuickDocHealth quickDocHealth = configService.getQuickDocHealth();
            if (quickDocHealth == null) {
                quickDocHealth = new QuickDocHealth(ObjectId.get(), serviceAddress(),
                        false, new Date(), null);
            }
            if (!quickDocHealth.getInitialized()) {
                // 初始化Admin管理账号
                reactiveUserService
                        .saveUser(new QuickDocUser(ObjectId.get(), "admin",
                                "chenbichao",
                                new QuickDocUser.Authorities[]{QuickDocUser.Authorities.ADMIN},
                                new String[]{"administrators"})).subscribe();

                // 初始化系统目录
                AccessAuthorization[] configOwners = {SYSTEM_ADMIN_GROUP_OWNER};
                AccessAuthorization[] rootOwners = {SYSTEM_ADMIN_GROUP_OWNER};

                reactiveFolderService.save("config", quickDocHealth.getId(),
                        false, configOwners).subscribe();

                reactiveFolderService.save("root", quickDocHealth.getId(),
                        true, rootOwners).subscribe();
                reactiveFolderService.save("api", quickDocHealth.getId(),
                        true, rootOwners).subscribe();

                // 初始化成功标记
                quickDocHealth.setInitialized(true);
            }
            quickDocHealth.setStartup(new Date());
            configService.saveQuickDocHealth(quickDocHealth);
        };
    }

}
