package cn.mxleader.quickdoc.web.domain;

import cn.mxleader.quickdoc.entities.SysFolder;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import reactor.core.publisher.Flux;

public class EntityConverter {
/*
    public static Flux<WebFolder> switchToWebFormat(Flux<SysFolder> folderFlux) {
        return folderFlux.map(folder -> {
            WebFolder webFolder = new WebFolder();
            BeanUtils.copyProperties(folder, webFolder);
            Long dirCount = mongoTemplate.count(
                    Query.query(Criteria.where("parentId").is(folder.getId())),
                    SysFolder.class);
            Long filesCount = mongoTemplate.count(Query.query(GridFsCriteria
                            .whereMetaData("folderId").is(folder.getId())),
                    "fs.files");
            webFolder.setChildrenCount(dirCount + filesCount);
            return webFolder;
        });
    }*/

}
