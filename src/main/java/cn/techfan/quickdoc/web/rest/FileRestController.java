package cn.techfan.quickdoc.web.rest;

import cn.techfan.quickdoc.common.entities.ApiResponseEntity;
import com.mongodb.client.gridfs.model.GridFSFile;
import cn.techfan.quickdoc.service.GridFsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/files-api")
public class FileRestController {

    private static final String ACTION_GET_FILE_LIST = "获取文件清单";

    private final GridFsService gridFsService;

    @Autowired
    FileRestController(GridFsService gridFsService) {
        this.gridFsService = gridFsService;
    }

    @RequestMapping("/")
    public Flux<ApiResponseEntity> getFiles() {
        return Flux.fromStream(gridFsService.loadAllFilenames())
                .map(filename -> new ApiResponseEntity<String>(
                        ACTION_GET_FILE_LIST,
                        ApiResponseEntity.Code.SUCCESS,
                        filename));
    }

    @RequestMapping("/{filename}")
    public Mono<GridFSFile> getFile(@PathVariable String filename) {
        return Mono.just(gridFsService.getFileDescription(filename));
    }

}
