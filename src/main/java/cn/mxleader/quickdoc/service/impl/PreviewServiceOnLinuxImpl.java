package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.service.PreviewService;
import org.jodconverter.DocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.office.OfficeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PreviewServiceOnLinuxImpl implements PreviewService {
    private final Logger log = LoggerFactory.getLogger(PreviewServiceOnLinuxImpl.class);
    private final DocumentConverter converter;

    public PreviewServiceOnLinuxImpl(DocumentConverter converter){
        this.converter = converter;
    }
    @Override
    public HttpEntity<byte[]> getEntity(GridFsResource fs) throws IOException {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.valueOf(fs.getContentType()));
        header.set("Content-Disposition",
                "inline; filename=" + java.net.URLEncoder.encode(fs.getFilename(), "UTF-8"));
        byte[] document;
        String type = fs.getContentType();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        if (type.startsWith("application/vnd.") || type.startsWith("application/ms")) {
            try {
                converter.convert(fs.getInputStream()).as(DefaultDocumentFormatRegistry.getFormatByMediaType(type))
                        .to(bout).as(DefaultDocumentFormatRegistry.PDF)
                        .execute();
                header.setContentType(MediaType.APPLICATION_PDF);
                document = bout.toByteArray();
            }catch(OfficeException exp){
                document = FileCopyUtils.copyToByteArray(fs.getInputStream());
                log.error(exp.getMessage());
            }
        } else {
            document = FileCopyUtils.copyToByteArray(fs.getInputStream());
        }
        header.setContentLength(document.length);
        return new HttpEntity<>(document, header);
    }
}
