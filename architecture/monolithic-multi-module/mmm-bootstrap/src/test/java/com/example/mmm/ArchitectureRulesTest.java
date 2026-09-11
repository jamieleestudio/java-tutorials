package com.example.mmm;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Architecture guard for the multi-module monolith.
 * Key difference vs ①: context boundaries are now Maven modules (compile-time),
 * these rules guard the REMAINING package-level concerns.
 */
class ArchitectureRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter().importPackages("com.example.mmm");
    }

    // --- Domain purity ---

    @Test
    void domain_layers_do_not_depend_on_spring_or_jakarta() {
        noClasses()
                .that().resideInAnyPackage("..order.domain..", "..product.domain..", "..payment.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta..")
                .check(classes);
    }

    @Test
    void shared_kernel_is_pure_java() {
        noClasses()
                .that().resideInAPackage("..shared..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta..")
                .check(classes);
    }

    // --- Layer direction ---

    @Test
    void interfaces_do_not_access_infrastructure_or_impl() {
        noClasses()
                .that().resideInAnyPackage("..interfaces..")
                .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..application.impl..")
                .check(classes);
    }

    @Test
    void domain_does_not_depend_on_application_or_infrastructure() {
        noClasses()
                .that().resideInAnyPackage("..order.domain..", "..product.domain..", "..payment.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..application..", "..infrastructure..", "..interfaces..")
                .check(classes);
    }

    // --- Cross-context: order may only touch product/payment application packages ---

    @Test
    void order_does_not_access_product_or_payment_domain_or_infrastructure() {
        noClasses()
                .that().resideInAPackage("..order..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..product.domain..", "..product.infrastructure..", "..product.interfaces..",
                        "..payment.domain..", "..payment.infrastructure..", "..payment.interfaces..")
                .check(classes);
    }

    @Test
    void product_and_payment_do_not_depend_on_order() {
        noClasses()
                .that().resideInAnyPackage("..product..", "..payment..")
                .should().dependOnClassesThat().resideInAPackage("..order..")
                .check(classes);
    }

    @Test
    void product_and_payment_are_independent() {
        noClasses().that().resideInAPackage("..product..")
                .should().dependOnClassesThat().resideInAPackage("..payment..")
                .check(classes);
        noClasses().that().resideInAPackage("..payment..")
                .should().dependOnClassesThat().resideInAPackage("..product..")
                .check(classes);
    }

    @Test
    void bounded_contexts_are_free_of_cycles() {
        slices().matching("com.example.mmm.(*)..")
                .should().beFreeOfCycles()
                .check(classes);
    }

    // --- Shared kernel must not depend on contexts ---

    @Test
    void shared_kernel_does_not_depend_on_contexts() {
        noClasses()
                .that().resideInAPackage("..shared..")
                .should().dependOnClassesThat().resideInAnyPackage("..order..", "..product..", "..payment..")
                .check(classes);
    }
}