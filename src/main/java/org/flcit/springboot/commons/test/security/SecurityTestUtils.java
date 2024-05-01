/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flcit.springboot.commons.test.security;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;

import org.flcit.commons.core.util.ObjectUtils;
import org.flcit.commons.core.util.StringUtils;
import org.flcit.springboot.commons.test.security.filter.ClearUserFilter;
import org.flcit.springboot.commons.test.security.filter.UserFilter;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
public final class SecurityTestUtils {

    private SecurityTestUtils() { }

    /**
     * @param roles
     * @param authorities
     * @return
     */
    public static User getUser(String[] roles, String[] authorities) {
        return getUser(null, null, roles, authorities);
    }

    private static User getUser(String username, String password, String[] roles, String[] authorities) {
        final List<GrantedAuthority> grantedAuthorities = new ArrayList<>(1);
        if (!org.springframework.util.ObjectUtils.isEmpty(authorities)) {
            for (String authority : authorities) {
                grantedAuthorities.add(new SimpleGrantedAuthority(authority));
            }
        }
        if (!org.springframework.util.ObjectUtils.isEmpty(roles)) {
            for (String role : roles) {
                grantedAuthorities.add(new SimpleGrantedAuthority(StringUtils.prefixIfMissing("ROLE_", role)));
            }
        }
        return new User(ObjectUtils.getOrDefault(username, "ANONYMOUS"), ObjectUtils.getOrDefault(password, "PASSWORD"), true, true, true, true, grantedAuthorities);
    }

    /**
     * @param chain
     * @param user
     */
    public static void addSecurityContextUser(final SecurityFilterChain chain, final User user) {
        TestSecurityContextHolder.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(user,
                user.getPassword(), user.getAuthorities()));
        boolean userGenericFilterBean = false;
        boolean clearUserGenericFilterBean = false;
        for (Filter filter : chain.getFilters()) {
            if (filter instanceof UserFilter) {
                userGenericFilterBean = true;
            }
            if (filter instanceof ClearUserFilter) {
                clearUserGenericFilterBean = true;
            }
            if (userGenericFilterBean && clearUserGenericFilterBean) {
                break;
            }
        }
        if (!userGenericFilterBean) {
            chain.getFilters().add(Math.min(chain.getFilters().size() - 1, 6), new UserFilter());
        }
        if (!userGenericFilterBean) {
            chain.getFilters().add(chain.getFilters().size() - 1, new ClearUserFilter());
        }
    }

}
