package com.example.architecture.evolvable;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class ArchitectureRulesTest {

    private final JavaClasses classes = new ClassFileImporter().importPackages("com.example.architecture.evolvable");

    @Test
    void layered_dependencies_are_respected() {
        ArchRule layered = layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .layer("Interfaces").definedBy("..interfaces..")
                .layer("Application").definedBy("..application..")
                .layer("Domain").definedBy("..domain..")
                .layer("Infrastructure").definedBy("..infrastructure..")
                .layer("Api").definedBy("..api..")
                // Application can access Domain, Infrastructure, Api
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Interfaces", "Infrastructure")
                // Domain should not access anything outside (except Api if needed, but ideally pure)
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
                // Infrastructure implements Domain interfaces
                .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Application");

        layered.check(classes);
    }

    @Test
    void interfaces_do_not_depend_on_domain_or_infrastructure_directly() {
        noClasses()
                .that().resideInAPackage("..interfaces..")
                .should().dependOnClassesThat().resideInAnyPackage("..domain..", "..infrastructure..")
                .check(classes);
    }

    @Test
    void domain_is_pure_business_without_framework_dependencies() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta..")
                .check(classes);
    }
}
