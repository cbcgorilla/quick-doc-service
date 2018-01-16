package cn.mxleader.quickdoc.security.handler;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

//@ TODO 此部分代码未启用。
@Component("webAuthenticationFailureHandler")
public class WebAuthenticationFailureHandler implements AuthenticationFailureHandler{

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException e) throws IOException, ServletException {

        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        redirectStrategy.sendRedirect(request, response, "/login");
    }
}
