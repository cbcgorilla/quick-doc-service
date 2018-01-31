package cn.mxleader.quickdoc.config;

import cn.mxleader.quickdoc.service.QuickDocConfigService;
import cn.mxleader.quickdoc.service.ReactiveDirectoryService;
import cn.mxleader.quickdoc.service.ReactiveFileService;
import cn.mxleader.quickdoc.service.TensorFlowService;
import cn.mxleader.quickdoc.service.impl.TensorFlowServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
@ConditionalOnClass(TensorFlowService.class)
@EnableConfigurationProperties(TensorFlowProperties.class)
public class TensorFlowConfiguration {
    private final Logger log = LoggerFactory.getLogger(TensorFlowConfiguration.class);
    private final TensorFlowProperties tensorFlowProperties;

    public TensorFlowConfiguration(TensorFlowProperties tensorFlowProperties) {
        this.tensorFlowProperties = tensorFlowProperties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "quickdoc.tensorflow", value = "model-dir")
    @Autowired
    public TensorFlowService tensorFlowService(ReactiveFileService reactiveFileService,
                                               QuickDocConfigService quickDocConfigService,
                                               ReactiveDirectoryService reactiveDirectoryService) {
        /*ObjectId rootParentId = quickDocConfigService.getQuickDocConfig().getId();
        Optional<FsDirectory> fsDirectory = reactiveDirectoryService
                .findByPathAndParentId("config", rootParentId).blockOptional();

        File directory = new File(tensorFlowProperties.getModelDir());
        if (fsDirectory.isPresent() && directory.exists() && directory.isDirectory()) {
            Flux.just(directory.listFiles())
                    .map(
                            file -> {
                                try {
                                    FsDescription fsDescription = new FsDescription(ObjectId.get(),
                                            file.getName(),
                                            file.length(),
                                            StringUtils.getFilenameExtension(file.getName()).toLowerCase(),
                                            new Date(),
                                            ObjectId.get(),
                                            fsDirectory.get().getId(),
                                            ObjectId.get(),
                                            false,
                                            new FsOwner[]{new FsOwner("admin",
                                                    FsOwner.Type.TYPE_PRIVATE, 7)},
                                            null);
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
        }*/
        return new TensorFlowServiceImpl(reactiveFileService, tensorFlowProperties.getModelDir());
    }

}
