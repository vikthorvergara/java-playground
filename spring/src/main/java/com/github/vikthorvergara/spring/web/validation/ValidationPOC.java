package com.github.vikthorvergara.spring.web.validation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootApplication
public class ValidationPOC {

    @Bean
    public SecurityFilterChain permitAll(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .csrf(c -> c.disable())
                .build();
    }

    public record SignUp(
            @NotBlank @Size(min = 3, max = 20) String username,
            @Email String email,
            @Min(18) @Max(120) int age) {
    }

    @RestController
    public static class Endpoints {
        @PostMapping("/signup")
        public Map<String, Object> signup(@Valid @RequestBody SignUp body) {
            return Map.of("accepted", true, "user", body.username());
        }
    }

    @ControllerAdvice
    public static class GlobalErrors {
        @ExceptionHandler(MethodArgumentNotValidException.class)
        @ResponseBody
        public ResponseEntity<ProblemDetail> onInvalid(MethodArgumentNotValidException ex) {
            ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
            pd.setTitle("Invalid request body");
            Map<String, String> fields = new LinkedHashMap<>();
            ex.getBindingResult().getFieldErrors().forEach(fe -> fields.put(fe.getField(), fe.getDefaultMessage()));
            pd.setProperty("fieldErrors", fields);
            return ResponseEntity.badRequest().body(pd);
        }
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
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ValidationPOC.class, "--server.port=0")) {
            int port = ctx.getBean(PortHolder.class).port;
            String base = "http://localhost:" + port;

            validBodyAccepted(base);
            blankUsernameRejectedWithProblemDetail(base);
            multipleFieldErrorsCollected(base);
            invalidEmailRejected(base);
        }
    }

    static RestTemplate rt() {
        return new RestTemplate();
    }

    static HttpEntity<String> jsonBody(String json) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(json, h);
    }

    static void validBodyAccepted(String base) {
        System.out.println("--- valid body -> 200 ---");
        String body = "{\"username\":\"alice\",\"email\":\"a@example.com\",\"age\":30}";
        ResponseEntity<String> res = rt().postForEntity(base + "/signup", jsonBody(body), String.class);
        System.out.println("status=" + res.getStatusCode() + " body=" + res.getBody());
    }

    static void blankUsernameRejectedWithProblemDetail(String base) {
        System.out.println("\n--- blank username -> 400 ProblemDetail ---");
        String body = "{\"username\":\"\",\"email\":\"a@example.com\",\"age\":30}";
        try {
            rt().postForEntity(base + "/signup", jsonBody(body), String.class);
            System.out.println("UNEXPECTED 200");
        } catch (HttpClientErrorException e) {
            System.out.println("status=" + e.getStatusCode() + " body=" + e.getResponseBodyAsString());
        }
    }

    static void multipleFieldErrorsCollected(String base) {
        System.out.println("\n--- multiple errors -> single 400 with all fields ---");
        String body = "{\"username\":\"x\",\"email\":\"not-an-email\",\"age\":12}";
        try {
            rt().postForEntity(base + "/signup", jsonBody(body), String.class);
            System.out.println("UNEXPECTED 200");
        } catch (HttpClientErrorException e) {
            System.out.println("status=" + e.getStatusCode() + " body=" + e.getResponseBodyAsString());
        }
    }

    static void invalidEmailRejected(String base) {
        System.out.println("\n--- invalid email -> 400 ---");
        String body = "{\"username\":\"alice\",\"email\":\"bogus\",\"age\":30}";
        try {
            rt().postForEntity(base + "/signup", jsonBody(body), String.class);
            System.out.println("UNEXPECTED 200");
        } catch (HttpClientErrorException e) {
            System.out.println("status=" + e.getStatusCode() + " body=" + e.getResponseBodyAsString());
        }
    }
}
