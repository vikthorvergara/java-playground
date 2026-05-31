package com.github.vikthorvergara.spring.web.validation.groups;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.GroupSequence;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
public class ValidationGroupsPOC {

    public interface First {
    }

    public interface Second {
    }

    @GroupSequence({First.class, Second.class})
    public interface OrderedChecks {
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = PasswordsMatchValidator.class)
    public @interface PasswordsMatch {
        String message() default "passwords must match";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    public static class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, SignUp> {
        @Override
        public boolean isValid(SignUp value, ConstraintValidatorContext context) {
            return value != null && value.password() != null && value.password().equals(value.confirmPassword());
        }
    }

    @PasswordsMatch(groups = Second.class)
    public record SignUp(
            @NotBlank(groups = First.class) @Email(groups = First.class) String email,
            @NotBlank(groups = First.class) @Size(min = 8, groups = First.class) String password,
            @NotBlank(groups = First.class) String confirmPassword) {
    }

    @RestController
    public static class Endpoints {
        @PostMapping("/signup")
        public Map<String, String> signup(@Validated(OrderedChecks.class) @RequestBody SignUp form) {
            return Map.of("email", form.email());
        }
    }

    @RestControllerAdvice
    public static class GlobalErrors {
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ProblemDetail onInvalid(MethodArgumentNotValidException e) {
            Map<String, String> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
            List<String> globalErrors = e.getBindingResult().getGlobalErrors().stream()
                    .map(ObjectError::getDefaultMessage).toList();
            ProblemDetail pd = ProblemDetail.forStatus(400);
            pd.setTitle("validation failed");
            pd.setProperty("fieldErrors", fieldErrors);
            pd.setProperty("globalErrors", globalErrors);
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
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ValidationGroupsPOC.class, "--server.port=0")) {
            int port = ctx.getBean(PortHolder.class).port;
            String base = "http://localhost:" + port;

            firstGroupAndSecondGroupBothPass(base);
            firstGroupFailureShortCircuitsSecondGroup(base);
            secondGroupCatchesCrossFieldMismatchOnlyAfterFirstPasses(base);
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

    static void firstGroupAndSecondGroupBothPass(String base) {
        System.out.println("--- valid email + 8+ char password + matching confirm -> 200 ---");
        ResponseEntity<String> res = rt().postForEntity(base + "/signup",
                jsonBody("{\"email\":\"a@b.co\",\"password\":\"longenough\",\"confirmPassword\":\"longenough\"}"),
                String.class);
        System.out.println("status=" + res.getStatusCode() + " body=" + res.getBody());
    }

    static void firstGroupFailureShortCircuitsSecondGroup(String base) {
        System.out.println("\n--- First group fails (short pwd, mismatched) -> 400 with field errors only ---");
        System.out.println("    note: cross-field PasswordsMatch in Second group is NOT evaluated");
        try {
            rt().postForEntity(base + "/signup",
                    jsonBody("{\"email\":\"a@b.co\",\"password\":\"abc\",\"confirmPassword\":\"different\"}"),
                    String.class);
            System.out.println("UNEXPECTED 200");
        } catch (HttpClientErrorException e) {
            System.out.println("status=" + e.getStatusCode() + " body=" + e.getResponseBodyAsString());
        }
    }

    static void secondGroupCatchesCrossFieldMismatchOnlyAfterFirstPasses(String base) {
        System.out.println("\n--- First group passes; Second group cross-field fails -> 400 with globalErrors only ---");
        try {
            rt().postForEntity(base + "/signup",
                    jsonBody("{\"email\":\"a@b.co\",\"password\":\"longenough\",\"confirmPassword\":\"different1\"}"),
                    String.class);
            System.out.println("UNEXPECTED 200");
        } catch (HttpClientErrorException e) {
            System.out.println("status=" + e.getStatusCode() + " body=" + e.getResponseBodyAsString());
        }
    }
}
