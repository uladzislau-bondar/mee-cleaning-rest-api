package com.mee.security.auth.ajax;

import com.mee.entity.Company;
import com.mee.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.mee.security.model.CompanyContext;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class AjaxAuthenticationProvider implements AuthenticationProvider {
    private final BCryptPasswordEncoder encoder;
    private final CompanyService companyService;

    @Autowired
    public AjaxAuthenticationProvider(CompanyService companyService, BCryptPasswordEncoder encoder) {
        this.companyService = companyService;
        this.encoder = encoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.notNull(authentication, "No authentication data provided");

        String email = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        Company company = companyService.getByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (!encoder.matches(password, company.getPassword())) {
            throw new BadCredentialsException("Authentication Failed. Email or Password not valid.");
        }

        if (company.getRoles() == null) throw new InsufficientAuthenticationException("User has no roles assigned");

        List<GrantedAuthority> authorities = company.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                .collect(Collectors.toList());
        
        CompanyContext companyContext = new CompanyContext(company.getEmail(), authorities);
        
        return new UsernamePasswordAuthenticationToken(companyContext, null, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
