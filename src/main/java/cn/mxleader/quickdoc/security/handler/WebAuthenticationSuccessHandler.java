package cn.mxleader.quickdoc.security.handler;

import cn.mxleader.quickdoc.security.model.ActiveUser;
import cn.mxleader.quickdoc.service.ReactiveUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;

@Component("webAuthenticationSuccessHandler")
public class WebAuthenticationSuccessHandler implements
        AuthenticationSuccessHandler {

    private static final String SESSION_USER = "ActiveUser";
    private static final String AUTHORITY_ADMIN = "ADMIN";
    private static final String AUTHORITY_USER = "USER";

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Autowired
    private ReactiveUserService reactiveUserService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.setMaxInactiveInterval(30 * 60);
            String userGroup = reactiveUserService.findUser(authentication.getName())
                    .block().getGroup();
            session.setAttribute(SESSION_USER, new ActiveUser(authentication.getName(),
                    userGroup,
                    authentication.getAuthorities()));
        }
        redirectStrategy.sendRedirect(request, response, determineTargetUrl(authentication));
        clearAuthenticationAttributes(request);
    }

    protected void clearAuthenticationAttributes(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }

    protected String determineTargetUrl(Authentication authentication) {
        boolean isAdmin = false;
        boolean isUser = false;
        Collection<? extends GrantedAuthority> authorities = authentication
                .getAuthorities();
        for (GrantedAuthority grantedAuthority : authorities) {
            if (grantedAuthority.getAuthority().equals(AUTHORITY_ADMIN)) {
                isAdmin = true;
                break;
            }
            if (grantedAuthority.getAuthority().equals(AUTHORITY_USER)) {
                isUser = true;
                break;
            }
        }
        if (isAdmin) {
            return "/admin";
        } else if (isUser) {
            return "/";
        } else {
            throw new IllegalStateException();
        }
    }

    public void setRedirectStrategy(final RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
    }

    protected RedirectStrategy getRedirectStrategy() {
        return redirectStrategy;
    }
}