package cn.mxleader.quickdoc;

import cn.mxleader.quickdoc.common.utils.FileUtils;
import cn.mxleader.quickdoc.entities.AccessAuthorization;
import cn.mxleader.quickdoc.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    CommandLineRunner uploadLocalFiles(FileService fileService) {
        return args -> {
            File folder = new File("E:\\IT服务台管理\\报告材料\\排班");
            if (folder.exists() && folder.isDirectory()) {
                Flux.just(folder.listFiles())
                        .filter(file -> file.getName().toLowerCase().endsWith(".xlsx"))
                        .map(
                                file -> {
                                    try {
                                        String fileType = FileUtils.guessMimeType(file.getName());
                                       /* FileMetadata metadata = new FileMetadata("音乐",
                                                new ObjectId("5a6966f7ae3e442518745836"),
                                                false,
                                                getRandomOwners(), null);
*/
                                        fileService.store(new FileInputStream(file), file.getName(), fileType);
                                    } catch (IOException exp) {
                                        exp.printStackTrace();
                                    }
                                    return file;
                                }
                        ).subscribe(v -> log.info(v.toString()));
            }
        };
    }

    private AccessAuthorization[] getRandomOwners() {
        AccessAuthorization owners[] = new AccessAuthorization[3];
        owners[0] = new AccessAuthorization("chenbichao", AccessAuthorization.Type.TYPE_PRIVATE, 7);
        owners[1] = new AccessAuthorization("陈毕超", AccessAuthorization.Type.TYPE_PRIVATE, 7);
        owners[2] = new AccessAuthorization("administrators", AccessAuthorization.Type.TYPE_GROUP, 7);
        return owners;
    }

}
