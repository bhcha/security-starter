package com.ldx.hexacore.security;

import com.ldx.hexacore.security.config.properties.HexacoreSecurityProperties;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.config.autoconfigure.TokenProviderAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class HexacoreSecurityApplicationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    TokenProviderAutoConfiguration.class
            ))
            .withPropertyValues(
                    "hexacore.security.enabled=true"
            );

    @Test
    void contextLoads() {
        contextRunner.run(context -> {
            // 기본 자동 설정이 정상적으로 로딩되는지 확인
            assertThat(context).hasSingleBean(HexacoreSecurityProperties.class);
            assertThat(context).hasSingleBean(TokenProvider.class);
        });
    }

}
