package com.github.vikthorvergara.spring.security.hierarchy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@SpringBootApplication
@EnableMethodSecurity
public class RoleHierarchyPOC {

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_USER");
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        return handler;
    }

    @Service
    public static class ReportService {
        @PreAuthorize("hasRole('USER')")
        public String read() {
            return "report data";
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RoleHierarchyPOC.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext ctx = app.run(args)) {
            ReportService reports = ctx.getBean(ReportService.class);
            adminInheritsUserRoleThroughHierarchy(reports);
            unrelatedRoleStillDenied(reports);
        }
    }

    static void authenticateAs(String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("u", "n",
                        List.of(new SimpleGrantedAuthority(role))));
    }

    static void adminInheritsUserRoleThroughHierarchy(ReportService reports) {
        System.out.println("--- ROLE_ADMIN > ROLE_USER: admin passes hasRole('USER') without holding it ---");
        authenticateAs("ROLE_ADMIN");
        System.out.println("admin read() = " + reports.read() + " (granted via hierarchy)");
    }

    static void unrelatedRoleStillDenied(ReportService reports) {
        System.out.println("\n--- ROLE_GUEST is outside the hierarchy: still denied ---");
        authenticateAs("ROLE_GUEST");
        try {
            reports.read();
            System.out.println("UNEXPECTED: guest allowed");
        } catch (AccessDeniedException e) {
            System.out.println("guest read() -> " + e.getClass().getSimpleName());
        }
    }
}
