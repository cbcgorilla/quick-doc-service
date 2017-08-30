package com.neofinance.quickdoc.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.neofinance.quickdoc.repository.ReactiveFileEntityRepository;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class GridFsService {

    private final GridFsTemplate gridFsTemplate;

    public GridFsService(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    /**
     * 存储文件， 如同名文件已存在则更新文件内容
     *
     * @param file        输入流文件
     * @param filename    输入文件名
     * @param contentType 文件类型
     * @return
     */
    public boolean storeFile(InputStream file, String filename, String contentType) {
        // 删除库中同名历史文件
        deleteFile(filename);
        gridFsTemplate.store(file, filename, contentType);
        return true;
    }

    /**
     * 删除Mongo库内文件
     *
     * @param filename 文件名
     * @return
     */
    public void deleteFile(String filename) {
        Optional<GridFSFile> existing = checkStoredFile(filename);
        if (existing.isPresent()) {
            gridFsTemplate.delete(filenameQuery(filename));
        }
    }

    public Stream<String> loadAllFilenames() {
        return StreamSupport.stream(
                gridFsTemplate.find(null)
                        .map(GridFSFile::getFilename)
                        .spliterator(), false);
    }

    public GridFSFile getFileDescription(String name) {
        Optional<GridFSFile> optionalCreated = checkStoredFile(name);
        if (optionalCreated.isPresent()) {
            return optionalCreated.get();
        } else {
            return null;
        }
    }

    public GridFsResource getFileResource(String filename) {
        return gridFsTemplate.getResource(filename);
    }

    private Optional<GridFSFile> checkStoredFile(String name) {
        GridFSFile file = gridFsTemplate.findOne(filenameQuery(name));
        return Optional.ofNullable(file);
    }

    private static Query filenameQuery(String name) {
        return Query.query(GridFsCriteria.whereFilename().is(name));
    }
}
