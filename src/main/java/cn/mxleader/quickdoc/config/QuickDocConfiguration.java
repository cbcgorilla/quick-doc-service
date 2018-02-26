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
import cn.mxleader.quickdoc.web.context.MxLeaderMultipartResolver;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

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
        //return new DefaultStreamServiceImpl();
        if (quickDocStreamProperties.getEnabled())
            return new KafkaStreamServiceImpl(kafkaTemplate, quickDocStreamProperties.getTopic());
        else
            return new DefaultStreamServiceImpl();
    }

    @Bean
    public MultipartResolver multipartResolver(){
        CommonsMultipartResolver resolver = new MxLeaderMultipartResolver();
        resolver.setDefaultEncoding("UTF-8");
        resolver.setResolveLazily(true);//resolveLazily属性启用是为了推迟文件解析，以在在UploadAction中捕获文件大小异常
        resolver.setMaxInMemorySize(40960);
        resolver.setMaxUploadSize(1024*1024*1024);//上传文件大小 1G 1024*1024*1024
        return resolver;
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
