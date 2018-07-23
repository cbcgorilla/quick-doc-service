package cn.mxleader.quickdoc.security.provider;

import cn.mxleader.quickdoc.security.exp.UserLogonException;
import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.stream.Collectors;

@Component
public class WebAuthenticationProvider
        implements AuthenticationProvider {

    @Value("${quickdoc.ldap.url}")
    private String ldapUrl;
    @Value("${quickdoc.ldap.domain}")
    private String domain;

    @Autowired
    private UserService userService;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        if (userService.validateUser(username, password)
                || (userService.get(username) != null && checkAdUser(username, password))) {
            SysUser sysUser = userService.get(username);
            return new UsernamePasswordAuthenticationToken(sysUser.getUsername(),
                    sysUser.getPassword(),
                    sysUser.getAuthorities().stream()
                            .map(authority -> new WebAuthority(authority.name()))
                            .collect(Collectors.toList()));
        } else {
            throw new UserLogonException("登录错误，用户名或密码有误,请检查后重新输入！");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(
                UsernamePasswordAuthenticationToken.class);
    }

    /**
     * 验证AD域账号密码
     * @param username
     * @param password
     * @return
     */
    private boolean checkAdUser(String username, String password) {
        // 这里有两种格式，domain\User或邮箱的后缀名,建议用domain\User这种格式
        String user = domain + "\\" + username;
        Hashtable env = new Hashtable();
        DirContext ctx;
        env.put(Context.SECURITY_AUTHENTICATION, "simple");// 一种模式，不用管，就这么写就可以了
        env.put(Context.SECURITY_PRINCIPAL, user);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        try {
            ctx = new InitialDirContext(env);
            ctx.close();
            return true; // 验证成功返回true
        } catch (NamingException err) {
            return false;// 验证失败返回false
        }
    }
}
