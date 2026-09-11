package com.example.eda.order;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter().importPackages("com.example.eda");
    }

    @Test
    void domain_is_pure() {
        noClasses()
                .that().resideInAnyPackage("..order.domain..", "..payment.domain..", "..product.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..", "jakarta..", "com.fasterxml.jackson..")
                .check(classes);
    }

    @Test
    void application_depends_only_on_ports_and_contracts() {
        // application may use shared event contracts + product-api, never infrastructure
        noClasses()
                .that().resideInAnyPackage("..order.application..")
                .should().dependOnClassesThat().resideInAnyPackage("..order.infrastructure..")
                .check(classes);
    }

    @Test
    void interfaces_do_not_access_infrastructure_or_impl() {
        noClasses()
                .that().resideInAnyPackage("..order.interfaces..")
                .should().dependOnClassesThat().resideInAnyPackage("..order.infrastructure..", "..order.application.impl..")
                .check(classes);
    }

    @Test
    void outbox_is_infrastructure_only() {
        // The local message table is a persistence concern — never leaked upward.
        noClasses()
                .that().resideInAnyPackage("..order.application..", "..order.domain..", "..order.interfaces..")
                .should().dependOnClassesThat().resideInAnyPackage("..outbox..")
                .check(classes);
    }

    @Test
    void payment_and_product_do_not_depend_on_order() {
        noClasses()
                .that().resideInAnyPackage("..payment..", "..product..")
                .should().dependOnClassesThat().resideInAPackage("..order..")
                .check(classes);
    }
}