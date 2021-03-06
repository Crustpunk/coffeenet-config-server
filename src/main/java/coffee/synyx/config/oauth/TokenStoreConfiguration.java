package coffee.synyx.config.oauth;

import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.io.Resource;

import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

import static org.springframework.util.StreamUtils.copyToString;

import static java.lang.invoke.MethodHandles.lookup;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * @author  Tobias Schneider - schneider@synyx.de
 */
@Configuration
@EnableConfigurationProperties(KeyStoreProperties.class)
class TokenStoreConfiguration {

    private static final Logger LOGGER = getLogger(lookup().lookupClass());

    private final ApplicationContext context;
    private final KeyStoreProperties keyStoreProperties;

    @Autowired
    TokenStoreConfiguration(ApplicationContext context, KeyStoreProperties keyStoreProperties) {

        this.context = context;
        this.keyStoreProperties = keyStoreProperties;
    }

    @Bean
    @Autowired
    TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {

        JwtTokenStore jwtTokenStore = new JwtTokenStore(jwtAccessTokenConverter);
        LOGGER.info("//> JwtTokenStore created");

        return jwtTokenStore;
    }


    @Bean
    JwtAccessTokenConverter jwtAccessTokenConverter() {

        final JwtAccessTokenConverter converter = new JwtAccessTokenConverter();

        if (keyStoreProperties.isEnabled()) {
            final Resource resource = context.getResource(keyStoreProperties.getPublicKey());

            String publicKey;

            try {
                publicKey = copyToString(resource.getInputStream(), UTF_8);
            } catch (IOException e) {
                throw new PublicKeyException("Could not retrieve the public CoffeeNet key", e);
            }

            converter.setVerifierKey(publicKey);
            LOGGER.info("//> Prepared and added public key to JwtAccessTokenConverter");
        }

        LOGGER.info("//> JwtAccessTokenConverter created");

        return converter;
    }
}
