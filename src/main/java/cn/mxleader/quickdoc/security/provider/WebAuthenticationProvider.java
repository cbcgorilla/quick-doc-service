package cn.mxleader.quickdoc.security.provider;

import cn.mxleader.quickdoc.common.UserLogonException;
import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class WebAuthenticationProvider
        implements AuthenticationProvider {

    @Autowired
    private UserService userService;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String name = authentication.getName();
        String password = authentication.getCredentials().toString();
        if (userService.validateUser(name, password)){
            SysUser sysUser = userService.findUser(name);
            return new UsernamePasswordAuthenticationToken(sysUser.getUsername(),
                    sysUser.getPassword(),
                    Arrays.stream(sysUser.getAuthorities())
                            .map(authority -> new WebAuthority(authority.name()))
                            .collect(Collectors.toList()));
        }else{
            throw new UserLogonException("登录错误，用户名或密码有误,请检查后重新输入！");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(
                UsernamePasswordAuthenticationToken.class);
    }
}
