package com.example.erp.archtest;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * The published contract ({@code ..api..XxxApi}) and the local use cases
 * ({@code ..application..XxxApplicationService}) are independent:
 * the contract is implemented only by {@code ..interfaces.provider..}, never by
 * the application service itself.
 */
class ApiContractRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = ArchitectureTestSupport.classes();
    }

    @Test
    void api_interfaces_are_implemented_only_by_providers() {
        classes().should(new ArchCondition<JavaClass>("implement a domain api interface only from interfaces.provider") {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                for (JavaClass implemented : item.getAllRawInterfaces()) {
                    if (isDomainApi(implemented) && !item.getPackageName().contains(".interfaces.provider")) {
                        events.add(SimpleConditionEvent.violated(item,
                                item.getName() + " implements " + implemented.getName()
                                        + " (implementors must live in ..interfaces.provider..)"));
                    }
                }
            }
        }).check(classes);
    }

    private static boolean isDomainApi(JavaClass candidate) {
        return candidate.isInterface()
                && candidate.getSimpleName().endsWith("Api")
                && candidate.getPackageName().startsWith("com.example.erp.")
                && candidate.getPackageName().endsWith(".api");
    }
}
