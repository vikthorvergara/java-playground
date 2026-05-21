package com.github.vikthorvergara.spring.security.method;

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
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootApplication
@EnableMethodSecurity
public class MethodSecurityPOC {

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

    public record Document(long id, String owner, String text) {
    }

    @Service
    public static class DocumentService {
        private final Map<Long, Document> store = new ConcurrentHashMap<>();
        private final AtomicLong seq = new AtomicLong();

        @PreAuthorize("#owner == authentication.name")
        public Document create(String owner, String text) {
            long id = seq.incrementAndGet();
            Document doc = new Document(id, owner, text);
            store.put(id, doc);
            return doc;
        }

        @PostAuthorize("returnObject.owner == authentication.name")
        public Document get(long id) {
            return store.get(id);
        }

        public Document seed(String owner, String text) {
            long id = seq.incrementAndGet();
            Document doc = new Document(id, owner, text);
            store.put(id, doc);
            return doc;
        }
    }

    @RestController
    public static class Endpoints {
        private final DocumentService docs;

        public Endpoints(DocumentService docs) {
            this.docs = docs;
        }

        @GetMapping("/docs/create")
        public Document create(@RequestParam String owner, @RequestParam String text) {
            return docs.create(owner, text);
        }

        @GetMapping("/docs/{id}")
        public Document get(@PathVariable long id) {
            return docs.get(id);
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
        try (ConfigurableApplicationContext ctx = SpringApplication.run(MethodSecurityPOC.class,
                "--server.port=0", "--logging.level.org.springframework.security=WARN")) {
            int port = ctx.getBean(PortHolder.class).port;
            String base = "http://localhost:" + port;
            JwtEncoder encoder = ctx.getBean(JwtEncoder.class);
            DocumentService docs = ctx.getBean(DocumentService.class);

            long aliceDoc = docs.seed("alice", "alice secret").id();
            long bobDoc = docs.seed("bob", "bob secret").id();

            String aliceToken = mintToken(encoder, "alice");

            preAuthorizeAllowsCreateForSelf(base, aliceToken);
            preAuthorizeBlocksCreateForOther(base, aliceToken);
            postAuthorizeAllowsReadingOwnDocument(base, aliceToken, aliceDoc);
            postAuthorizeBlocksReadingOthersDocument(base, aliceToken, bobDoc);
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

    static void preAuthorizeAllowsCreateForSelf(String base, String token) {
        System.out.println("--- @PreAuthorize(#owner == authentication.name): alice creates own doc -> 200 ---");
        ResponseEntity<String> res = bearer(token).getForEntity(base + "/docs/create?owner=alice&text=hi", String.class);
        System.out.println("status=" + res.getStatusCode() + " body=" + res.getBody());
    }

    static void preAuthorizeBlocksCreateForOther(String base, String token) {
        System.out.println("\n--- @PreAuthorize blocks alice creating doc owned by bob -> 403 ---");
        try {
            bearer(token).getForEntity(base + "/docs/create?owner=bob&text=nope", String.class);
            System.out.println("UNEXPECTED 200");
        } catch (HttpClientErrorException e) {
            System.out.println("status=" + e.getStatusCode());
        }
    }

    static void postAuthorizeAllowsReadingOwnDocument(String base, String token, long id) {
        System.out.println("\n--- @PostAuthorize(returnObject.owner == name): alice reads own doc -> 200 ---");
        ResponseEntity<String> res = bearer(token).getForEntity(base + "/docs/" + id, String.class);
        System.out.println("status=" + res.getStatusCode() + " body=" + res.getBody());
    }

    static void postAuthorizeBlocksReadingOthersDocument(String base, String token, long id) {
        System.out.println("\n--- @PostAuthorize blocks alice reading bob's doc -> 403 ---");
        try {
            bearer(token).getForEntity(base + "/docs/" + id, String.class);
            System.out.println("UNEXPECTED 200");
        } catch (HttpClientErrorException e) {
            System.out.println("status=" + e.getStatusCode());
        }
    }
}
