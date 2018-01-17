package cn.mxleader.quickdoc.security.authprovider;

import cn.mxleader.quickdoc.entities.UserEntity;
import cn.mxleader.quickdoc.security.model.WebAuthority;
import cn.mxleader.quickdoc.service.ReactiveUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class WebAuthenticationProvider
        implements AuthenticationProvider {

    @Autowired
    private ReactiveUserService reactiveUserService;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String name = authentication.getName();
        String password = authentication.getCredentials().toString();
        UserEntity userEntity = reactiveUserService
                .validateUser(name, password)
                /*.onErrorMap(v -> {
                    if (v.getClass().equals(UsernameNotFoundException.class)) {
                        throw new UsernameNotFoundException(v.getMessage());
                    } else if (v.getClass().equals(BadCredentialsException.class)) {
                        throw new BadCredentialsException(v.getMessage());
                    }
                    return v;
                })*/
                .block();
        return new UsernamePasswordAuthenticationToken(userEntity.getUsername(),
                userEntity.getPassword(),
                Stream.of(userEntity.getAuthorities())
                        .map(v -> new WebAuthority(v))
                        .collect(Collectors.toList()));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(
                UsernamePasswordAuthenticationToken.class);
    }
}
