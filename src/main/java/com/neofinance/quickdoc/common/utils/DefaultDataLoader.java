package com.neofinance.quickdoc.common.utils;

import com.mongodb.MongoClient;
import com.neofinance.quickdoc.common.entities.FsEntity;
import com.neofinance.quickdoc.common.entities.FsOwner;
import com.neofinance.quickdoc.service.GridFsService;
import com.neofinance.quickdoc.service.ReactiveCategoryService;
import com.neofinance.quickdoc.service.ReactiveDirectoryService;
import com.neofinance.quickdoc.service.ReactiveFileService;
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
    CommandLineRunner defaultDatabase(MongoClient mongoClient, GridFsAssistant gridFsAssistant) {
        return args -> {
            //MongoDatabase database = mongoClient.getDatabase("school");
            //System.out.println(database.getName());
            //System.out.println("database from bean GridFsUtil: "+ gridFsAssistant.getMongoDB("school").getName());
        };
    }

    @Bean
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
                        ).subscribe(System.out::println);
            }
        };
    }

    //@Bean
    CommandLineRunner initDirectory(ReactiveDirectoryService reactiveDirectoryService) {
        return args -> {
            for (int i = 0; i < 10; i++) {
                Thread.sleep(280);
                reactiveDirectoryService.addDirectory("root", null, null).subscribe();
                reactiveDirectoryService.addDirectory("michael", null, null).subscribe();
                reactiveDirectoryService.addDirectory("chenbichao", null, null).subscribe();
            }
            // 对所有跟目录新增子目录, 并修改子目录属主
            reactiveDirectoryService.allRootDirectories()
                    .flatMap(v -> {
                                return reactiveDirectoryService.updateFsOwners(v, getRandomOwners());
                            }
                    )
                    .flatMap(parent -> reactiveDirectoryService.addDirectory("sub", parent, null)
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
                    .subscribe(System.out::println);

            // 重命名 michael 目录
            reactiveDirectoryService.findByPathAndParentId("michael", 0L)
                    .flatMap(directory -> reactiveDirectoryService.renameDirectory(directory, "michael new"))
                    .onErrorMap(v -> {
                        log.log(Level.WARNING, v.getMessage());
                        return v;
                    })
                    .subscribe(System.out::println);

            // 删除 chenbichao/sub子目录
            reactiveDirectoryService.findByPathAndParentId("sub", 1503973850289331809L)
                    .flatMap(reactiveDirectoryService::deleteDirectory)
                    .onErrorMap(v -> {
                        log.log(Level.WARNING, v.getMessage());
                        return v;
                    })
                    .subscribe(System.out::println);

            // 删除 chenbichao子目录
            reactiveDirectoryService.findByPathAndParentId("chenbichao", 0L)
                    .flatMap(reactiveDirectoryService::deleteDirectory)
                    .onErrorMap(v -> {
                        log.log(Level.WARNING, v.getMessage());
                        return v;
                    })
                    .subscribe(System.out::println);
        };
    }

    private FsOwner[] getRandomOwners() {
        FsOwner owners[] = new FsOwner[3];
        owners[0] = new FsOwner("chenbichao", FsOwner.Type.TYPE_PRIVATE);
        owners[1] = new FsOwner("陈毕超", FsOwner.Type.TYPE_PRIVATE);
        owners[2] = new FsOwner("Michael Chen", FsOwner.Type.TYPE_PUBLIC);
        return owners;
    }

}
