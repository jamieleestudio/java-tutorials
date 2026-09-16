package com.example.erp.archtest;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

class DomainIsolationRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = ArchitectureTestSupport.classes();
    }

    @Test
    void cross_domain_dependencies_use_api_package_only() {
        classes()
                .should(ArchitectureTestSupport.dependOnOtherDomainsOnlyThroughApi())
                .check(classes);
    }

    @Test
    void domains_are_free_of_cycles() {
        slices().matching("com.example.erp.(*)..")
                .should().beFreeOfCycles()
                .check(classes);
    }
}
