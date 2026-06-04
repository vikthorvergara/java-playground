package com.github.vikthorvergara.spring.security.problem;

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
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.security.KeyPairGenerator;
import java.security.Principal;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;

@SpringBootApplication
public class ProblemHandlersPOC {

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
        @GetMapping("/reports")
        public String reports(Principal principal) {
            return "reports for " + principal.getName();
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
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Object scope = jwt.getClaim("scope");
            if (scope instanceof String s && !s.isBlank()) {
                return List.of(s.split(" ")).stream()
                        .map(sc -> (GrantedAuthority) new SimpleGrantedAuthority("SCOPE_" + sc))
                        .toList();
            }
            return List.of();
        });
        return converter;
    }

    static void writeProblem(jakarta.servlet.http.HttpServletResponse res, int status, String title, String detail) throws java.io.IOException {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(org.springframework.http.HttpStatusCode.valueOf(status), detail);
        pd.setTitle(title);
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        String body = "{\"type\":\"about:blank\",\"title\":\"" + pd.getTitle()
                + "\",\"status\":" + pd.getStatus() + ",\"detail\":\"" + escape(pd.getDetail()) + "\"}";
        res.getWriter().write(body);
    }

    static String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (req, res, ex) -> writeProblem(res, 401, "Unauthorized", "Bearer token missing or invalid: " + ex.getMessage());
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (req, res, ex) -> writeProblem(res, 403, "Forbidden", "Insufficient scope: " + ex.getMessage());
    }

    @Bean
    public SecurityFilterChain filter(HttpSecurity http, JwtAuthenticationConverter converter,
                                      AuthenticationEntryPoint entryPoint, AccessDeniedHandler deniedHandler) throws Exception {
        return http
                .authorizeHttpRequests(a -> a.requestMatchers("/reports").hasAuthority("SCOPE_reports.read").anyRequest().authenticated())
                .oauth2ResourceServer(o -> o
                        .jwt(j -> j.jwtAuthenticationConverter(converter))
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(deniedHandler))
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler(deniedHandler))
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

    static String mintToken(JwtEncoder encoder, String subject, String scope) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .claim("scope", scope)
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(RSA_KEY.getKeyID()).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public static void main(String[] args) {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ProblemHandlersPOC.class,
                "--server.port=0", "--logging.level.org.springframework.security=WARN")) {
            int port = ctx.getBean(PortHolder.class).port;
            String base = "http://localhost:" + port;
            JwtEncoder encoder = ctx.getBean(JwtEncoder.class);

            missingTokenReturnsProblem401(base);
            wrongScopeReturnsProblem403(base, mintToken(encoder, "bob", "orders.read"));
            correctScopeReturns200(base, mintToken(encoder, "alice", "reports.read"));
        }
    }

    static RestTemplate bearer(String token) {
        RestTemplate rt = new RestTemplate();
        rt.getInterceptors().add((req, body, ex) -> {
            req.getHeaders().setBearerAuth(token);
            return ex.execute(req, body);
        });
        return rt;
    }

    static void missingTokenReturnsProblem401(String base) {
        System.out.println("--- no token -> 401 with application/problem+json body ---");
        try {
            new RestTemplate().getForEntity(base + "/reports", String.class);
            System.out.println("UNEXPECTED 200");
        } catch (HttpClientErrorException e) {
            System.out.println("status=" + e.getStatusCode());
            System.out.println("content-type=" + e.getResponseHeaders().getContentType());
            System.out.println("body=" + e.getResponseBodyAsString());
        }
    }

    static void wrongScopeReturnsProblem403(String base, String token) {
        System.out.println("\n--- valid token wrong scope (orders.read) -> 403 problem+json ---");
        try {
            bearer(token).getForEntity(base + "/reports", String.class);
            System.out.println("UNEXPECTED 200");
        } catch (HttpClientErrorException e) {
            System.out.println("status=" + e.getStatusCode());
            System.out.println("content-type=" + e.getResponseHeaders().getContentType());
            System.out.println("body=" + e.getResponseBodyAsString());
        }
    }

    static void correctScopeReturns200(String base, String token) {
        System.out.println("\n--- valid token with reports.read scope -> 200 ---");
        ResponseEntity<String> res = bearer(token).getForEntity(base + "/reports", String.class);
        System.out.println("status=" + res.getStatusCode() + " body=" + res.getBody());
    }
}
