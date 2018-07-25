package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.common.utils.HanyuPinyinUtil;
import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.service.ConfigService;
import cn.mxleader.quickdoc.service.LDAPService;
import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;

public class LDAPServiceImpl implements LDAPService {

    private final Properties env;
    private final String defaultSearchBase;
    private final String blacklist[];
    private static final String defaultOrgFilter = "(&(objectClass=organizationalUnit)(name=*))";
    private static final String defaultPersonFilter = "(&(objectCategory=Person)(objectClass=user)(name=*))";
    private static final String defaultOrgAtts[] = {"distinguishedName", "name","objectGUID","ou"};
    private static final String defaultPersonAtts[] = {"distinguishedName", "memberOf", "name", "sAMAccountName",
            "displayName", "title", "mail", "department"};

    private final ConfigService configService;

    public LDAPServiceImpl(ConfigService configService, Properties env,
                           String defaultSearchBase,String blacklist[]) {
        this.configService = configService;
        this.env = env;
        this.defaultSearchBase = defaultSearchBase;
        this.blacklist = blacklist;
    }

    public Flux<SearchResult> searchLdapItems(String searchFilter, String returnedAtts[], String searchBase)
            throws NamingException {
        LdapContext ctx = new InitialLdapContext(env, null);
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtls.setReturningAttributes(returnedAtts);
        NamingEnumeration<SearchResult> answer = ctx.search(searchBase, searchFilter, searchCtls);
        Flux<SearchResult> searchResultFlux = Flux.fromStream(Collections.list(answer).stream());
        ctx.close();
        return searchResultFlux;
    }

    public Flux<SysUser> searchLdapUsers(String searchBase) throws NamingException {
        Flux<SysUser> sysUserFlux = searchLdapItems(defaultPersonFilter, defaultPersonAtts, searchBase)
                .map(sr -> {
                    try {
                        String sn = sr.getAttributes().get("sAMAccountName").get().toString();
                        String displayName = sr.getAttributes().get("displayName").get().toString();
                        String title = sr.getAttributes().get("title").get().toString();
                        String email = sr.getAttributes().get("mail").get().toString();
                        String department = sr.getAttributes().get("department").get().toString();
                        return new SysUser(ObjectId.get(), sn, displayName, title,
                                HanyuPinyinUtil.toHanyuPinyin(displayName),
                                configService.getSysProfile().getIconMap().get("AWARD"),
                                true, department,
                                new HashSet<SysUser.Authority>() {{
                                    add(SysUser.Authority.USER);
                                }},
                                new HashSet<String>() {{
                                    add("users");
                                    add(department);
                                }},
                                email);
                    } catch (NamingException exp) {
                        exp.printStackTrace();
                        return null;
                    }
                });
        // 过滤黑名单部门人员
        if(blacklist!=null && blacklist.length>0){
            return sysUserFlux.filter(sysUser -> {
                for (String blackItem : blacklist) {
                    if(sysUser.getDepartment().startsWith(blackItem))
                        return false;
                }
                return true;
            });
        }else {
            return sysUserFlux;
        }
    }

    public Flux<SysUser> searchLdapUsers() throws NamingException {
        return searchLdapUsers(defaultSearchBase);
    }

    public Flux<SearchResult> searchLdapOrg(String searchBase) throws NamingException {
        return searchLdapItems(defaultOrgFilter, defaultOrgAtts, searchBase);
    }

    public Flux<SearchResult> searchLdapOrg() throws NamingException {
        return searchLdapOrg(defaultSearchBase);
    }

}
