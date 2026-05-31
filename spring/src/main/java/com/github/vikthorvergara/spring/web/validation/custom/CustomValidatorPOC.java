package com.github.vikthorvergara.spring.web.validation.custom;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SpringBootApplication
public class CustomValidatorPOC {

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = SkuValidator.class)
    public @interface Sku {
        String message() default "must match SKU pattern AAA-1234";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    public static class SkuValidator implements ConstraintValidator<Sku, String> {
        private static final Pattern PATTERN = Pattern.compile("^[A-Z]{3}-\\d{4}$");

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return value != null && PATTERN.matcher(value).matches();
        }
    }

    public record Item(@NotBlank String name, @Sku String sku, @Min(0) int qty) {
    }

    @RestController
    public static class Endpoints {
        @PostMapping("/items")
        public Item create(@Valid @RequestBody Item item) {
            return item;
        }
    }

    @RestControllerAdvice
    public static class GlobalErrors {
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ProblemDetail onInvalid(MethodArgumentNotValidException e) {
            Map<String, String> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
            ProblemDetail pd = ProblemDetail.forStatus(400);
            pd.setTitle("validation failed");
            pd.setProperty("fieldErrors", fieldErrors);
            return pd;
        }
    }

    @Bean
    public SecurityFilterChain permitAll(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
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

    public static void main(String[] args) {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(CustomValidatorPOC.class, "--server.port=0")) {
            int port = ctx.getBean(PortHolder.class).port;
            String base = "http://localhost:" + port;

            validItemAccepted(base);
            invalidSkuRejected(base);
            multipleViolationsReported(base);
        }
    }

    static RestTemplate rt() {
        return new RestTemplate();
    }

    static HttpEntity<String> jsonBody(String body) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, h);
    }

    static void validItemAccepted(String base) {
        System.out.println("--- valid SKU pattern AAA-1234 -> 200 ---");
        ResponseEntity<String> res = rt().postForEntity(base + "/items",
                jsonBody("{\"name\":\"keyboard\",\"sku\":\"KBD-0001\",\"qty\":3}"), String.class);
        System.out.println("status=" + res.getStatusCode() + " body=" + res.getBody());
    }

    static void invalidSkuRejected(String base) {
        System.out.println("\n--- bad SKU lowercase -> 400 with custom @Sku message ---");
        try {
            rt().postForEntity(base + "/items",
                    jsonBody("{\"name\":\"mouse\",\"sku\":\"abc-12\",\"qty\":1}"), String.class);
            System.out.println("UNEXPECTED 200");
        } catch (HttpClientErrorException e) {
            System.out.println("status=" + e.getStatusCode() + " body=" + e.getResponseBodyAsString());
        }
    }

    static void multipleViolationsReported(String base) {
        System.out.println("\n--- blank name + bad SKU + negative qty -> 400 with all field errors ---");
        try {
            rt().postForEntity(base + "/items",
                    jsonBody("{\"name\":\"\",\"sku\":\"nope\",\"qty\":-5}"), String.class);
            System.out.println("UNEXPECTED 200");
        } catch (HttpClientErrorException e) {
            System.out.println("status=" + e.getStatusCode() + " body=" + e.getResponseBodyAsString());
        }
    }
}
