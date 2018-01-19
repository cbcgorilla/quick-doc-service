package cn.mxleader.quickdoc.common.config;

import cn.mxleader.quickdoc.common.utils.KeyUtil;
import cn.mxleader.quickdoc.entities.FsDetail;
import cn.mxleader.quickdoc.entities.FsOwner;
import cn.mxleader.quickdoc.entities.UserEntity;
import cn.mxleader.quickdoc.service.*;
import cn.mxleader.quickdoc.service.impl.ReactiveUserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import static cn.mxleader.quickdoc.common.CommonCode.SYSTEM_ADMIN_GROUP_OWNER;
import static cn.mxleader.quickdoc.common.CommonCode.SYSTEM_PUBLIC_OWNER;
import static cn.mxleader.quickdoc.common.utils.KeyUtil.getSHA256UUID;
import static cn.mxleader.quickdoc.security.config.WebSecurityConfig.AUTHORITY_ADMIN;
import static cn.mxleader.quickdoc.security.config.WebSecurityConfig.AUTHORITY_USER;

@SpringBootConfiguration
public class QuickDocConfiguration {
    private final Logger log = LoggerFactory.getLogger(QuickDocConfiguration.class);

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
                                    .saveUser(new UserEntity(KeyUtil.stringUUID(), "admin",
                                            "chenbichao",
                                            new String[]{AUTHORITY_ADMIN, AUTHORITY_USER}, "admin")).subscribe();

                            // 初始化文件分类
                            reactiveCategoryService.addCategory("照片").subscribe();
                            reactiveCategoryService.addCategory("音乐").subscribe();
                            reactiveCategoryService.addCategory("图书").subscribe();
                            reactiveCategoryService.addCategory("视频").subscribe();

                            // 初始化根目录
                            FsOwner[] configOwners = {SYSTEM_ADMIN_GROUP_OWNER};
                            reactiveDirectoryService.saveDirectory("config", 0L,
                                    configOwners).subscribe();

                            FsOwner[] rootOwners = {SYSTEM_PUBLIC_OWNER, SYSTEM_ADMIN_GROUP_OWNER};
                            reactiveDirectoryService.saveDirectory("root", 0L,
                                    rootOwners).subscribe();

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

    //@Bean
    CommandLineRunner initCategory(ReactiveCategoryService reactiveCategoryService) {
        return args -> {
            for (int i = 0; i < 10; i++) {
                Thread.sleep(280);
                reactiveCategoryService.addCategory("科技").subscribe();
            }
            reactiveCategoryService.findByType("科技").subscribe(System.out::println);

            // 更改分类名
            reactiveCategoryService.renameCategory("科技", "人文")
                    .onErrorMap(v -> {
                        log.warn(v.getMessage());
                        return v;
                    })
                    .subscribe(System.out::println);

            // 删除分类
            reactiveCategoryService.deleteCategory("人文俩hi")
                    .onErrorMap(v -> {
                        log.warn(v.getMessage());
                        return v;
                    })
                    .subscribe(System.out::println);
        };
    }

    //@Bean
    CommandLineRunner uploadLocalFiles(ReactiveFileService reactiveFileService) {
        return args -> {
            File directory = new File("E:\\BaiduYunDownload\\发展路线图");
            if (directory.exists() && directory.isDirectory()) {
                Flux.just(directory.listFiles())
                        .filter(file -> file.getName().toLowerCase().endsWith(".pdf"))
                        .map(
                                file -> {
                                    try {
                                        FsDetail fsDetail = new FsDetail(getSHA256UUID(),
                                                file.getName(),
                                                file.length(),
                                                StringUtils.getFilenameExtension(file.getName()).toLowerCase(),
                                                new Date(),
                                                15162005167600724L,
                                                15162005167728678L,
                                                null,
                                                getRandomOwners(),
                                                null,
                                                null);
                                        reactiveFileService.storeFile(
                                                fsDetail,
                                                new FileInputStream(file))
                                                .subscribe();
                                    } catch (IOException exp) {
                                        exp.printStackTrace();
                                    }
                                    return file;
                                }
                        ).subscribe(v -> log.info(v.toString()));
            }
        };
    }

    private FsOwner[] getRandomOwners() {
        FsOwner owners[] = new FsOwner[3];
        owners[0] = new FsOwner("chenbichao", FsOwner.Type.TYPE_PRIVATE, 7);
        owners[1] = new FsOwner("陈毕超", FsOwner.Type.TYPE_PRIVATE, 7);
        owners[2] = new FsOwner("Michael Chen", FsOwner.Type.TYPE_PUBLIC, 7);
        return owners;
    }

}
