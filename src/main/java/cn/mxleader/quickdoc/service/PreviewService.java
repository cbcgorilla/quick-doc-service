package cn.mxleader.quickdoc.service;

import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpEntity;

import java.io.IOException;

public interface PreviewService {

    HttpEntity<byte[]> getEntity(GridFsResource fs) throws IOException;
}
