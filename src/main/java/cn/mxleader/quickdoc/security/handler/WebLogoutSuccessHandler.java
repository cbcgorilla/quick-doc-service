package cn.mxleader.quickdoc.security.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component("webLogoutSuccessHandler")
public class WebLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final String SESSION_USER = "ActiveUser";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        final HttpSession session = request.getSession();
        if (session != null) {
            session.removeAttribute(SESSION_USER);
            // 发送用户退出消息到Kafka平台
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String ctime = formatter.format(new Date());
            kafkaTemplate.send("active-session",
                    ctime + " [User logout] username: " + authentication.getName());
        }
        response.sendRedirect("/login?logout=true");
    }
}
