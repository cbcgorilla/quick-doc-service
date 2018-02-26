package cn.mxleader.quickdoc.security.handler;

import cn.mxleader.quickdoc.entities.ErrorResponse;
import cn.mxleader.quickdoc.entities.RestResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final Logger log = LoggerFactory.getLogger(ApiAccessDeniedHandler.class);

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exp) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            log.warn("用户: " + auth.getName()
                    + " 尝试访问受保护路径 URL: "
                    + request.getRequestURI()
                    + "\n异常信息："
                    + exp.getMessage());
        }

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        RestResponse entity = new ErrorResponse(0,
                "访问路径：" + request.getRequestURI() +
                        "无授权，请核对您访问的路径与权限是否匹配！");
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), entity);
    }
}