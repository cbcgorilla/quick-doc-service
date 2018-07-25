package cn.mxleader.quickdoc.config;

import cn.mxleader.quickdoc.entities.AuthAction;
import cn.mxleader.quickdoc.entities.AuthType;
import cn.mxleader.quickdoc.entities.Authorization;
import cn.mxleader.quickdoc.service.*;
import cn.mxleader.quickdoc.service.impl.LDAPServiceImpl;
import cn.mxleader.quickdoc.web.domain.TreeNode;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.*;

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
        env.put("java.naming.ldap.attributes.binary", "objectGUID");
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
    @ConditionalOnProperty(prefix = "quickdoc.ldap", value = "enabled")
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

    @Bean
    public CommandLineRunner initLdapOrg(LDAPService ldapService) {
        return args -> {
            List<TreeNode> nodeList = ldapService.searchLdapOrg().map(sr -> {
                TreeNode node = null;
                try {
                    List<String> dir = Flux.fromArray(
                            sr.getAttributes().get("distinguishedName").get().toString().split(","))
                            .filter(name -> name.startsWith("OU="))
                            .map(name -> name.replaceFirst("OU=", ""))
                            .collectList().block();
                    Collections.reverse(dir);
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < dir.size()-1; i++) {
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
                if(parent != null)
                        parent.addChildren(node);
            }
            System.out.println(nodeList.get(0));
        };
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
