package cn.mxleader.quickdoc.service;

import cn.mxleader.quickdoc.entities.SysUser;
import reactor.core.publisher.Flux;

import javax.naming.NamingException;
import javax.naming.directory.SearchResult;

public interface LDAPService {

    Flux<SearchResult> searchLdapItems(String searchFilter, String returnedAtts[], String searchBase)
            throws NamingException;

    Flux<SysUser> searchLdapUsers(String searchBase) throws NamingException;

    Flux<SysUser> searchLdapUsers() throws NamingException;

    Flux<SearchResult> searchLdapOrg(String searchBase) throws NamingException;

    Flux<SearchResult> searchLdapOrg() throws NamingException;
}
