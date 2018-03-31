package cn.mxleader.quickdoc.config;

import cn.mxleader.quickdoc.entities.*;
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
import java.util.Arrays;
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
                                            FileService fileService,
                                            DiskService diskService,
                                            FolderService folderService,
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
                        storeServerFile("classpath:static/images/favicon.png"));

                sysProfile.getIconMap().put("AWARD", fileService.
                        storeServerFile("classpath:static/images/Goldbull.jpg"));

                // 初始化Admin管理账号
                userService.saveUser(new SysUser(ObjectId.get(), "admin",
                        "系统管理员", "chenbichao",
                        sysProfile.getIconMap().get("AWARD"),
                        new HashSet<SysUser.Authority>() {{
                            add(SysUser.Authority.ADMIN);
                        }},
                        new HashSet<String>() {{
                            add("administrators");
                            add("users");
                        }},
                        "admin@mxleader.cn"));
                for (int i = 0; i <= 50000; i++) {
                    userService.saveUser(new SysUser(ObjectId.get(), "testUser" + i,
                            "测试账号" + i, "chenbichao",
                            sysProfile.getIconMap().get("AWARD"),
                            new HashSet<SysUser.Authority>() {{
                                add(SysUser.Authority.USER);
                            }},
                            new HashSet<String>() {{
                                add("users");
                            }},
                            "testaccount" + i + "@mxleader.cn"));
                }

                // 初始化系统目录
                ObjectId gId = diskService.save("共享磁盘1",
                        new Authorization("administrators", AuthType.GROUP)).getId();
                ObjectId id = diskService.save("我的磁盘1",
                        new Authorization("admin", AuthType.PRIVATE)).getId();

                // ============================以下为测试数据 @TODO 待删除
                ObjectId gId1 = folderService.save("一级目录", new ParentLink(gId, AuthTarget.DISK, gId),
                        new Authorization("administrators", AuthType.GROUP)).getId();
                folderService.save("一级目录2WRITE", new ParentLink(gId, AuthTarget.DISK, gId),
                        new Authorization("administrators", AuthType.GROUP, AuthAction.WRITE)).getId();

                ObjectId id1 = folderService.save("一级目录", new ParentLink(id, AuthTarget.DISK, id),
                        new Authorization("admin", AuthType.PRIVATE)).getId();
                folderService.addParent(id1, new ParentLink(gId, AuthTarget.DISK, gId));
                ObjectId id2 = folderService.save("二级目录", new ParentLink(id1, AuthTarget.FOLDER, id),
                        new Authorization("admin", AuthType.PRIVATE)).getId();
                ObjectId id3 = folderService.save("三级目录", new ParentLink(id2, AuthTarget.FOLDER, id),
                        new Authorization("admin", AuthType.PRIVATE)).getId();
/*
                fileService.saveMetadata(sysProfile.getIconMap().get("AWARD"),
                        new Metadata("application/octet-stream",
                                new HashSet<ParentLink>() {{
                                    add(new ParentLink(id3, AuthTarget.FOLDER, id));
                                }},
                                new HashSet<Authorization>() {{
                                    add(new Authorization("admin", AuthType.PRIVATE));
                                }},
                                null));

                fileService.saveMetadata(sysProfile.getIconMap().get("SYS_LOGO"),
                        new Metadata("application/octet-stream",
                                new HashSet<ParentLink>() {{
                                    add(new ParentLink(gId1, AuthTarget.FOLDER, gId));
                                }},
                                new HashSet<Authorization>() {{
                                    add(new Authorization("administrators", AuthType.GROUP));
                                }},
                                null));
                fileService.addParent(sysProfile.getIconMap().get("SYS_LOGO"),
                        new ParentLink(gId, AuthTarget.DISK, gId));*/
                // ==================测试数据结束

                // 初始化成功标记
                sysProfile.setInitialized(true);
            }
            sysProfile.setStartup(new Date());
            configService.saveSysProfile(sysProfile);
        };
    }

}
