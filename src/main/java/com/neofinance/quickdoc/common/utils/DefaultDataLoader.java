package com.neofinance.quickdoc.common.utils;

import com.neofinance.quickdoc.common.entities.FsOwner;
import com.neofinance.quickdoc.service.CategoryService;
import com.neofinance.quickdoc.service.DirectoryService;
import com.neofinance.quickdoc.service.GridFsService;
import lombok.extern.java.Log;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;

@Log
@SpringBootConfiguration
public class DefaultDataLoader {

    @Bean
    CommandLineRunner initCategory(CategoryService categoryService) {
        return args -> {
            for (int i = 0; i < 10; i++) {
                Thread.sleep(280);
                categoryService.addCategory("科技").subscribe();
            }
            categoryService.findByType("科技").subscribe(System.out::println);

            // 更改分类名
            categoryService.renameCategory("科技", "人文")
                    .onErrorMap(v -> {
                        log.log(Level.WARNING, v.getMessage());
                        return v;
                    })
                    .subscribe(System.out::println);

            // 删除分类
            categoryService.deleteCategory("人文俩hi")
                    .onErrorMap(v -> {
                        log.log(Level.WARNING, v.getMessage());
                        return v;
                    })
                    .subscribe(System.out::println);
        };
    }

    //Bean
    CommandLineRunner initGridFs(GridFsService gridFsService) {
        return args -> {
            File directory = new File("E:\\Download");
            if (directory.exists() && directory.isDirectory()) {
                Flux.just(directory.listFiles())
                        .filter(file -> file.getName().toLowerCase().endsWith(".pdf"))
                        .map(file -> {
                                    try {
                                        gridFsService.storeFile(
                                                new FileInputStream(file),
                                                file.getName(),
                                                "PDF");
                                    } catch (IOException exp) {
                                        exp.printStackTrace();
                                    }
                                    return file;
                                }
                        ).subscribe(System.out::println);
            }
        };
    }

    @Bean
    CommandLineRunner initDirectory(DirectoryService directoryService) {
        return args -> {
            for (int i = 0; i < 10; i++) {
                Thread.sleep(280);
                directoryService.addDirectory("root", null, null).subscribe();
                directoryService.addDirectory("michael", null, null).subscribe();
                directoryService.addDirectory("chenbichao", null, null).subscribe();
            }
            // 对所有跟目录新增子目录, 并修改子目录属主
            directoryService.allRootDirectories()
                    .flatMap(v -> {
                                return directoryService.updateFsOwners(v, getRandomOwners());
                            }
                    )
                    .flatMap(parent -> directoryService.addDirectory("sub", parent, null)
                            .flatMap(
                                    v -> {
                                        return directoryService.updateFsOwners(v, getRandomOwners());
                                    }
                            )
                    )
                    .onErrorMap(v -> {
                        log.log(Level.WARNING, v.getMessage());
                        return v;
                    })
                    .subscribe(System.out::println);

            // 重命名 michael 目录
            directoryService.findByPathAndParent("michael", 0L)
                    .flatMap(directory -> directoryService.renameDirectory(directory, "michael new"))
                    .onErrorMap(v -> {
                        log.log(Level.WARNING, v.getMessage());
                        return v;
                    })
                    .subscribe(System.out::println);

            // 删除 chenbichao/sub子目录
            directoryService.findByPathAndParent("sub", 1503973850289331809L)
                    .flatMap(directoryService::deleteDirectory)
                    .onErrorMap(v -> {
                        log.log(Level.WARNING, v.getMessage());
                        return v;
                    })
                    .subscribe(System.out::println);

            // 删除 chenbichao子目录
            directoryService.findByPathAndParent("chenbichao", 0L)
                    .flatMap(directoryService::deleteDirectory)
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
