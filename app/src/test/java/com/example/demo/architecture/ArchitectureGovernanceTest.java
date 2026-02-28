package com.example.demo.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackages;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * 模块边界治理测试，在 CI test 阶段阻断跨边界依赖。
 */
class ArchitectureGovernanceTest {

    private static final String ORDER = "com.example.demo.order..";
    private static final String NOTICE = "com.example.demo.notice..";
    private static final String JOB = "com.example.demo.job..";
    private static final String EXTENSION = "com.example.demo.extension..";
    private static final String[] CONTRACT_PACKAGES = {"com.example.demo..api.."};

    private static final String[] BUSINESS_PACKAGES = {
            ORDER, NOTICE, JOB, EXTENSION,
            "com.example.demo.auth.config..",
            "com.example.demo.auth.controller..",
            "com.example.demo.auth.dto..",
            "com.example.demo.auth.service..",
            "com.example.demo.auth.support..",
            "com.example.demo.auth.web.."
    };

    private static final String[] IDENTITY_IMPL_PACKAGES = {
            "com.example.demo.user..",
            "com.example.demo.dept..",
            "com.example.demo.menu..",
            "com.example.demo.permission..",
            "com.example.demo.post..",
            "com.example.demo.datascope.."
    };

    private static final String[] BUSINESS_IMPL_PACKAGES = {
            ORDER, NOTICE, JOB, EXTENSION,
            "com.example.demo.auth.config..",
            "com.example.demo.auth.controller..",
            "com.example.demo.auth.dto..",
            "com.example.demo.auth.service..",
            "com.example.demo.auth.support..",
            "com.example.demo.auth.web.."
    };

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
            .importPackages("com.example.demo");

    @Test
    void businessModulesShouldNotDependOnIdentityImplPackages() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage(BUSINESS_PACKAGES)
                .should().dependOnClassesThat().resideInAnyPackage(IDENTITY_IMPL_PACKAGES)
                .because("业务模块必须通过 identity-api 契约访问身份域");
        rule.check(classes);
    }

    @Test
    void identityImplShouldNotDependOnBusinessImplPackages() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage(IDENTITY_IMPL_PACKAGES)
                .should().dependOnClassesThat().resideInAnyPackage(BUSINESS_IMPL_PACKAGES)
                .because("身份域实现不得反向依赖业务实现模块");
        rule.check(classes);
    }

    @Test
    void businessModulesShouldNotDependOnOtherBusinessModules() {
        noDependencyFrom(ORDER, NOTICE, JOB).check(classes);
        noDependencyFrom(NOTICE, ORDER, JOB).check(classes);
        noDependencyFrom(JOB, ORDER, NOTICE).check(classes);
        noDependencyFrom(EXTENSION, ORDER, NOTICE, JOB).check(classes);
    }

    private ArchRule noDependencyFrom(String sourcePackage, String... targetPackages) {
        return noClasses()
                .that().resideInAnyPackage(sourcePackage)
                .should().dependOnClassesThat(resideInAnyPackage(targetPackages)
                        .and(resideOutsideOfPackages(CONTRACT_PACKAGES)))
                .because("业务模块之间禁止跨边界直连，必须通过 *-api 契约模块");
    }
}
