package cn.mxleader.quickdoc.config;

import cn.mxleader.quickdoc.entities.FsOwner;
import cn.mxleader.quickdoc.entities.UserEntity;
import cn.mxleader.quickdoc.service.KafkaService;
import cn.mxleader.quickdoc.service.ReactiveCategoryService;
import cn.mxleader.quickdoc.service.ReactiveDirectoryService;
import cn.mxleader.quickdoc.service.ReactiveQuickDocConfigService;
import cn.mxleader.quickdoc.service.impl.KafkaServiceImpl;
import cn.mxleader.quickdoc.service.impl.ReactiveUserServiceImpl;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Date;

import static cn.mxleader.quickdoc.common.utils.AuthenticationUtil.SYSTEM_ADMIN_GROUP_OWNER;
import static cn.mxleader.quickdoc.security.config.WebSecurityConfig.AUTHORITY_ADMIN;
import static cn.mxleader.quickdoc.security.config.WebSecurityConfig.AUTHORITY_USER;

@SpringBootConfiguration
@ConditionalOnClass(KafkaService.class)
@EnableConfigurationProperties(QuickDocProperties.class)
public class QuickDocConfiguration {
    private final Logger log = LoggerFactory.getLogger(QuickDocConfiguration.class);

    private final QuickDocProperties quickDocProperties;

    public QuickDocConfiguration(QuickDocProperties quickDocProperties) {
        this.quickDocProperties = quickDocProperties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "quickdoc", value = "stream-topic")
    public KafkaService kafkaService(KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaServiceImpl(kafkaTemplate, quickDocProperties.getStreamTopic());
    }

    @Bean
    CommandLineRunner setupConfiguration(ReactiveUserServiceImpl reactiveUserService,
                                         ReactiveCategoryService reactiveCategoryService,
                                         ReactiveDirectoryService reactiveDirectoryService,
                                         ReactiveQuickDocConfigService reactiveQuickDocConfigService) {
        return args -> {
            reactiveQuickDocConfigService.getQuickDocConfig()
                    .flatMap(quickDocConfig -> {
                        if (!quickDocConfig.getInitialized()) {
                            // 初始化Admin管理账号
                            reactiveUserService
                                    .saveUser(new UserEntity(ObjectId.get(), "root",
                                            "chenbichao",
                                            new String[]{AUTHORITY_ADMIN},
                                            new String[]{AUTHORITY_ADMIN, AUTHORITY_USER},
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
                        return reactiveQuickDocConfigService.saveQuickDocConfig(quickDocConfig);
                    })
                    .onErrorMap(v -> {
                        log.warn(v.getMessage());
                        return v;
                    })
                    .subscribe(v -> log.info(v.toString()));
        };
    }

}
