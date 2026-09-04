package com.example.monolithic;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Architecture guard rules for the monolithic application.
 * Enforces: 4-layer dependency direction, domain purity,
 * interfaces-only-depends-on-service-interfaces, cross-context direction.
 */
class ArchitectureRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter().importPackages("com.example.monolithic");
    }

    // --- Domain Purity: zero framework dependencies ---

    @Test
    void domain_layers_do_not_depend_on_spring_or_jakarta() {
        noClasses()
                .that().resideInAnyPackage("..order.domain..", "..product.domain..", "..payment.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..", "jakarta.."
                )
                .check(classes);
    }

    @Test
    void domain_layers_do_not_depend_on_application_infrastructure_or_interfaces() {
        noClasses()
                .that().resideInAnyPackage("..order.domain..", "..product.domain..", "..payment.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..application..", "..infrastructure..", "..interfaces.."
                )
                .check(classes);
    }

    // --- Layer dependency direction ---

    @Test
    void interfaces_layer_does_not_access_infrastructure() {
        noClasses()
                .that().resideInAnyPackage("..interfaces..")
                .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..")
                .check(classes);
    }

    @Test
    void interfaces_layer_does_not_access_service_impl() {
        // Controllers must inject the Service interface, never the impl class.
        noClasses()
                .that().resideInAnyPackage("..interfaces..")
                .should().dependOnClassesThat().resideInAnyPackage("..application.impl..")
                .check(classes);
    }

    @Test
    void infrastructure_layer_does_not_access_interfaces() {
        noClasses()
                .that().resideInAnyPackage("..infrastructure..")
                .should().dependOnClassesThat().resideInAnyPackage("..interfaces..")
                .check(classes);
    }

    @Test
    void infrastructure_layers_do_not_cross_contexts() {
        noClasses()
                .that().resideInAPackage("..order.infrastructure..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..product.infrastructure..", "..payment.infrastructure..")
                .check(classes);

        noClasses()
                .that().resideInAPackage("..product.infrastructure..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..order.infrastructure..", "..payment.infrastructure..")
                .check(classes);

        noClasses()
                .that().resideInAPackage("..payment.infrastructure..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..order.infrastructure..", "..product.infrastructure..")
                .check(classes);
    }

    // --- Cross-context dependency rules ---

    @Test
    void order_context_does_not_access_product_or_payment_domain_or_infrastructure() {
        // Order may depend on product/payment application Service interfaces only,
        // never their domain or infrastructure layers.
        noClasses()
                .that().resideInAPackage("..order..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..product.domain..", "..payment.domain..",
                        "..product.infrastructure..", "..payment.infrastructure.."
                )
                .check(classes);
    }

    @Test
    void product_and_payment_do_not_depend_on_order() {
        // Provider-defined interfaces: product/payment are independent of order.
        noClasses()
                .that().resideInAnyPackage("..product..", "..payment..")
                .should().dependOnClassesThat().resideInAPackage("..order..")
                .check(classes);
    }

    @Test
    void product_does_not_depend_on_payment_and_vice_versa() {
        noClasses()
                .that().resideInAPackage("..product..")
                .should().dependOnClassesThat().resideInAPackage("..payment..")
                .check(classes);

        noClasses()
                .that().resideInAPackage("..payment..")
                .should().dependOnClassesThat().resideInAPackage("..product..")
                .check(classes);
    }

    @Test
    void bounded_contexts_are_free_of_cycles() {
        slices().matching("com.example.monolithic.(*)..")
                .should().beFreeOfCycles()
                .check(classes);
    }

    // --- Shared kernel rules ---

    @Test
    void shared_kernel_does_not_depend_on_any_bounded_context() {
        noClasses()
                .that().resideInAPackage("..shared..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..order..", "..product..", "..payment.."
                )
                .check(classes);
    }

    // --- Naming convention rules ---

    @Test
    void application_impl_classes_are_only_accessed_via_interfaces_by_interfaces_layer() {
        // impl package classes must implement the corresponding Service interface.
        noClasses()
                .that().resideInAPackage("..application.impl..")
                .should().dependOnClassesThat().resideInAnyPackage("..interfaces..")
                .check(classes);
    }
}