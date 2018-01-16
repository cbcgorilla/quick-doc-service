package cn.mxleader.quickdoc.common.config;

import cn.mxleader.quickdoc.common.utils.KeyUtil;
import cn.mxleader.quickdoc.entities.FsDetail;
import cn.mxleader.quickdoc.entities.FsOwner;
import cn.mxleader.quickdoc.entities.UserEntity;
import cn.mxleader.quickdoc.service.*;
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

import static cn.mxleader.quickdoc.common.utils.KeyUtil.getSHA256UUID;

@SpringBootConfiguration
public class QuickDocDefaultConfiguration {
    private final Logger log = LoggerFactory.getLogger(QuickDocDefaultConfiguration.class);

    @Bean
    CommandLineRunner setupConfiguration(ReactiveUserService reactiveUserService,
                                         ReactiveCategoryService reactiveCategoryService,
                                         ReactiveDirectoryService reactiveDirectoryService,
                                         ReactiveQuickDocConfigService reactiveQuickDocConfigService) {
        return args -> {
            reactiveQuickDocConfigService.getQuickDocConfig()
                    .flatMap(quickDocConfig -> {
                        if (!quickDocConfig.getInitialized()) {
                            // 初始化Admin管理账号
                            reactiveUserService
                                    .saveUser(new UserEntity(null, "admin",
                                                    "chenbichao",
                                                    new String[]{"ADMIN","USER"})).subscribe();

                            // 初始化文件分类
                            reactiveCategoryService.addCategory("照片").subscribe();
                            reactiveCategoryService.addCategory("音乐").subscribe();
                            reactiveCategoryService.addCategory("图书").subscribe();
                            reactiveCategoryService.addCategory("视频").subscribe();

                            // 初始化根目录
                            reactiveDirectoryService.addDirectory("root", 0L,
                                    null, false).subscribe();

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
    CommandLineRunner initDirectory(ReactiveDirectoryService reactiveDirectoryService) {
        return args -> {
            reactiveDirectoryService.addDirectory("root", null, null, false).subscribe();
            // 对所有跟目录新增子目录, 并修改子目录属主
            reactiveDirectoryService.allRootDirectories()
                    .flatMap(v -> {
                                return reactiveDirectoryService.updateFsOwners(v, getRandomOwners());
                            }
                    )
                    .flatMap(parent -> reactiveDirectoryService.addDirectory("service-desk", parent.getId(), null, false)
                            .flatMap(
                                    v -> {
                                        return reactiveDirectoryService.updateFsOwners(v, getRandomOwners());
                                    }
                            )
                    )
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

    // @Bean
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
                                                1503903557505L,
                                                1504012777983983801L,
                                                null,
                                                null,
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

    // @Bean
    CommandLineRunner testDirectory(ReactiveDirectoryService reactiveDirectoryService) {
        return args -> {
            for (int i = 0; i < 10; i++) {
                Thread.sleep(280);
                reactiveDirectoryService.addDirectory("root", null, null, false).subscribe();
                reactiveDirectoryService.addDirectory("michael", null, null, false).subscribe();
                reactiveDirectoryService.addDirectory("chenbichao", null, null, false).subscribe();
            }

            // 重命名 michael 目录
            reactiveDirectoryService.findByPathAndParentId("michael", 0L)
                    .flatMap(directory -> reactiveDirectoryService.renameDirectory(directory, "michael new"))
                    .onErrorMap(v -> {
                        log.warn(v.getMessage());
                        return v;
                    })
                    .subscribe(v -> log.info(v.toString()));

            // 删除 chenbichao/sub子目录
            reactiveDirectoryService.findByPathAndParentId("sub", 1503973850289331809L)
                    .flatMap(reactiveDirectoryService::deleteDirectory)
                    .onErrorMap(v -> {
                        log.warn(v.getMessage());
                        return v;
                    })
                    .subscribe(v -> log.info(v.toString()));

            // 删除 chenbichao子目录
            reactiveDirectoryService.findByPathAndParentId("chenbichao", 0L)
                    .flatMap(reactiveDirectoryService::deleteDirectory)
                    .onErrorMap(v -> {
                        log.warn(v.getMessage());
                        return v;
                    })
                    .subscribe(v -> log.info(v.toString()));
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
