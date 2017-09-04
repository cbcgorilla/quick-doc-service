package cn.techfan.quickdoc.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Bean
    public WebAuthenticationSuccessHandler getSuccessHandler() {
        return new WebAuthenticationSuccessHandler();
    }

    @Autowired
    private WebAuthenticationProvider authProvider;

    @Override
    protected void configure(
            AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @TODO 启用用csrf特性
        //  1. 删除 .csrf().disable() ,
        //  2. 涉及POST操作的HTML页面添加如下内容：
        //     head段： <meta name="_csrf_header" th:content="${_csrf.headerName}"/>
        //     form段： <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
        http.csrf().disable().authorizeRequests()
                .antMatchers("/config-api", "/config-api/**/*").permitAll()
                .antMatchers("/management/**/*").hasAuthority("ADMIN")
                .anyRequest().authenticated()
                .and()
                //.httpBasic()
                .formLogin()
                .loginPage("/login")
                .successHandler(getSuccessHandler())
                .permitAll()
                .and()
                .logout()
                .permitAll();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // 针对 css 和 images 资源忽略认证
        web.ignoring()
                .antMatchers(HttpMethod.GET, "/css/**/*")
                .antMatchers(HttpMethod.GET, "/images/**/*");
    }

}