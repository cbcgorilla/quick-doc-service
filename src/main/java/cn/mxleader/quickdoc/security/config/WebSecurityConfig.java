package cn.mxleader.quickdoc.security.config;

import cn.mxleader.quickdoc.security.authprovider.WebAuthenticationProvider;
import cn.mxleader.quickdoc.security.handler.ApiAccessDeniedHandler;
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

    private static final String AUTHORITY_USER = "USER";
    private static final String AUTHORITY_ADMIN = "ADMIN";

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
                .antMatchers(HttpMethod.GET,"/css/**/*")
                .antMatchers(HttpMethod.GET, "/fonts/**/*")
                .antMatchers(HttpMethod.GET, "/images/**/*")
                .antMatchers(HttpMethod.GET, "/js/**/*")
                .antMatchers(HttpMethod.GET, "/less/**/*")
                .antMatchers(HttpMethod.GET, "/scss/**/*");
    }

    @Configuration
    @Order(1)
    public static class ManagementConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // @TODO 删除.csrf().disable() 可屏蔽 /management 路径下的POST提交，仅支持GET方法交互
            http.csrf().disable().antMatcher("/management/**").authorizeRequests()
                    .anyRequest().hasAuthority(AUTHORITY_ADMIN)
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

            http.antMatcher("/rest/**").authorizeRequests()
                    .anyRequest().hasAuthority(AUTHORITY_USER)
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

        private static final String[] SWAGGER_AUTH_WHITELIST = {

                // -- swagger ui
                "/swagger-resources/**",
                "/swagger-ui.html",
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
            http
                    .authorizeRequests()
                    .antMatchers(SWAGGER_AUTH_WHITELIST).permitAll()
                    .antMatchers("/login*", "/login*", "/signin/**", "/signup/**", "/invalidSession*",
                            "/user/registration*", "/registrationConfirm*", "/expiredAccount*", "/registration*",
                            "/forgetPassword*", "/user/resetPassword*", "/user/changePassword*").permitAll()
                    //.antMatchers("/invalidSession*").anonymous()
                    .antMatchers("/admin*").hasAuthority(AUTHORITY_ADMIN)
                    .anyRequest().hasAuthority(AUTHORITY_USER)
                    .and()
                    .formLogin()
                    .loginPage("/login")
                    .defaultSuccessUrl("/homepage.html")
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
                    .permitAll();
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

    }

}