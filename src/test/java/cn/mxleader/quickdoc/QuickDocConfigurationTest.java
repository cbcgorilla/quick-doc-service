package cn.mxleader.quickdoc;

import cn.mxleader.quickdoc.entities.FsDetail;
import cn.mxleader.quickdoc.entities.FsOwner;
import cn.mxleader.quickdoc.service.KafkaService;
import cn.mxleader.quickdoc.service.ReactiveCategoryService;
import cn.mxleader.quickdoc.service.ReactiveFileService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

@RunWith(JUnitPlatform.class)
@SpringBootTest(classes = QuickDocTaskExecutorTestConfig.class)
@ExtendWith(SpringExtension.class)
public class QuickDocConfigurationTest {

    private final Logger log = LoggerFactory.getLogger(QuickDocConfigurationTest.class);

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private AsyncServiceTest asyncServiceTest;

    @Test
    @DisplayName("AsyncService Test")
    public void setAsyncServiceTest() {
        System.out.println("Hello.....................");
        asyncServiceTest.sendMessage("ddd");
    }

    @Test
    @DisplayName("Kafka Service Test")
    public void kafkaServiceTest() {
        kafkaService.sendMessage("Hello Message from JUnit........");
        log.info("Hello Message has benn sent out...");
    }

    @Test
    @DisplayName("Kafka Service Topic Test")
    public void kafkaServiceTopicTest() {
        kafkaService.sendMessage("active-session", "Hello Message from JUnit........");
        log.info("Hello Message has benn sent out...");
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
                                        FsDetail fsDetail = new FsDetail(ObjectId.get(),
                                                file.getName(),
                                                file.length(),
                                                StringUtils.getFilenameExtension(file.getName()).toLowerCase(),
                                                new Date(),
                                                ObjectId.get(),
                                                ObjectId.get(),
                                                null,
                                                false,
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
        owners[2] = new FsOwner("administrators", FsOwner.Type.TYPE_GROUP, 7);
        return owners;
    }


}
