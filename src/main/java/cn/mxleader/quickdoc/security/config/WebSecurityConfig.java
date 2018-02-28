package cn.mxleader.quickdoc.security.config;

import cn.mxleader.quickdoc.entities.QuickDocUser;
import cn.mxleader.quickdoc.security.authprovider.WebAuthenticationProvider;
import cn.mxleader.quickdoc.security.handler.ApiAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private WebAuthenticationProvider authProvider;

    @Override
    protected void configure(
            AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authProvider);
    }

    @Override
    public void configure(WebSecurity web) {
        // 针对静态资源忽略访问认证
        web.ignoring()
                .antMatchers(HttpMethod.GET, "/css/**/*")
                .antMatchers(HttpMethod.GET, "/images/**/*")
                .antMatchers(HttpMethod.GET, "/js/**/*")
                .antMatchers(HttpMethod.GET, "/less/**/*")
                .antMatchers(HttpMethod.GET, "/scss/**/*")
                .antMatchers(HttpMethod.GET, "/webfonts/**/*");
    }

    @Configuration
    @Order(1)
    public static class ManagementConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // @TODO 删除.csrf().disable() 可屏蔽 /management 路径下的POST提交，仅支持GET方法交互
            http.csrf().disable().requestMatcher(
                    EndpointRequest.to("mongo-status", "quick-doc-health")).authorizeRequests()
                    .anyRequest().hasAuthority(QuickDocUser.Authorities.ADMIN.name())
                    .and().httpBasic()
                    .authenticationEntryPoint(authenticationEntryPoint())
                    .and().exceptionHandling()
                    .accessDeniedHandler(accessDeniedHandler());
        }

        @Bean
        public AccessDeniedHandler accessDeniedHandler() {
            return new ApiAccessDeniedHandler();
        }

        @Bean
        public AuthenticationEntryPoint authenticationEntryPoint() {
            BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
            entryPoint.setRealmName("Management Realm");
            return entryPoint;
        }
    }

    @Configuration
    @Order(2)
    public static class RestApiConfigurationAdapter extends WebSecurityConfigurerAdapter {

        protected void configure(HttpSecurity http) throws Exception {
            //http.antMatcher("/guest/**").authorizeRequests().anyRequest().permitAll();

            http.antMatcher("/api/**").authorizeRequests()
                    .anyRequest().hasAuthority(QuickDocUser.Authorities.ADMIN.name())
                    .and().httpBasic()
                    .authenticationEntryPoint(authenticationEntryPoint())
                    .and().exceptionHandling()
                    .accessDeniedHandler(accessDeniedHandler())
                    .and().csrf().disable();
        }

        @Bean
        public AccessDeniedHandler accessDeniedHandler() {
            return new ApiAccessDeniedHandler();
        }

        @Bean
        public AuthenticationEntryPoint authenticationEntryPoint() {
            BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
            entryPoint.setRealmName("Management Realm");
            return entryPoint;
        }
    }

    @Configuration
    @Order(3)
    public static class GeneralLoginConfigurationAdapter extends WebSecurityConfigurerAdapter {

        /**
         * SWAGGER API 接口文档界面资源授权白名单 swagger ui
         */
        private static final String[] SWAGGER_AUTH_WHITELIST = {
                //"/swagger-ui.html",
                "/swagger-resources/**",
                "/v2/api-docs",
                "/webjars/**"
        };

        @Autowired
        private AuthenticationSuccessHandler webAuthenticationSuccessHandler;

        @Autowired
        private AuthenticationFailureHandler webAuthenticationFailureHandler;

        @Autowired
        private LogoutSuccessHandler webLogoutSuccessHandler;

        protected void configure(HttpSecurity http) throws Exception {
            // @TODO 启用用csrf特性
            //  1. 删除 .csrf().disable() ,
            //  2. 涉及POST操作的HTML页面添加如下内容：
            //     head段： <meta name="_csrf_header" th:content="${_csrf.headerName}"/>
            //     form段： <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
            // @formatter:off
            http.csrf().disable()
                    .authorizeRequests()
                    .antMatchers("/login*", "/login*", "/signin/**", "/signup/**").permitAll()
                    //.antMatchers("/invalidSession*").anonymous()
                    .antMatchers(SWAGGER_AUTH_WHITELIST).permitAll()
                    .antMatchers("/admin*", "/admin/**", "/swagger-ui.html")
                    .hasAuthority(QuickDocUser.Authorities.ADMIN.name())
                    .anyRequest().hasAnyAuthority(QuickDocUser.Authorities.ADMIN.name(), QuickDocUser.Authorities.USER.name())
                    .and()
                    .formLogin()
                    .loginPage("/login")
                    //.defaultSuccessUrl("/index.html")
                    .failureUrl("/login?error=true")
                    .successHandler(webAuthenticationSuccessHandler)
                    /*.failureHandler(webAuthenticationFailureHandler) @TODO 未启用 */
                    //.authenticationDetailsSource(authenticationDetailsSource)
                    .permitAll()
                    .and()
                    .sessionManagement()
                    .invalidSessionUrl("/login")
                    .maximumSessions(1).sessionRegistry(sessionRegistry())
                    .and()
                    .sessionFixation().none()
                    .and()
                    .logout()
                    //.logoutUrl("/perform_logout")
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .deleteCookies("JSESSIONID")
                    .logoutSuccessHandler(webLogoutSuccessHandler)
                    .invalidateHttpSession(false)
                    .logoutSuccessUrl("/login?logout=true")
                    .permitAll()
                    .and().exceptionHandling()
                    .accessDeniedHandler(accessDeniedHandler());
            // @formatter:on
        }

        @Bean
        public SessionRegistry sessionRegistry() {
            return new SessionRegistryImpl();
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
        public AccessDeniedHandler accessDeniedHandler() {
            return new ApiAccessDeniedHandler();
        }


    }

}