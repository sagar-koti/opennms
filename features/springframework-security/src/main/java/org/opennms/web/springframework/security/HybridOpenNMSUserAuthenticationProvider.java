package org.opennms.web.springframework.security;

import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.model.OnmsUser;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

public class HybridOpenNMSUserAuthenticationProvider implements AuthenticationProvider, InitializingBean {
    private UserManager m_userManager = null;
    private SpringSecurityUserDao m_userDao = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_userManager);
        Assert.notNull(m_userDao);
    }
    
    public UserManager getUserManager() {
        return m_userManager;
    }
    
    public void setUserManager(final UserManager userManager) {
        m_userManager = userManager;
    }

    public SpringSecurityUserDao getUserDao() {
        return m_userDao;
    }
    
    public void setUserDao(final SpringSecurityUserDao userDao) {
        m_userDao = userDao;
    }
    
    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final String username = authentication.getPrincipal().toString();
        final String password = authentication.getCredentials().toString();
        final OnmsUser user = m_userDao.getByUsername(username);

        boolean hasUser = false;

        if (user == null) {
            throw new BadCredentialsException("Bad credentials");
        }

        try {
            hasUser = m_userManager.hasUser(user.getUsername());
        } catch (final Exception e) {
            throw new AuthenticationServiceException("An error occurred while checking for " + username + " in the UserManager", e);
        }
        if (hasUser) {
            if (!m_userManager.comparePasswords(username, password)) {
                throw new BadCredentialsException("Bad credentials");
            }
        } else {
            if (!m_userManager.checkSaltedPassword(password, user.getPassword())) {
                throw new BadCredentialsException("Bad credentials");
            }
        }

        if (user.getAuthorities().size() == 0) {
            user.addAuthority(SpringSecurityUserDao.ROLE_USER);
        }

        final AbstractAuthenticationToken token = new AbstractAuthenticationToken(user.getAuthorities()) {
            private static final long serialVersionUID = 3659409846867741010L;

            @Override
            public Object getPrincipal() {
                return user.getUsername();
            }

            @Override
            public Object getCredentials() {
                return user.getPassword();
            }
        };
        token.setAuthenticated(true);

        return token;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean supports(final Class authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

}