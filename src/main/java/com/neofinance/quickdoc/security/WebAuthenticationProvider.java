package com.neofinance.quickdoc.security;

import com.neofinance.quickdoc.common.entities.WebUser;
import com.neofinance.quickdoc.service.UserAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
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
        WebUser webUser = userAuthenticationService.validateUser(name, password).block();
        if (webUser.getAuthorities() != null) {

            // use the credentials
            // and authenticate against the third-party system
            List<WebAuthority> list = Stream.of(webUser.getAuthorities())
                    .map(v -> new WebAuthority(v))
                    .collect(Collectors.toList());
            return new UsernamePasswordAuthenticationToken(name, password, list);
        } else {
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(
                UsernamePasswordAuthenticationToken.class);
    }
}
