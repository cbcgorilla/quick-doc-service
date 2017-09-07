package cn.techfan.quickdoc.web.dto;

import cn.techfan.quickdoc.common.entities.FsOwner;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class WebEntity {
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
