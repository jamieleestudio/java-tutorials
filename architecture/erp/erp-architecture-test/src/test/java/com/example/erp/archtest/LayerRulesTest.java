package com.example.erp.archtest;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class LayerRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = ArchitectureTestSupport.classes();
    }

    @Test
    void layered_dependencies_are_respected() {
        ArchRule rule = layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .layer("Interfaces").definedBy("..interfaces..")
                .layer("Application").definedBy("..application..")
                .layer("Domain").definedBy("..domain..")
                .layer("Infrastructure").definedBy("..infrastructure..")
                .whereLayer("Interfaces").mayNotBeAccessedByAnyLayer()
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Interfaces")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
                .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Application");
        rule.check(classes);
    }

    @Test
    void api_packages_do_not_depend_on_implementation_layers() {
        noClasses()
                .that().resideInAPackage("..api..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..application..", "..domain..", "..infrastructure..", "..interfaces..")
                .check(classes);
    }
}
