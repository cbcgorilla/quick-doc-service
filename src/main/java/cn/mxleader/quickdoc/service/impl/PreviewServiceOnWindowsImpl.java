package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.service.PreviewService;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;

public class PreviewServiceOnWindowsImpl implements PreviewService {
    @Override
    public HttpEntity<byte[]> getEntity(GridFsResource fs) throws IOException {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.valueOf(fs.getContentType()));
        header.set("Content-Disposition",
                "inline; filename=" + java.net.URLEncoder.encode(fs.getFilename(), "UTF-8"));
        byte[] document = FileCopyUtils.copyToByteArray(fs.getInputStream());
        header.setContentLength(document.length);
        return new HttpEntity<>(document, header);
    }
}
