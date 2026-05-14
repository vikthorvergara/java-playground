package com.github.vikthorvergara.spring.security.csrf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class SecurityPOC {

    @RestController
    public static class Endpoints {
        @GetMapping("/public/hello")
        public String publicHello() {
            return "anyone may read this";
        }

        @GetMapping("/secure/me")
        public String me(java.security.Principal principal) {
            return "secure: " + principal.getName();
        }

        @GetMapping("/admin/area")
        public String admin() {
            return "ADMIN ONLY";
        }

        @PostMapping("/data")
        public Map<String, Object> writeData(@RequestBody Map<String, Object> body) {
            return Map.of("accepted", true, "echo", body);
        }

        @GetMapping("/csrf-token")
        public Map<String, String> csrf(CsrfToken token) {
            return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
        }
    }

    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public UserDetailsService users(PasswordEncoder enc) {
        UserDetails alice = User.withUsername("alice").password("alicepw").roles("USER").build();
        UserDetails admin = User.withUsername("admin").password("adminpw").roles("USER", "ADMIN").build();
        return new InMemoryUserDetailsManager(alice, admin);
    }

    @Bean
    public SecurityFilterChain filter(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .csrf(c -> c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .httpBasic(org.springframework.security.config.Customizer.withDefaults())
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

    public static void main(String[] args) {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(SecurityPOC.class,
                "--server.port=0", "--logging.level.org.springframework.security=WARN")) {
            int port = ctx.getBean(PortHolder.class).port;
            String base = "http://localhost:" + port;

            publicEndpointPermitAll(base);
            secureEndpointRejectsAnonymous(base);
            secureEndpointAcceptsBasicAuth(base);
            adminAreaForbiddenForUserRole(base);
            adminAreaAllowedForAdminRole(base);
            postWithoutCsrfTokenIs403(base);
            postWithCsrfTokenIs200(base);
        }
    }

    static RestTemplate plain() {
        return new RestTemplate();
    }

    static RestTemplate basic(String user, String pw) {
        RestTemplate rt = new RestTemplate();
        rt.getInterceptors().add(new org.springframework.http.client.support.BasicAuthenticationInterceptor(user, pw));
        return rt;
    }

    static void publicEndpointPermitAll(String base) {
        System.out.println("--- /public permitAll ---");
        ResponseEntity<String> res = plain().getForEntity(base + "/public/hello", String.class);
        System.out.println("status=" + res.getStatusCode() + " body=" + res.getBody());
    }

    static void secureEndpointRejectsAnonymous(String base) {
        System.out.println("\n--- /secure anonymous -> 401 ---");
        try {
            plain().getForEntity(base + "/secure/me", String.class);
            System.out.println("UNEXPECTED 200");
        } catch (HttpClientErrorException e) {
            System.out.println("status=" + e.getStatusCode());
        }
    }

    static void secureEndpointAcceptsBasicAuth(String base) {
        System.out.println("\n--- /secure with basic auth -> 200 ---");
        ResponseEntity<String> res = basic("alice", "alicepw").getForEntity(base + "/secure/me", String.class);
        System.out.println("status=" + res.getStatusCode() + " body=" + res.getBody());
    }

    static void adminAreaForbiddenForUserRole(String base) {
        System.out.println("\n--- /admin as USER -> 403 ---");
        try {
            basic("alice", "alicepw").getForEntity(base + "/admin/area", String.class);
            System.out.println("UNEXPECTED 200");
        } catch (HttpClientErrorException e) {
            System.out.println("status=" + e.getStatusCode());
        }
    }

    static void adminAreaAllowedForAdminRole(String base) {
        System.out.println("\n--- /admin as ADMIN -> 200 ---");
        ResponseEntity<String> res = basic("admin", "adminpw").getForEntity(base + "/admin/area", String.class);
        System.out.println("status=" + res.getStatusCode() + " body=" + res.getBody());
    }

    static void postWithoutCsrfTokenIs403(String base) {
        System.out.println("\n--- POST /data without CSRF token -> rejected ---");
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(Map.of("k", "v"), h);
        try {
            basic("alice", "alicepw").postForEntity(base + "/data", req, String.class);
            System.out.println("UNEXPECTED 200");
        } catch (HttpClientErrorException e) {
            System.out.println("status=" + e.getStatusCode() + " (CSRF filter blocked the unsafe method)");
        }
    }

    @SuppressWarnings("unchecked")
    static void postWithCsrfTokenIs200(String base) {
        System.out.println("\n--- POST /data with CSRF token -> 200 ---");
        RestTemplate rt = basic("alice", "alicepw");
        ResponseEntity<Map> probe = rt.getForEntity(base + "/csrf-token", Map.class);
        Map<String, String> tokenInfo = probe.getBody();
        String token = tokenInfo.get("token");
        String headerName = tokenInfo.get("headerName");
        List<String> setCookies = probe.getHeaders().get(HttpHeaders.SET_COOKIE);
        System.out.println("token (first 8): " + token.substring(0, 8) + "... header: " + headerName);

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.add(headerName, token);
        if (setCookies != null) {
            for (String setCookie : setCookies) {
                String pair = setCookie.split(";", 2)[0];
                h.add(HttpHeaders.COOKIE, pair);
            }
        }
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(Map.of("k", "v"), h);
        ResponseEntity<String> res = rt.exchange(base + "/data", HttpMethod.POST, req, String.class);
        System.out.println("status=" + res.getStatusCode() + " body=" + res.getBody());
    }
}
