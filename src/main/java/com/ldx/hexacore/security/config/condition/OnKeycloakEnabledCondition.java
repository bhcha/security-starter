package com.ldx.hexacore.security.config.condition;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Keycloak 활성화 상태 기반 조건 검사 클래스입니다.
 * TokenProvider와 Keycloak 설정을 모두 검사합니다.
 */
public class OnKeycloakEnabledCondition extends SpringBootCondition {

    private static final String PROVIDER_PROPERTY = "security-starter.token-provider.provider";
    private static final String KEYCLOAK_ENABLED_PROPERTY = "security-starter.token-provider.keycloak.enabled";

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String provider = context.getEnvironment().getProperty(PROVIDER_PROPERTY, "keycloak");
        String keycloakEnabled = context.getEnvironment().getProperty(KEYCLOAK_ENABLED_PROPERTY, "true");

        ConditionMessage.Builder message = ConditionMessage
                .forCondition("Keycloak Enabled Condition");

        // Provider가 keycloak이고 keycloak.enabled가 true여야 함
        boolean isKeycloakProvider = "keycloak".equals(provider);
        boolean isKeycloakEnabled = Boolean.parseBoolean(keycloakEnabled);

        if (isKeycloakProvider && isKeycloakEnabled) {
            return ConditionOutcome.match(message.foundExactly("provider=keycloak and keycloak.enabled=true"));
        }

        if (!isKeycloakProvider) {
            return ConditionOutcome.noMatch(message.didNotFind("provider=keycloak")
                    .items("found provider=" + provider));
        }

        return ConditionOutcome.noMatch(message.didNotFind("keycloak.enabled=true")
                .items("found keycloak.enabled=" + keycloakEnabled));
    }
}