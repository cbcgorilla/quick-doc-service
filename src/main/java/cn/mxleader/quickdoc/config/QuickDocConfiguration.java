package cn.mxleader.quickdoc.config;

import cn.mxleader.quickdoc.entities.FsOwner;
import cn.mxleader.quickdoc.entities.QuickDocConfig;
import cn.mxleader.quickdoc.entities.UserEntity;
import cn.mxleader.quickdoc.service.QuickDocConfigService;
import cn.mxleader.quickdoc.service.ReactiveCategoryService;
import cn.mxleader.quickdoc.service.ReactiveDirectoryService;
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

import static cn.mxleader.quickdoc.common.AuthenticationHandler.SYSTEM_ADMIN_GROUP_OWNER;

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
            return "localhost:"+serverPort;
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
                                            ReactiveCategoryService reactiveCategoryService,
                                            ReactiveDirectoryService reactiveDirectoryService,
                                            QuickDocConfigService quickDocConfigService) {
        return args -> {
            QuickDocConfig quickDocConfig = quickDocConfigService.getQuickDocConfig();
            if(quickDocConfig==null){
                quickDocConfig = new QuickDocConfig(ObjectId.get(), serviceAddress(),
                        false, new Date(), null);
            }
            if (!quickDocConfig.getInitialized()) {
                // 初始化Admin管理账号
                reactiveUserService
                        .saveUser(new UserEntity(ObjectId.get(), "admin",
                                "chenbichao",
                                new UserEntity.Authorities[]{UserEntity.Authorities.ADMIN},
                                new String[]{"administrators"})).subscribe();

                // 初始化文件分类
                reactiveCategoryService.addCategory("照片").subscribe();
                reactiveCategoryService.addCategory("音乐").subscribe();
                reactiveCategoryService.addCategory("图书").subscribe();
                reactiveCategoryService.addCategory("视频").subscribe();

                // 初始化根目录
                FsOwner[] configOwners = {SYSTEM_ADMIN_GROUP_OWNER};
                reactiveDirectoryService.saveDirectory("config", quickDocConfig.getId(),
                        false, configOwners).subscribe();

                FsOwner[] rootOwners = {SYSTEM_ADMIN_GROUP_OWNER};
                reactiveDirectoryService.saveDirectory("root", quickDocConfig.getId(),
                        true, rootOwners).subscribe();

                // 初始化成功标记
                quickDocConfig.setInitialized(true);
            }
            quickDocConfig.setStartup(new Date());
            quickDocConfigService.saveQuickDocConfig(quickDocConfig);
        };
    }

}
