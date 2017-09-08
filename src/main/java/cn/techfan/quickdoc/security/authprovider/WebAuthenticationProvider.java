package cn.techfan.quickdoc.security.authprovider;

import cn.techfan.quickdoc.entities.UserEntity;
import cn.techfan.quickdoc.security.model.WebAuthority;
import cn.techfan.quickdoc.service.UserAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class WebAuthenticationProvider
        implements AuthenticationProvider {

    @Autowired
    private UserAuthenticationService userAuthenticationService;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String name = authentication.getName();
        String password = authentication.getCredentials().toString();
        UserEntity userEntity = userAuthenticationService.validateUser(name, password).block();
        if (userEntity.getAuthorities() != null) {
            List<WebAuthority> list = Stream.of(userEntity.getAuthorities())
                    .map(v -> new WebAuthority(v))
                    .collect(Collectors.toList());
            return new UsernamePasswordAuthenticationToken(name, password, list);
        } else {
            throw new BadCredentialsException("用户名密码校验失败!");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(
                UsernamePasswordAuthenticationToken.class);
    }
}
