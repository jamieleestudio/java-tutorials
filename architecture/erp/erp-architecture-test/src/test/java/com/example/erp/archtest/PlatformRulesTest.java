package com.example.erp.archtest;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class PlatformRulesTest {

    private static final String[] DOMAINS = {
            "system", "teachingplan", "exam", "attendance", "grade", "student", "enrollment",
            "dormitory", "moraleducation", "hr", "finance", "asset", "employment", "evaluation",
            "message", "iotterminal", "quality", "workflow", "integration"
    };

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = ArchitectureTestSupport.classes();
    }

    @Test
    void platform_does_not_depend_on_business_domains() {
        noClasses()
                .that().resideInAPackage("com.example.erp.platform..")
                .should().dependOnClassesThat().resideInAnyPackage(domainPackages())
                .check(classes);
    }

    @Test
    void shared_kernel_does_not_depend_on_business_domains() {
        noClasses()
                .that().resideInAPackage("com.example.erp.shared..")
                .should().dependOnClassesThat().resideInAnyPackage(domainPackages())
                .check(classes);
    }

    @Test
    void shared_kernel_is_dependency_free() {
        noClasses()
                .that().resideInAPackage("com.example.erp.shared..")
                .should().dependOnClassesThat().resideOutsideOfPackages("java..", "com.example.erp.shared..")
                .check(classes);
    }

    @Test
    void system_domain_is_a_pure_provider() {
        noClasses()
                .that().resideInAPackage("com.example.erp.system..")
                .should().dependOnClassesThat().resideInAnyPackage(otherDomainPackages())
                .check(classes);
    }

    private static String[] domainPackages() {
        return Arrays.stream(DOMAINS).map(d -> "com.example.erp." + d + "..").toArray(String[]::new);
    }

    private static String[] otherDomainPackages() {
        return Arrays.stream(DOMAINS)
                .filter(d -> !"system".equals(d))
                .map(d -> "com.example.erp." + d + "..")
                .toArray(String[]::new);
    }
}
