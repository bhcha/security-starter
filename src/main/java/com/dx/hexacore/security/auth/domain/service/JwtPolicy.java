package com.dx.hexacore.security.auth.domain.service;

import com.dx.hexacore.security.auth.domain.vo.Token;

public class JwtPolicy {

    public boolean validate(Token token) {
        if (token == null) {
            return false;
        }

        return !token.isExpired();
    }
}