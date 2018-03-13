package cn.mxleader.quickdoc.config;

import cn.mxleader.quickdoc.entities.AccessAuthorization;
import cn.mxleader.quickdoc.entities.Metadata;
import cn.mxleader.quickdoc.entities.SysProfile;
import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.service.*;
import cn.mxleader.quickdoc.service.impl.DefaultStreamServiceImpl;
import cn.mxleader.quickdoc.service.impl.KafkaStreamServiceImpl;
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
import static cn.mxleader.quickdoc.web.config.AuthenticationToolkit.translateShareSetting;

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
/*
    @Bean
    public MultipartResolver multipartResolver(){
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setDefaultEncoding("UTF-8");
        resolver.setResolveLazily(true);//resolveLazily属性启用是为了推迟文件解析，以在在UploadAction中捕获文件大小异常
        resolver.setMaxInMemorySize(40960);
        resolver.setMaxUploadSize(1024*1024*1024);//上传文件大小 1G 1024*1024*1024
        return resolver;
    }
*/

    @Bean
    CommandLineRunner initConfigurationData(UserService userService,
                                            FileService fileService,
                                            ReactiveFolderService reactiveFolderService,
                                            ConfigService configService) {
        return args -> {
            SysProfile sysProfile = configService.getSysProfile();
            if (sysProfile == null) {
                sysProfile = new SysProfile(ObjectId.get(), serviceAddress(),
                        false, new Date(), null);
            }
            if (!sysProfile.getInitialized()) {

                Metadata metadata = new Metadata("image/jpeg", ObjectId.get(),
                        new AccessAuthorization[]{new AccessAuthorization("users",
                                AccessAuthorization.Type.TYPE_GROUP,1)}, null);

                //ObjectId fileId = fileService.store(file.getInputStream(), filename, metadata);
                // 初始化Admin管理账号
                userService
                        .saveUser(new SysUser(ObjectId.get(), "admin", "系统管理员",
                                "chenbichao",
                                new ObjectId("5aa7a78b16422c2e3c19e3b9"),
                                new SysUser.Authorities[]{SysUser.Authorities.ADMIN},
                                new String[]{"administrators","users"},
                                "chenbichao@mxleader.cn"));

                // 初始化系统目录
                AccessAuthorization[] configOwners = {SYSTEM_ADMIN_GROUP_OWNER};
                AccessAuthorization[] rootOwners = {SYSTEM_ADMIN_GROUP_OWNER};

                reactiveFolderService.save("config", sysProfile.getId(), configOwners).subscribe();

                reactiveFolderService.save("root", sysProfile.getId(), rootOwners).subscribe();
                reactiveFolderService.save("api", sysProfile.getId(), rootOwners).subscribe();

                // 初始化成功标记
                sysProfile.setInitialized(true);
            }
            sysProfile.setStartup(new Date());
            configService.saveSysProfile(sysProfile);
        };
    }

}
