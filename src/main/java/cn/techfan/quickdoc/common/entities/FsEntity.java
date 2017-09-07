package cn.techfan.quickdoc.common.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document
@AllArgsConstructor
@ToString
@Data
public class FsEntity {
    @Id
    private String id;
    private String filename;
    private Long contentLength;
    private String contentType;
    private Date uploadDate;
    private Long categoryId;
    private Long directoryId;
    private ObjectId storedId = null;
    private FsOwner[] owners = null;
    private String category = null;
    private String directory = null;
}
