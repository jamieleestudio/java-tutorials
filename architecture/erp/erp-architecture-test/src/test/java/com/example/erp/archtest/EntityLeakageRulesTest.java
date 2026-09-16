package com.example.erp.archtest;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class EntityLeakageRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = ArchitectureTestSupport.classes();
    }

    @Test
    void persistence_types_are_not_used_outside_infrastructure() {
        noClasses()
                .that().resideOutsideOfPackage("..infrastructure..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure.persistence..")
                .check(classes);
    }

    @Test
    void controllers_do_not_depend_on_repositories() {
        noClasses()
                .that().resideInAPackage("..interfaces..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..domain.repository..", "..infrastructure.persistence..")
                .check(classes);
    }
}
