package cn.techfan.quickdoc.common.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Data
public class ApiResponseEntity<T> {
    public enum Code {
        SUCCESS, FAIL, ERROR
    }

    private String action;
    private Code code;
    private T result;
}
