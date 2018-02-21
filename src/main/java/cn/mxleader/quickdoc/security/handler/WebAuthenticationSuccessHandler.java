package cn.mxleader.quickdoc.security.handler;

import cn.mxleader.quickdoc.entities.QuickDocUser;
import cn.mxleader.quickdoc.security.entities.ActiveUser;
import cn.mxleader.quickdoc.service.ReactiveUserService;
import cn.mxleader.quickdoc.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;

import static cn.mxleader.quickdoc.common.CommonCode.SESSION_USER;

@Component("webAuthenticationSuccessHandler")
public class WebAuthenticationSuccessHandler implements
        AuthenticationSuccessHandler {

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Autowired
    private ReactiveUserService reactiveUserService;

    @Autowired
    private StreamService streamService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.setMaxInactiveInterval(30 * 60);
            String[] userGroups = reactiveUserService.findUser(authentication.getName())
                    .block().getGroups();
            session.setAttribute(SESSION_USER, new ActiveUser(authentication.getName(),
                    userGroups,
                    authentication.getAuthorities()));
            // 发送用户登录消息到平台MQ
            streamService.sendMessage(" [User login ] username: " + authentication.getName());
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
            if (grantedAuthority.getAuthority().equals(QuickDocUser.Authorities.ADMIN.name())) {
                isAdmin = true;
                break;
            }
            if (grantedAuthority.getAuthority().equals(QuickDocUser.Authorities.USER.name())) {
                isUser = true;
                break;
            }
        }
        if (isAdmin) {
            return "/users";
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