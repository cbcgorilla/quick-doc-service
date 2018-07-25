package cn.mxleader.quickdoc.service.impl;

import cn.mxleader.quickdoc.common.utils.HanyuPinyinUtil;
import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.service.ConfigService;
import cn.mxleader.quickdoc.service.LDAPService;
import cn.mxleader.quickdoc.web.domain.TreeNode;
import org.bson.types.ObjectId;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.*;

public class LDAPServiceImpl implements LDAPService {

    private final Properties env;
    private final String defaultSearchBase;
    private final String blacklist[];
    private static final String defaultOrgFilter = "(&(objectClass=organizationalUnit)(name=*))";
    private static final String defaultPersonFilter = "(&(objectCategory=Person)(objectClass=user)(name=*))";
    private static final String defaultOrgAtts[] = {"distinguishedName", "name", "objectGUID", "ou"};
    private static final String defaultPersonAtts[] = {"distinguishedName", "memberOf", "name", "sAMAccountName",
            "displayName", "title", "mail", "department"};

    private final ConfigService configService;

    public LDAPServiceImpl(ConfigService configService, Properties env,
                           String defaultSearchBase, String blacklist[]) {
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
        if (blacklist != null && blacklist.length > 0) {
            return sysUserFlux.filter(sysUser -> {
                for (String blackItem : blacklist) {
                    if (sysUser.getDepartment().startsWith(blackItem))
                        return false;
                }
                return true;
            });
        } else {
            return sysUserFlux;
        }
    }

    public Flux<SysUser> searchLdapUsers() throws NamingException {
        return searchLdapUsers(defaultSearchBase);
    }

    public Flux<SearchResult> searchLdapOrg(String searchBase) throws NamingException {
        return searchLdapItems(defaultOrgFilter, defaultOrgAtts, searchBase);
    }

    public TreeNode getLdapOrgTree(String searchBase) throws NamingException {
        List<TreeNode> nodeList = searchLdapItems(defaultOrgFilter, defaultOrgAtts, searchBase).map(sr -> {
            TreeNode node = null;
            try {
                List<String> dir = Flux.fromArray(
                        sr.getAttributes().get("distinguishedName").get().toString().split(","))
                        .filter(name -> name.startsWith("OU="))
                        .map(name -> name.replaceFirst("OU=", ""))
                        .collectList().block();
                Collections.reverse(dir);
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < dir.size() - 1; i++) {
                    sb.append(dir.get(i) + "\\");
                }
                byte[] guid = (byte[]) sr.getAttributes().get("objectGUID").get();
                node = new TreeNode(bytesToHexString(guid), sr.getAttributes().get("name").get().toString(),
                        "", new ArrayList<>(), true, false, sb.toString(), dir.size());
            } catch (NamingException e) {
                e.printStackTrace();
            }
            return node;
        }).collectList().block();
        //组织按层级排序
        Collections.sort(nodeList, Comparator.comparingInt(TreeNode::getLevel));
        //Collections.sort(nodeList, (TreeNode t1, TreeNode t2) -> Integer.compare(t1.getLevel(),t2.getLevel()));
            /*
            Collections.sort(nodeList, new Comparator<TreeNode>() {
                @Override
                public int compare (TreeNode o1, TreeNode o2){
                    if (o1.getLevel() < o2.getLevel()) {
                        return -1;
                    } else if (o1.getLevel() > o2.getLevel()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });*/
        for (int i = 1; i < nodeList.size(); i++) {
            TreeNode node = nodeList.get(i);
            TreeNode parent = searchNodeList(nodeList, node.getPath(), node.getLevel() - 1);
            if (parent != null)
                parent.addChildren(node);
        }
        return nodeList.get(0);
    }

    public TreeNode getLdapOrgTree() throws NamingException {
        return getLdapOrgTree(defaultSearchBase);
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private static TreeNode searchNodeList(List<TreeNode> nodeList, String path, int level) {
        for (TreeNode item : nodeList) {
            if (path.equalsIgnoreCase(item.getPath() + item.getName() + "\\")
                    && item.getLevel() == level)
                return item;
        }
        return null;
    }

}
