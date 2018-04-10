package cn.mxleader.quickdoc.web.config;

import cn.mxleader.quickdoc.security.exp.PreAuthException;
import cn.mxleader.quickdoc.web.domain.LayuiData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@RestController
public class WebExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(PreAuthException.class)
    public final ResponseEntity<LayuiData>
    handleAllExceptions(PreAuthException exp, WebRequest request) {
        LayuiData<String> resp = new LayuiData<>(-1,
                exp.getPreAuthMessage(),
                0, request.getRemoteUser());
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}
