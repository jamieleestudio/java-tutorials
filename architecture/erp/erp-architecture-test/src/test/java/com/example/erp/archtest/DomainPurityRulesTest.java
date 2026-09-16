package com.example.erp.archtest;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class DomainPurityRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = ArchitectureTestSupport.classes();
    }

    @Test
    void domain_does_not_depend_on_frameworks() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..", "jakarta..", "javax..", "com.fasterxml..")
                .check(classes);
    }

    @Test
    void domain_does_not_depend_on_other_layers() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..application..", "..infrastructure..", "..interfaces..")
                .check(classes);
    }
}
