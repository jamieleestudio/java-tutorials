package com.example.erp.archtest;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ArchitectureTestSupport {

    static final String ROOT = "com.example.erp";

    private static final Pattern DOMAIN_PATTERN = Pattern.compile("com\\.example\\.erp\\.([a-z]+)\\..*");
    private static final Pattern END_PATTERN = Pattern.compile("com\\.example\\.erp\\..*\\.interfaces\\.([a-z]+)\\..*");
    private static final String[] NON_DOMAIN_SEGMENTS = {"platform", "shared", "app", "archtest"};

    private ArchitectureTestSupport() {
    }

    static JavaClasses classes() {
        return new ClassFileImporter().importPackages(ROOT);
    }

    static String domainOf(String packageName) {
        Matcher matcher = DOMAIN_PATTERN.matcher(packageName);
        if (!matcher.matches()) {
            return null;
        }
        String segment = matcher.group(1);
        for (String excluded : NON_DOMAIN_SEGMENTS) {
            if (excluded.equals(segment)) {
                return null;
            }
        }
        return segment;
    }

    static String endOf(String packageName) {
        Matcher matcher = END_PATTERN.matcher(packageName);
        return matcher.matches() ? matcher.group(1) : null;
    }

    static ArchCondition<JavaClass> dependOnOtherDomainsOnlyThroughApi() {
        return new ArchCondition<>("depend on other domains only through their api package") {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                String sourceDomain = domainOf(item.getPackageName());
                if (sourceDomain == null) {
                    return;
                }
                for (Dependency dependency : item.getDirectDependenciesFromSelf()) {
                    JavaClass target = dependency.getTargetClass();
                    String targetDomain = domainOf(target.getPackageName());
                    if (targetDomain == null || targetDomain.equals(sourceDomain)) {
                        continue;
                    }
                    if (!target.getPackageName().startsWith(ROOT + "." + targetDomain + ".api")) {
                        events.add(SimpleConditionEvent.violated(item,
                                item.getName() + " -> " + target.getName()
                                        + " (cross-domain dependencies must target the api package)"));
                    }
                }
            }
        };
    }

    static ArchCondition<JavaClass> endsDoNotDependOnEachOther() {
        return new ArchCondition<>("not depend on another end") {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                String sourceEnd = endOf(item.getPackageName());
                if (sourceEnd == null) {
                    return;
                }
                for (Dependency dependency : item.getDirectDependenciesFromSelf()) {
                    String targetEnd = endOf(dependency.getTargetClass().getPackageName());
                    if (targetEnd != null && !targetEnd.equals(sourceEnd)) {
                        events.add(SimpleConditionEvent.violated(item,
                                item.getName() + " -> " + dependency.getTargetClass().getName()));
                    }
                }
            }
        };
    }
}
