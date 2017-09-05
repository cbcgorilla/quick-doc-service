package cn.techfan.quickdoc.common.data;

import cn.techfan.quickdoc.common.entities.FsEntity;
import cn.techfan.quickdoc.common.entities.FsOwner;
import cn.techfan.quickdoc.common.entities.WebUser;
import cn.techfan.quickdoc.common.utils.KeyUtil;
import cn.techfan.quickdoc.service.*;
import lombok.extern.java.Log;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;

@Log
@SpringBootConfiguration
public class DefaultDataLoader {

    @Bean
    CommandLineRunner initAdminUser(UserAuthenticationService userAuthenticationService) {
        return args -> {
            // 初始化Admin用户
            userAuthenticationService
                    .saveUser(new WebUser(KeyUtil.stringUUID(),
                                    "admin",
                                    "chenbichao",
                                    new String[]{"ADMIN", "USER"}),
                            true)
                    .subscribe(v->log.info(v.toString()));
        };
    }

    @Bean
    CommandLineRunner initDirectory(ReactiveDirectoryService reactiveDirectoryService) {
        return args -> {
            reactiveDirectoryService.addDirectory("root", null, null).subscribe();
            // 对所有跟目录新增子目录, 并修改子目录属主
            reactiveDirectoryService.allRootDirectories()
                    .flatMap(v -> {
                                return reactiveDirectoryService.updateFsOwners(v, getRandomOwners());
                            }
                    )
                    .flatMap(parent -> reactiveDirectoryService.addDirectory("service-desk", parent, null)
                            .flatMap(
                                    v -> {
                                        return reactiveDirectoryService.updateFsOwners(v, getRandomOwners());
                                    }
                            )
                    )
                    .onErrorMap(v -> {
                        log.log(Level.WARNING, v.getMessage());
                        return v;
                    })
                    .subscribe(v->log.info(v.toString()));

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
                        log.log(Level.WARNING, v.getMessage());
                        return v;
                    })
                    .subscribe(System.out::println);

            // 删除分类
            reactiveCategoryService.deleteCategory("人文俩hi")
                    .onErrorMap(v -> {
                        log.log(Level.WARNING, v.getMessage());
                        return v;
                    })
                    .subscribe(System.out::println);
        };
    }

    // @Bean
    CommandLineRunner initGridFs(GridFsService gridFsService, ReactiveFileService reactiveFileService) {
        return args -> {
            File directory = new File("E:\\BaiduYunDownload\\发展路线图");
            if (directory.exists() && directory.isDirectory()) {
                Flux.just(directory.listFiles())
                        .filter(file -> file.getName().toLowerCase().endsWith(".pdf"))
                        .map(
                                file -> {
                                    try {
                                        FsEntity fsEntity = new FsEntity(KeyUtil.getSHA256UUID(),
                                                file.getName(),
                                                file.length(),
                                                "PDF",
                                                new Date(),
                                                1503903557505L,
                                                1504012777983983801L,
                                                null,
                                                null,
                                                null,
                                                null);
                                        reactiveFileService.storeFile(
                                                fsEntity,
                                                new FileInputStream(file))
                                                .subscribe();
                                    } catch (IOException exp) {
                                        exp.printStackTrace();
                                    }
                                    return file;
                                }
                        ).subscribe(v->log.info(v.toString()));
            }
        };
    }

    // @Bean
    CommandLineRunner testDirectory(ReactiveDirectoryService reactiveDirectoryService) {
        return args -> {
            for (int i = 0; i < 10; i++) {
                Thread.sleep(280);
                reactiveDirectoryService.addDirectory("root", null, null).subscribe();
                reactiveDirectoryService.addDirectory("michael", null, null).subscribe();
                reactiveDirectoryService.addDirectory("chenbichao", null, null).subscribe();
            }

            // 重命名 michael 目录
            reactiveDirectoryService.findByPathAndParentId("michael", 0L)
                    .flatMap(directory -> reactiveDirectoryService.renameDirectory(directory, "michael new"))
                    .onErrorMap(v -> {
                        log.log(Level.WARNING, v.getMessage());
                        return v;
                    })
                    .subscribe(v->log.info(v.toString()));

            // 删除 chenbichao/sub子目录
            reactiveDirectoryService.findByPathAndParentId("sub", 1503973850289331809L)
                    .flatMap(reactiveDirectoryService::deleteDirectory)
                    .onErrorMap(v -> {
                        log.log(Level.WARNING, v.getMessage());
                        return v;
                    })
                    .subscribe(v->log.info(v.toString()));

            // 删除 chenbichao子目录
            reactiveDirectoryService.findByPathAndParentId("chenbichao", 0L)
                    .flatMap(reactiveDirectoryService::deleteDirectory)
                    .onErrorMap(v -> {
                        log.log(Level.WARNING, v.getMessage());
                        return v;
                    })
                    .subscribe(v->log.info(v.toString()));
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
