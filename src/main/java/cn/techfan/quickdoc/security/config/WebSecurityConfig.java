package cn.techfan.quickdoc.security.config;

import cn.techfan.quickdoc.security.handler.WebAuthenticationSuccessHandler;
import cn.techfan.quickdoc.security.authprovider.WebAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private WebAuthenticationProvider authProvider;

    @Override
    protected void configure(
            AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // 针对 css 和 images 资源忽略认证
        web.ignoring()
                .antMatchers(HttpMethod.GET, "/css/**/*")
                .antMatchers(HttpMethod.GET, "/images/**/*");
    }

    @Configuration
    @Order(1)
    public static class ManagementConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/management/**/*").authorizeRequests().anyRequest().hasAuthority("ADMIN")
                    .and().httpBasic().authenticationEntryPoint(authenticationEntryPoint())
                    .and().exceptionHandling().accessDeniedPage("/403");
        }

        @Bean
        public AuthenticationEntryPoint authenticationEntryPoint() {
            BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
            entryPoint.setRealmName("Management Realm");
            return entryPoint;
        }

        @Bean
        public WebAuthenticationSuccessHandler getSuccessHandler() {
            return new WebAuthenticationSuccessHandler();
        }

    }

    @Configuration
    @Order(2)
    public static class GuestConfigurationAdapter extends WebSecurityConfigurerAdapter {

        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/files-api/**").authorizeRequests().anyRequest().permitAll();
        }
    }

    @Configuration
    @Order(3)
    public static class GeneralLoginConfigurationAdapter extends WebSecurityConfigurerAdapter {

        protected void configure(HttpSecurity http) throws Exception {
            // @TODO 启用用csrf特性
            //  1. 删除 .csrf().disable() ,
            //  2. 涉及POST操作的HTML页面添加如下内容：
            //     head段： <meta name="_csrf_header" th:content="${_csrf.headerName}"/>
            //     form段： <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>

            http.csrf().disable().authorizeRequests()
                    .antMatchers("/**").hasAuthority("USER")
                    .anyRequest().authenticated()
                    .and()
                    .formLogin()
                    .loginPage("/login")
                    .successHandler(getSuccessHandler())
                    .permitAll()
                    .and()
                    .logout()
                    .permitAll()/*
                    .and()
                    .exceptionHandling()
                    .defaultAuthenticationEntryPointFor(
                            loginUrlauthenticationEntryPointWithWarning(),
                            new AntPathRequestMatcher("/user/private/**"))
                    .defaultAuthenticationEntryPointFor(
                            loginUrlauthenticationEntryPoint(),
                            new AntPathRequestMatcher("/user/general/**"))*/;
        }

        @Bean
        public AuthenticationEntryPoint loginUrlauthenticationEntryPoint() {
            return new LoginUrlAuthenticationEntryPoint("/userLogin");
        }

        @Bean
        public AuthenticationEntryPoint loginUrlauthenticationEntryPointWithWarning() {
            return new LoginUrlAuthenticationEntryPoint("/userLoginWithWarning");
        }

        @Bean
        public WebAuthenticationSuccessHandler getSuccessHandler() {
            return new WebAuthenticationSuccessHandler();
        }

    }

}