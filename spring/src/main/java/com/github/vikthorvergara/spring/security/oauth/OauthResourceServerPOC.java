package com.github.vikthorvergara.spring.security.oauth;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.Principal;
import java.time.Instant;

@SpringBootApplication
public class OauthResourceServerPOC {

    static final RSAKey RSA_KEY = generateRsaKey();

    static RSAKey generateRsaKey() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            var kp = gen.generateKeyPair();
            return new RSAKey.Builder((RSAPublicKey) kp.getPublic())
                    .privateKey((RSAPrivateKey) kp.getPrivate())
                    .keyID("poc-key")
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @RestController
    public static class Endpoints {
        @GetMapping("/me")
        public String me(Principal principal) {
            return "authenticated as " + principal.getName();
        }
    }

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        return NimbusJwtDecoder.withPublicKey(RSA_KEY.toRSAPublicKey()).build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(RSA_KEY));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public SecurityFilterChain filter(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(a -> a.anyRequest().authenticated())
                .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(c -> c.disable())
                .build();
    }

    @Bean
    PortHolder portHolder() {
        return new PortHolder();
    }

    public static class PortHolder implements ApplicationListener<WebServerInitializedEvent> {
        volatile int port;

        @Override
        public void onApplicationEvent(WebServerInitializedEvent event) {
            this.port = event.getWebServer().getPort();
        }
    }

    static String mintToken(JwtEncoder encoder, String subject) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(RSA_KEY.getKeyID()).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public static void main(String[] args) {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(OauthResourceServerPOC.class,
                "--server.port=0", "--logging.level.org.springframework.security=WARN")) {
            int port = ctx.getBean(PortHolder.class).port;
            String base = "http://localhost:" + port;
            JwtEncoder encoder = ctx.getBean(JwtEncoder.class);

            missingTokenIs401(base);
            garbageTokenIs401(base);
            validBearerTokenIs200(base, mintToken(encoder, "alice"));
        }
    }

    static RestTemplate rt() {
        return new RestTemplate();
    }

    static RestTemplate bearer(String token) {
        RestTemplate rt = new RestTemplate();
        rt.getInterceptors().add((req, body, ex) -> {
            req.getHeaders().setBearerAuth(token);
            return ex.execute(req, body);
        });
        return rt;
    }

    static void missingTokenIs401(String base) {
        System.out.println("--- GET /me without bearer token -> 401 ---");
        try {
            rt().getForEntity(base + "/me", String.class);
            System.out.println("UNEXPECTED 200");
        } catch (HttpClientErrorException e) {
            System.out.println("status=" + e.getStatusCode() + " www-authenticate=" + e.getResponseHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE));
        }
    }

    static void garbageTokenIs401(String base) {
        System.out.println("\n--- GET /me with garbage token -> 401 ---");
        try {
            bearer("not-a-real-jwt").getForEntity(base + "/me", String.class);
            System.out.println("UNEXPECTED 200");
        } catch (HttpClientErrorException e) {
            System.out.println("status=" + e.getStatusCode());
        }
    }

    static void validBearerTokenIs200(String base, String token) {
        System.out.println("\n--- GET /me with valid signed JWT -> 200 ---");
        System.out.println("token (first 16): " + token.substring(0, 16) + "...");
        ResponseEntity<String> res = bearer(token).getForEntity(base + "/me", String.class);
        System.out.println("status=" + res.getStatusCode() + " body=" + res.getBody());
    }
}
