package com.ldx.hexacore.security.auth.domain.service;

import com.ldx.hexacore.security.auth.domain.Authentication;
import com.ldx.hexacore.security.auth.domain.vo.Credentials;
import com.ldx.hexacore.security.util.ValidationMessages;

public class AuthenticationDomainService {

    public Authentication authenticate(Credentials credentials) {
        if (credentials == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Credentials"));
        }

        return Authentication.attemptAuthentication(credentials);
    }
}