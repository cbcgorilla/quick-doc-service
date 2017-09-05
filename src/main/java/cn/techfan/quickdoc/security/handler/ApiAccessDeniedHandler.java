package cn.techfan.quickdoc.security.handler;

import cn.techfan.quickdoc.common.entities.ApiResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Log
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exc) throws IOException, ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            log.warning("用户: " + auth.getName()
                    + " 尝试访问受保护路径 URL: "
                    + request.getRequestURI());
        }

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        ApiResponseEntity<String> entity =
                new ApiResponseEntity<>("访问路径：" + request.getRequestURI(),
                        ApiResponseEntity.Code.ERROR,
                        "无授权，请核对您访问的路径与权限是否匹配！");
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), entity);
    }
}