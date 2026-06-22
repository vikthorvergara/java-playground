package com.github.vikthorvergara.spring.security.permission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

@SpringBootApplication
@EnableMethodSecurity
public class PermissionEvaluatorPOC {

    public record Doc(String owner) {
    }

    public static class OwnerPermissionEvaluator implements PermissionEvaluator {
        @Override
        public boolean hasPermission(Authentication auth, Object target, Object permission) {
            return target instanceof Doc doc
                    && "edit".equals(permission)
                    && doc.owner().equals(auth.getName());
        }

        @Override
        public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
            return false;
        }
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(new OwnerPermissionEvaluator());
        return handler;
    }

    @Service
    public static class DocService {
        @PreAuthorize("hasPermission(#doc, 'edit')")
        public String edit(Doc doc) {
            return "edited doc owned by " + doc.owner();
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PermissionEvaluatorPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            DocService docs = ctx.getBean(DocService.class);
            authenticateAs("alice");
            permissionGrantedForOwnedDoc(docs);
            permissionDeniedForForeignDoc(docs);
        }
    }

    static void authenticateAs(String name) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(name, "n", List.of()));
    }

    static void permissionGrantedForOwnedDoc(DocService docs) {
        System.out.println("--- custom PermissionEvaluator: hasPermission(doc,'edit') true for owner ---");
        System.out.println("alice edits own doc = " + docs.edit(new Doc("alice")));
    }

    static void permissionDeniedForForeignDoc(DocService docs) {
        System.out.println("\n--- hasPermission false for a doc alice does not own -> denied ---");
        try {
            docs.edit(new Doc("bob"));
            System.out.println("UNEXPECTED: allowed");
        } catch (AccessDeniedException e) {
            System.out.println("alice edits bob's doc -> " + e.getClass().getSimpleName());
        }
    }
}
