package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.web.domain.TreeNode;
import reactor.core.publisher.Flux;

import javax.naming.NamingException;
import javax.naming.directory.SearchResult;
import java.util.List;

public interface LDAPService {

    Flux<SearchResult> searchLdapItems(String searchFilter, String returnedAtts[], String searchBase)
            throws NamingException;

    Flux<SysUser> searchLdapUsers(String searchBase) throws NamingException;

    Flux<SysUser> searchLdapUsers() throws NamingException;

    Flux<SearchResult> searchLdapOrg(String searchBase) throws NamingException;

    List<TreeNode> getLdapOrgTree(String searchBase) throws NamingException;

    List<TreeNode> getLdapOrgTree() throws NamingException;
}
