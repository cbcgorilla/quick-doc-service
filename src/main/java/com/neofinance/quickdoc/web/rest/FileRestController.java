package com.neofinance.quickdoc.web.rest;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.neofinance.quickdoc.common.entities.ApiResponseEntity;
import com.neofinance.quickdoc.service.GridFsService;
import com.neofinance.quickdoc.web.mvc.FileController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

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
