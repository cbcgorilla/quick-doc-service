package cn.techfan.quickdoc.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@AllArgsConstructor
@ToString
@Data
public class FsDirectory {
    @Id
    private final Long id;
    private String path;
    private Long parentId;
    private FsOwner[] owners = null;
}