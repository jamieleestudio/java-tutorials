package com.example.cn.order;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Architecture guard for the distributed system.
 * Key rule: order infrastructure talks to payment/product ONLY via their api modules
 * (Service interfaces) — never their domain/infrastructure/interfaces classes.
 */
class ArchitectureRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter().importPackages("com.example.cn");
    }

    @Test
    void domain_layers_do_not_depend_on_spring_or_jakarta() {
        noClasses()
                .that().resideInAnyPackage("..order.domain..", "..product.domain..", "..payment.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta..")
                .check(classes);
    }

    @Test
    void order_does_not_touch_payment_or_product_internals() {
        // Cross-process: only the provider's api module (application contract) is allowed.
        noClasses()
                .that().resideInAPackage("..order..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..payment.domain..", "..payment.application.impl..",
                        "..payment.infrastructure..", "..payment.interfaces..",
                        "..product.domain..", "..product.application.impl..",
                        "..product.infrastructure..", "..product.interfaces..")
                .check(classes);
    }

    @Test
    void payment_and_product_do_not_depend_on_order() {
        noClasses()
                .that().resideInAnyPackage("..payment..", "..product..")
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
    void rpc_clients_live_in_order_infrastructure_only() {
        // The ②→③ swap happens ONLY here — application/domain stay untouched.
        noClasses()
                .that().resideInAnyPackage("..order.application..", "..order.domain..", "..order.interfaces..")
                .should().dependOnClassesThat().resideInAPackage("..order.infrastructure.rpc..")
                .check(classes);
    }

    @Test
    void interfaces_do_not_access_infrastructure_or_impl() {
        noClasses()
                .that().resideInAnyPackage("..interfaces..")
                .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..application.impl..")
                .check(classes);
    }
}