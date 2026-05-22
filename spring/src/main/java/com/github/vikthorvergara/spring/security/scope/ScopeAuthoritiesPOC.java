package com.github.vikthorvergara.spring.security.scope;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;

@SpringBootApplication
public class ScopeAuthoritiesPOC {

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
        @GetMapping("/whoami")
        public String whoami(Authentication auth) {
            return auth.getName() + " authorities=" + auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).sorted().collect(Collectors.toList());
        }

        @GetMapping("/orders")
        public String listOrders() {
            return "orders list";
        }

        @GetMapping("/orders/place")
        public String placeOrder() {
            return "order placed";
        }

        @GetMapping("/admin")
        public String admin() {
            return "admin console";
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
        JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>(scopes.convert(jwt));
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
            }
            return authorities;
        });
        return converter;
    }

    @Bean
    public SecurityFilterChain filter(HttpSecurity http, JwtAuthenticationConverter converter) throws Exception {
        return http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/orders/place").hasAuthority("SCOPE_orders.write")
                        .requestMatchers("/orders/**").hasAuthority("SCOPE_orders.read")
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(converter)))
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

    static String mintToken(JwtEncoder encoder, String subject, String scope, List<String> roles) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .claim("scope", scope)
                .claim("roles", roles)
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(RSA_KEY.getKeyID()).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public static void main(String[] args) {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ScopeAuthoritiesPOC.class,
                "--server.port=0", "--logging.level.org.springframework.security=WARN")) {
            int port = ctx.getBean(PortHolder.class).port;
            String base = "http://localhost:" + port;
            JwtEncoder encoder = ctx.getBean(JwtEncoder.class);

            String reader = mintToken(encoder, "reader", "orders.read", List.of());
            String writer = mintToken(encoder, "writer", "orders.read orders.write", List.of());
            String admin = mintToken(encoder, "boss", "orders.read", List.of("ADMIN"));

            scopeClaimMapsToScopeAuthorities(base, reader, admin);
            readScopeAllowsListButNotPlace(base, reader);
            writeScopeAllowsPlace(base, writer);
            roleClaimMapsToRoleAuthorityForAdminEndpoint(base, reader, admin);
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

    static void scopeClaimMapsToScopeAuthorities(String base, String reader, String admin) {
        System.out.println("--- scope claim -> SCOPE_ authorities, roles claim -> ROLE_ authorities ---");
        System.out.println("reader: " + bearer(reader).getForObject(base + "/whoami", String.class));
        System.out.println("boss:   " + bearer(admin).getForObject(base + "/whoami", String.class));
    }

    static void readScopeAllowsListButNotPlace(String base, String reader) {
        System.out.println("\n--- reader (SCOPE_orders.read) GET /orders -> 200, /orders/place -> 403 ---");
        ResponseEntity<String> ok = bearer(reader).getForEntity(base + "/orders", String.class);
        System.out.println("/orders status=" + ok.getStatusCode() + " body=" + ok.getBody());
        try {
            bearer(reader).getForEntity(base + "/orders/place", String.class);
            System.out.println("UNEXPECTED 200");
        } catch (HttpClientErrorException e) {
            System.out.println("/orders/place status=" + e.getStatusCode());
        }
    }

    static void writeScopeAllowsPlace(String base, String writer) {
        System.out.println("\n--- writer (SCOPE_orders.write) GET /orders/place -> 200 ---");
        ResponseEntity<String> res = bearer(writer).getForEntity(base + "/orders/place", String.class);
        System.out.println("status=" + res.getStatusCode() + " body=" + res.getBody());
    }

    static void roleClaimMapsToRoleAuthorityForAdminEndpoint(String base, String reader, String admin) {
        System.out.println("\n--- /admin needs ROLE_ADMIN: reader -> 403, boss -> 200 ---");
        try {
            bearer(reader).getForEntity(base + "/admin", String.class);
            System.out.println("UNEXPECTED 200");
        } catch (HttpClientErrorException e) {
            System.out.println("reader status=" + e.getStatusCode());
        }
        ResponseEntity<String> res = bearer(admin).getForEntity(base + "/admin", String.class);
        System.out.println("boss status=" + res.getStatusCode() + " body=" + res.getBody());
    }
}
