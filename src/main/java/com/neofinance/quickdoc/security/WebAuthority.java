package com.neofinance.quickdoc.security;

import org.springframework.security.core.GrantedAuthority;

public class WebAuthority implements GrantedAuthority {
    private String authority;

    public WebAuthority(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority;
    }
}
