package cn.mxleader.quickdoc;

import cn.mxleader.quickdoc.entities.FsDescription;
import cn.mxleader.quickdoc.entities.FsOwner;
import cn.mxleader.quickdoc.service.ReactiveCategoryService;
import cn.mxleader.quickdoc.service.ReactiveFileService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@TestConfiguration
@EnableAsync
public class QuickDocTaskExecutorTestConfig {

    private final Logger log = LoggerFactory.getLogger(QuickDocTaskExecutorTestConfig.class);

    @Autowired
    public AsyncServiceTest asyncServiceTest;

    //@Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(5);
        executor.setKeepAliveSeconds(3000);
        executor.setThreadNamePrefix("MyExecutor-");

        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是由调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
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

    @Bean
    CommandLineRunner uploadLocalFiles(ReactiveFileService reactiveFileService) {
        return args -> {
            File directory = new File("E:\\IT服务台管理\\报告材料\\排班");
            if (directory.exists() && directory.isDirectory()) {
                Flux.just(directory.listFiles())
                        .filter(file -> file.getName().toLowerCase().endsWith(".xlsx"))
                        .map(
                                file -> {
                                    try {
                                        FsDescription fsDescription = new FsDescription(ObjectId.get(),
                                                file.getName(),
                                                file.length(),
                                                StringUtils.getFilenameExtension(file.getName()).toLowerCase(),
                                                new Date(),
                                                new ObjectId("5a6966f7ae3e442518745833"),
                                                new ObjectId("5a6966f7ae3e442518745836"),
                                                ObjectId.get(),
                                                false,
                                                getRandomOwners());
                                        reactiveFileService.storeFile(
                                                fsDescription,
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
        owners[2] = new FsOwner("administrators", FsOwner.Type.TYPE_GROUP, 7);
        return owners;
    }

}
