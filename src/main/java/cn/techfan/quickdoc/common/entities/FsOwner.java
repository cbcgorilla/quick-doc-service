package cn.techfan.quickdoc.common.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@AllArgsConstructor
@ToString
@Data
public class FsOwner {

    public enum Type {
        TYPE_PUBLIC, TYPE_PRIVATE
    }

    private String username;
    private Type type;
    private Integer privilege = 1;
}