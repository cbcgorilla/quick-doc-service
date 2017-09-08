package cn.techfan.quickdoc.web.dto;

import cn.techfan.quickdoc.entities.FsOwner;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class WebDirectory {
    @Id
    private Long id;
    private String path;
    private Long parentId;
    private FsOwner[] owners = null;
    private Long childrenCount;
}