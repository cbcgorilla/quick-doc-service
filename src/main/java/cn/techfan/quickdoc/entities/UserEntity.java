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
public class UserEntity {
    @Id
    private final String id;
    private String username;
    private String password;
    private String[] authorities = null;
}
