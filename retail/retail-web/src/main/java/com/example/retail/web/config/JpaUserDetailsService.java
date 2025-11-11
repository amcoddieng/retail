package com.example.retail.web.config;

import com.example.retail.domain.Utilisateur;
import com.example.retail.service.AuthService;
import javax.annotation.Resource;
import javax.ejb.EJB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class JpaUserDetailsService implements UserDetailsService {
    @Autowired
    private AuthService auth;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur u = auth.findByLogin(username);
        if (u == null || !u.isActif()) throw new UsernameNotFoundException("Utilisateur introuvable");
        var roles = auth.rolesOf(username);
        var auths = new ArrayList<SimpleGrantedAuthority>();
        for (String r : roles) auths.add(new SimpleGrantedAuthority("ROLE_" + r));
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getLogin()).password(u.getPasswordHash()).authorities(auths).accountLocked(!u.isActif()).build();
    }
}