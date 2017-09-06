package cn.techfan.quickdoc.security.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component("webLogoutSuccessHandler")
public class WebLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final String SESSION_USER = "ActiveUser";

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        final HttpSession session = request.getSession();
        if (session != null) {
            session.removeAttribute(SESSION_USER);
        }

        response.sendRedirect("/login.html?logout=true");
    }
}
