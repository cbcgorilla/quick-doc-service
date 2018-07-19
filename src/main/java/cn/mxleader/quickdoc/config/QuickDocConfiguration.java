package cn.mxleader.quickdoc.config;

import cn.mxleader.quickdoc.entities.SysProfile;
import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.service.*;
import cn.mxleader.quickdoc.service.impl.StreamServiceDefaultImpl;
import cn.mxleader.quickdoc.service.impl.StreamServiceKafkaImpl;
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
import java.util.HashMap;
import java.util.HashSet;

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
            return new StreamServiceKafkaImpl(kafkaTemplate, quickDocStreamProperties.getTopic());
        else
            return new StreamServiceDefaultImpl();
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
                                            LDAPService ldapService,
                                            FileService fileService,
                                            ConfigService configService) {
        return args -> {
            SysProfile sysProfile = configService.getSysProfile();
            if (sysProfile == null) {
                sysProfile = new SysProfile(ObjectId.get(), serviceAddress(), new HashMap<>(),
                        false, new Date(), null);
            }
            if (!sysProfile.getInitialized()) {

                // 初始化默认图标文件
                sysProfile.getIconMap().put("SYS_LOGO", fileService.
                        storeServerFile("static/images/favicon.png"));

                sysProfile.getIconMap().put("AWARD", fileService.
                        storeServerFile("static/images/Goldbull.jpg"));

                // 初始化Admin管理账号
                userService.saveUser(new SysUser(ObjectId.get(), "admin",
                        "管理员", "系统管理员", "chenbichao",
                        sysProfile.getIconMap().get("AWARD"), false, "",
                        new HashSet<SysUser.Authority>() {{
                            add(SysUser.Authority.ADMIN);
                        }},
                        new HashSet<String>() {{
                            add("administrators");
                            add("users");
                        }},
                        "admin@mxleader.cn"));
                // 初始化成功标记
                sysProfile.setInitialized(true);
            }
            sysProfile.setStartup(new Date());
            configService.saveSysProfile(sysProfile);
        };
    }

}
