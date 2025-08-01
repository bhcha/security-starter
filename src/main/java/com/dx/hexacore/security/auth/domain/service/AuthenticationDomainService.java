package com.dx.hexacore.security.auth.domain.service;

import com.dx.hexacore.security.auth.domain.Authentication;
import com.dx.hexacore.security.auth.domain.vo.Credentials;

public class AuthenticationDomainService {

    public Authentication authenticate(Credentials credentials) {
        if (credentials == null) {
            throw new IllegalArgumentException("Credentials cannot be null");
        }

        return Authentication.attemptAuthentication(credentials);
    }
}