package com.axel.trainingmetricsapi;

import com.axel.trainingmetricsapi.controller.security.JwtUtils;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.beAnnotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.GeneralCodingRules.THROW_GENERIC_EXCEPTIONS;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

class ArchitectureTests {

    static final JavaClasses productionClasses = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.axel.trainingmetricsapi");

    static final JavaClasses allClasses = new ClassFileImporter()
        .importPackages("com.axel.trainingmetricsapi");

    @Nested
    class LayerRules {

        @Test
        void layer_dependencies_are_respected() {
            ArchRule rule = layeredArchitecture().consideringAllDependencies()
                .layer("Controller").definedBy("..controller..")
                .layer("DTO").definedBy("..dto..")
                .layer("Application").definedBy("..application..")
                .layer("Infrastructure").definedBy("..infrastructure..")
                .layer("Repository").definedBy("..repository..")
                .layer("Domain").definedBy("..domain..")

                .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                .whereLayer("DTO").mayOnlyBeAccessedByLayers("Controller")
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Controller", "Infrastructure")
                .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
                .whereLayer("Repository").mayOnlyBeAccessedByLayers("Application")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Controller", "Application", "Repository", "Infrastructure", "DTO")

                .because("Each layer should only depend on its allowed neighbors — " +
                    "enforces separation of concerns and prevents architecture erosion");

            rule.check(productionClasses); // production only : tests import some classes like JwtUtils to manage exceptions
        }

        @Test
        void no_cyclic_dependencies() {
            ArchRule rule = slices().matching("com.axel.trainingmetricsapi.(*)..")
                .should().beFreeOfCycles();

            rule.check(allClasses); // test classes as well : tests should isolate and respect productions rules
        }
    }

    @Nested
    class DomainRules {

        @Test
        void domain_should_not_depend_on_other_layers() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..repository..", "..dto..", "..controller..", "..application..")
                .because("Domain should be independent of others layers");

            rule.check(allClasses); // test classes as well : tests should respect productions rules
        }

        @Test
        void no_framework_dependency_in_domain() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "javax.persistence..",
                    "org.hibernate..")
                .because("Domain layer should be framework-independent - no Spring, JPA, or Hibernate annotations");

            rule.check(allClasses); // test classes as well : tests should respect productions rules
        }

    }

    @Nested
    class SpringRules {

        @Test
        void rest_annotations_should_be_in_controller() {
            ArchRule rule = classes().that()
                .areAnnotatedWith(RestController.class)
                .or().areAnnotatedWith(RestControllerAdvice.class)
                .should().resideInAPackage("..controller..")
                .because("REST annotations should be in web layer");

            rule.check(allClasses); // test classes as well : tests should respect productions rules
        }

        @Test
        void service_annotations_should_be_in_application() {
            ArchRule rule = classes().that().areAnnotatedWith(Service.class)
                .should().resideInAPackage("..application..")
                .because("Service annotations should be in application layer");

            rule.check(allClasses); // test classes as well : tests should respect productions rules
        }

        @Test
        void no_spring_imports_in_application_layer() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                    "org.springframework.context..",
                    "org.springframework.cache..",
                    "org.springframework.security..",
                    "jakarta.persistence..")
                .because("Application layer should be framework-independent — " +
                    "infrastructure concerns must go through ports");

            rule.check(productionClasses);
        }

        @Test
        void entity_and_repository_annotations_should_be_in_repository() {
            ArchRule rule = classes().that()
                .areAnnotatedWith(Entity.class)
                .or().areAnnotatedWith(Repository.class)
                .should().resideInAPackage("..repository..")
                .because("Persistence annotations should be in repository layer");

            rule.check(allClasses); // test classes as well : tests should respect productions rules
        }

    }

    @Nested
    class CleanCodeRules {

        @Test
        void no_field_injection() {
            ArchRule rule = noFields()
                .should(beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired"))
                .orShould(beAnnotatedWith("com.google.inject.Inject"))
                .orShould(beAnnotatedWith("javax.inject.Inject"))
                .orShould(beAnnotatedWith("javax.annotation.Resource"))
                .orShould(beAnnotatedWith("jakarta.inject.Inject"))
                .orShould(beAnnotatedWith("jakarta.annotation.Resource"))
                .because("Use constructor injection instead of field injection");

            rule.check(productionClasses); // production only : Autowired annotations used to isolate web layer tests
        }

        @Test
        void no_generic_exceptions() {
            ArchRule rule = noClasses()
                .should(THROW_GENERIC_EXCEPTIONS)
                .because("Use specific exception types instead of generic ones");

            rule.check(allClasses); // test classes as well : tests should respect productions rules
        }

        @Test
        void should_use_java_time() {
            ArchRule rule = noClasses()
                .that().doNotBelongToAnyOf(JwtUtils.class) // JJWT API requires java.util.Date
                .should().dependOnClassesThat()
                .belongToAnyOf(java.util.Date.class, java.util.Calendar.class)
                .because("Use java.time (LocalDate, LocalDateTime) instead of legacy date classes");

            rule.check(productionClasses); // production only : test framework may legitimately use legacy date types
        }

        @Test
        void no_direct_output_or_java_logging() {
            ArchRule rule = noClasses()
                .should().callMethod(System.class, "println", String.class)
                .orShould().dependOnClassesThat().resideInAPackage("java.util.logging..")
                .because("Use SLF4J for logging instead of java.util.logging or System.out.println");

            rule.check(allClasses); // test classes as well : tests should respect productions rules
        }
    }

    @Nested
    class JpaRules {

        @Test
        void no_jpa_dependency_outside_repository() {
            ArchRule rule = noClasses()
                .that().resideOutsideOfPackage("..repository..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..")
                .because("JPA dependency should be confined to the repository layer");

            rule.check(productionClasses); // production only : this test references jakarta.persistence to define the rule
        }

        @Test
        void controllers_should_not_return_entities() {
            ArchRule rule = noMethods()
                .that().areDeclaredInClassesThat().resideInAPackage("..controller..")
                .should().haveRawReturnType(annotatedWith(Entity.class))
                .because("Controllers should return DTOs, not JPA entities");

            rule.check(allClasses);
        }
    }

    @Nested
    class ApiRules {

        @Test
        void openapi_models_should_stay_in_controller() {
            ArchRule rule = noClasses()
                .that().resideOutsideOfPackage("..controller..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("io.swagger..")
                .because("OpenAPI annotations should be confined to the controller layer — " +
                    "API documentation concerns should not leak into domain or service layers");

            rule.check(allClasses); // test classes as well : tests should respect productions rules
        }

    }
}
