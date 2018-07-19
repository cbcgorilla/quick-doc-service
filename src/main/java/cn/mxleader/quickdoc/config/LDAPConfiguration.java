package cn.mxleader.quickdoc.config;

import cn.mxleader.quickdoc.entities.AuthAction;
import cn.mxleader.quickdoc.entities.AuthType;
import cn.mxleader.quickdoc.entities.Authorization;
import cn.mxleader.quickdoc.service.*;
import cn.mxleader.quickdoc.service.impl.LDAPServiceImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.naming.Context;
import java.util.HashSet;
import java.util.Properties;

/**
 * 依赖QuickDocConfiguration初始化配置信息，需增加AutoConfigureAfter注解
 */
@SpringBootConfiguration
@ConditionalOnClass(StreamService.class)
@AutoConfigureAfter(QuickDocConfiguration.class)
@EnableConfigurationProperties(LDAPProperties.class)
public class LDAPConfiguration {

    private final LDAPProperties ldapProperties;

    public LDAPConfiguration(LDAPProperties ldapProperties) {
        this.ldapProperties = ldapProperties;
    }

    @Bean
    public LDAPService ldapService(ConfigService configService) {
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");//"none","simple","strong"
        env.put(Context.SECURITY_PRINCIPAL, ldapProperties.getUsername());
        env.put(Context.SECURITY_CREDENTIALS, ldapProperties.getPassword());
        env.put(Context.PROVIDER_URL, ldapProperties.getUrl());
        return new LDAPServiceImpl(configService, env, ldapProperties.getSearchBase(), ldapProperties.getBlacklist());
    }

    /**
     * 初始化LDAP域账号（）
     *
     * @param ldapService
     * @param userService
     * @return
     */
    @Bean
    public CommandLineRunner initLdapUsers(LDAPService ldapService,
                                           UserService userService,
                                           DiskService diskService) {
        return args -> ldapService.searchLdapUsers()
                .filter(sysUser -> userService.get(sysUser.getUsername()) == null)
                .map(userService::saveUser)
                .subscribe(sysUser -> diskService.save("我的磁盘",
                        new Authorization(sysUser.getUsername(), AuthType.PRIVATE,
                                new HashSet<AuthAction>() {{
                                    add(AuthAction.READ);
                                    add(AuthAction.WRITE);
                                    add(AuthAction.DELETE);
                                }})));
    }

}
