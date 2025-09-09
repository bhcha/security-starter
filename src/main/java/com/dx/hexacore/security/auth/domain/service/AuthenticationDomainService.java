package com.dx.hexacore.security.auth.domain.service;

import com.dx.hexacore.security.auth.domain.Authentication;
import com.dx.hexacore.security.auth.domain.vo.Credentials;
import com.dx.hexacore.security.util.ValidationMessages;

public class AuthenticationDomainService {

    public Authentication authenticate(Credentials credentials) {
        if (credentials == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Credentials"));
        }

        return Authentication.attemptAuthentication(credentials);
    }
}