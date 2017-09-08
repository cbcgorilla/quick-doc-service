package cn.techfan.quickdoc.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class CategoryReplaceModel {
    private String oldType;
    private String newType;
}
