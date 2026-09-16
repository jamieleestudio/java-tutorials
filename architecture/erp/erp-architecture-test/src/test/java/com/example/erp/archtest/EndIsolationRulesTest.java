package com.example.erp.archtest;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class EndIsolationRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = ArchitectureTestSupport.classes();
    }

    @Test
    void ends_do_not_depend_on_each_other() {
        classes()
                .should(ArchitectureTestSupport.endsDoNotDependOnEachOther())
                .check(classes);
    }
}
